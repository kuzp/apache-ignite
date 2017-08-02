/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.ml.trees.trainers.columnbased.vectors;

import com.zaxxer.sparsebits.SparseBitSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.ignite.lang.IgniteBiTuple;
import org.apache.ignite.ml.math.functions.IgniteFunction;
import org.apache.ignite.ml.trees.CategoricalRegionInfo;
import org.apache.ignite.ml.trees.CategoricalSplitInfo;
import org.apache.ignite.ml.trees.RegionInfo;
import org.apache.ignite.ml.trees.trainers.columnbased.Region;

import static org.apache.ignite.ml.trees.trainers.columnbased.vectors.Utils.splitByBitSet;

/**
 * Categorical feature vector implementation used by {@see ColumnDecisionTreeTrainer}.
 */
public class CategoricalFeatureVector
    implements FeatureVector<CategoricalRegionInfo, CategoricalSplitInfo<CategoricalRegionInfo>> {
    private final int catsCnt;
    /** Function for calculating impurity of a given region of points. */
    private IgniteFunction<DoubleStream, Double> calc;

    /** Minimal information gain value which is regarded as positive information gain. */
    private static double MIN_INGORMATION_GAIN = 1E-10;

    /**
     * @param calc Function for calculating impurity of a given region of points.
     * @param data Projection of samples on given feature in format of stream of (sample index, projection value).
     * @param samplesCnt Number of samples.
     * @param labels Labels of samples.
     * @param catsCnt Number of categories.
     */
    public CategoricalFeatureVector(IgniteFunction<DoubleStream, Double> calc, int catsCnt) {
//        samples = new ArrayList<>(samplesCnt);
        this.calc = calc;
        this.catsCnt = catsCnt;
    }

    /** {@inheritDoc} */
    @Override public double[] calculateRegions(Map<Integer, Region> regs, IgniteFunction<DoubleStream, Double> regCalc) {
        double[] res = new double[regs.size()];

        for (Map.Entry<Integer, Region> entry : regs.entrySet()) {
            Integer regIdx = entry.getKey();
            Region reg = entry.getValue();

            res[regIdx] = regCalc.apply(Arrays.stream(reg.samples()).mapToDouble(SampleInfo::getLabel));
        }

        return res;
    }

    /** */
    private SplitInfo<CategoricalRegionInfo> split(BitSet leftCats, int intervalIdx, Map<Integer, Integer> mapping,
        SampleInfo[] samples, double impurity) {
        Map<Boolean, List<SampleInfo>> leftRight = Arrays.stream(samples).
            collect(Collectors.partitioningBy((smpl) -> leftCats.get(mapping.get((int)smpl.getVal()))));

        List<SampleInfo> left = leftRight.get(true);
        int leftSize = left.size();
        double leftImpurity = calc.apply(left.stream().mapToDouble(SampleInfo::getLabel));

        List<SampleInfo> right = leftRight.get(false);
        int rightSize = right.size();
        double rightImpurity = calc.apply(right.stream().mapToDouble(SampleInfo::getLabel));

        int totalSize = leftSize + rightSize;

        // Result of this call will be sent back to trainer node, we do not need vectors inside of sent data.
        CategoricalSplitInfo<CategoricalRegionInfo> res = new CategoricalSplitInfo<>(intervalIdx,
            new CategoricalRegionInfo(leftImpurity, null), // cats can be computed on the last step.
            new CategoricalRegionInfo(rightImpurity, null),
            leftCats);

        res.setInfoGain(impurity - (double)leftSize / totalSize * leftImpurity - (double)rightSize / totalSize * rightImpurity);
        return res;
    }

    /**
     * Get a stream of subsets given categories count.
     *
     * @param catsCnt categories count.
     * @return Stream of subsets given categories count.
     */
    private Stream<BitSet> powerSet(int catsCnt) {
        Iterable<BitSet> iterable = () -> new PSI(catsCnt);
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    @Override public SplitInfo findBestSplit(Region<CategoricalRegionInfo> region, int regIdx) {
        Map<Integer, Integer> mapping = mapping(region.data().cats());

        return powerSet(region.data().cats().length()).
            map(s -> split(s, regIdx, mapping, region.samples(), region.data().impurity())).
            max(Comparator.comparingDouble(SplitInfo::infoGain)).
            orElse(null);
    }

    @Override public Region<CategoricalRegionInfo> createInitialRegion(SampleInfo[] samples) {
        BitSet set = new BitSet();
        set.set(0, catsCnt);

        Double impurity = calc.apply(Arrays.stream(samples).mapToDouble(SampleInfo::getVal));

        return new Region<>(samples, new CategoricalRegionInfo(impurity, set));
    }

    @Override public SparseBitSet calculateOwnershipBitSet(Region<CategoricalRegionInfo> region,
        CategoricalSplitInfo<CategoricalRegionInfo> s) {
        SparseBitSet res = new SparseBitSet();
        Arrays.stream(region.samples()).forEach(smpl -> res.set(smpl.getSampleInd(), s.bitSet().get((int)smpl.getVal())));
        return res;
    }

    @Override public IgniteBiTuple<Region, Region> performSplit(SparseBitSet bs,
        Region<CategoricalRegionInfo> reg, CategoricalRegionInfo leftData, CategoricalRegionInfo rightData) {
        return performSplitGeneric(bs, reg, leftData, rightData);
    }

    @Override public IgniteBiTuple<Region, Region> performSplitGeneric(
        SparseBitSet bs, Region<CategoricalRegionInfo> reg, RegionInfo leftData, RegionInfo rightData) {
        IgniteBiTuple<SampleInfo[], SampleInfo[]> lrSamples = splitByBitSet(bs.cardinality(), reg.samples().length - bs.cardinality(), reg.samples(), bs);
        BitSet leftCats = calculateCats(lrSamples.get1());
        CategoricalRegionInfo lInfo = new CategoricalRegionInfo(leftData.impurity(), leftCats);

        // TODO: IGNITE-5892 Check how it will work with sparse data.
        BitSet rightCats = calculateCats(lrSamples.get2());
        CategoricalRegionInfo rInfo = new CategoricalRegionInfo(rightData.impurity(), rightCats);
        return new IgniteBiTuple<>(new Region<>(lrSamples.get1(), lInfo), new Region<>(lrSamples.get2(), rInfo));
    }

    /**
     * Powerset iterator. Iterates not over the whole powerset, but on half of it.
     */
    private static class PSI implements Iterator<BitSet> {

        /** Current subset number. */
        private int i = 1; // We are not interested in {emptyset, set} split and therefore start from 1.

        /** Size of set, subsets of which we iterate over. */
        int size;

        /**
         * @param bitCnt Size of set, subsets of which we iterate over.
         */
        public PSI(int bitCnt) {
            this.size = 1 << (bitCnt - 1);
        }

        /** {@inheritDoc} */
        @Override public boolean hasNext() {
            return i < size;
        }

        /** {@inheritDoc} */
        @Override public BitSet next() {
            BitSet res = BitSet.valueOf(new long[] {i});
            i++;
            return res;
        }
    }

    /** */
    private Map<Integer, Integer> mapping(BitSet bs) {
        int bn = 0;
        Map<Integer, Integer> res = new HashMap<>();

        int i = 0;
        while ((bn = bs.nextSetBit(bn)) != -1) {
            res.put(bn, i);
            i++;
            bn++;
        }

        return res;
    }

    /** Get set of categories of given samples */
    private BitSet calculateCats(SampleInfo[] smpls) {
        BitSet res = new BitSet();

        for (SampleInfo smpl : smpls)
            res.set((int)smpl.getVal());

        return res;
    }
}
