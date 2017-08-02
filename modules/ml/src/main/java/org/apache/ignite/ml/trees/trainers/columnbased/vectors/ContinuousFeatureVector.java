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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.DoubleStream;
import org.apache.ignite.lang.IgniteBiTuple;
import org.apache.ignite.ml.math.functions.IgniteFunction;
import org.apache.ignite.ml.trees.ContinuousRegionInfo;
import org.apache.ignite.ml.trees.ContinuousSplitCalculator;
import org.apache.ignite.ml.trees.RegionInfo;
import org.apache.ignite.ml.trees.trainers.columnbased.Region;

import static org.apache.ignite.ml.trees.trainers.columnbased.vectors.Utils.splitByBitSet;

/**
 * Container of projection of samples on continuous feature.
 *
 * @param <D> Information about regions. Designed to contain information which will make computations of impurity
 * optimal.
 */
public class ContinuousFeatureVector<D extends ContinuousRegionInfo> implements
    FeatureVector<D, ContinuousSplitInfo<D>> {
    /** ContinuousSplitCalculator used for calculating of best split of each region. */
    private final ContinuousSplitCalculator<D> calc;

    /**
     * @param splitCalc Calculator used for calculating splits.
     * @param data Stream containing projection of samples on this feature in format (sample index, value of
     * projection).
     * @param samplesCnt Number of samples.
     * @param labels Labels of samples.
     */
    public ContinuousFeatureVector(ContinuousSplitCalculator<D> splitCalc) {
        this.calc = splitCalc;
//        samples = new SampleInfo[samplesCnt];
//
//
//        int i = 0;
//        Iterator<IgniteBiTuple<Integer, Double>> itr = data.iterator();
//        while (itr.hasNext()) {
//            IgniteBiTuple<Integer, Double> d = itr.next();
//            samples[i] = new SampleInfo(labels[d.get1()], d.get2(), d.get1());
//            i++;
//        }
//

    }

    /** {@inheritDoc} */
    @Override public SplitInfo<D> findBestSplit(Region<D> ri, int regIdx) {
        SplitInfo<D> res = calc.splitRegion(Arrays.stream(ri.samples()), regIdx, ri.data());

        if (res == null)
            return null;

        double lWeight = (double)res.leftData.getSize() / ri.samples().length;
        double rWeight = (double)res.rightData.getSize() / ri.samples().length;

        double infoGain = ri.data().impurity() - lWeight * res.leftData().impurity() - rWeight * res.rightData().impurity();
        res.setInfoGain(infoGain);

        return res;

//        double maxInfoGain = 0.0;
//        SplitInfo<D> res = null;
//
//        // Try to split every possible interval and find the best split.
//        int i = 0;
//        for (D info : regions) {
//            int l = info.left();
//            int r = info.right();
//            int size = (r - l) + 1;
//
//            double curImpurity = info.impurity();
//
//            SplitInfo<D> split = ;
//
//            if (split == null) {
//                i++;
//                continue;
//            }
//

//
//            double infoGain = curImpurity - lWeight * split.leftData().impurity() - rWeight * split.rightData().impurity();
//            if (maxInfoGain < infoGain) {
//                maxInfoGain = infoGain;
//
//                res = split;
//                res.setInfoGain(maxInfoGain);
//            }
//            i++;
//        }

//        return res;
    }

    @Override public Region<D> createInitialRegion(SampleInfo[] samples) {
        Arrays.sort(samples, Comparator.comparingDouble(SampleInfo::getVal));
        return new Region<>(samples, calc.calculateRegionInfo(Arrays.stream(samples).mapToDouble(SampleInfo::getLabel), samples.length));
    }

    /** {@inheritDoc} */
    @Override public SparseBitSet calculateOwnershipBitSet(Region<D> reg, ContinuousSplitInfo<D> s) {
        SparseBitSet res = new SparseBitSet();

        for (int i = 0; i < s.leftData().getSize(); i++)
            res.set(reg.samples()[i].getSampleInd());

        return res;
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

    /** {@inheritDoc} */
    @Override public IgniteBiTuple<Region, Region> performSplit(SparseBitSet bs, Region<D> reg, D leftData, D rightData) {
        int lSize = leftData.getSize();
        int rSize = rightData.getSize();

        IgniteBiTuple<SampleInfo[], SampleInfo[]> lrSamples = splitByBitSet(lSize, rSize, reg.samples(), bs);

        return new IgniteBiTuple<>(new Region<>(lrSamples.get1(), leftData), new Region<>(lrSamples.get2(), rightData));
    }

    /** {@inheritDoc} */
    @Override public IgniteBiTuple<Region, Region> performSplitGeneric(SparseBitSet bs, Region<D> reg, RegionInfo leftData,
        RegionInfo rightData) {
        int lSize = bs.cardinality();
        int rSize = reg.samples().length - lSize;

        IgniteBiTuple<SampleInfo[], SampleInfo[]> lrSamples = splitByBitSet(lSize, rSize, reg.samples(), bs);

        D ld = calc.calculateRegionInfo(Arrays.stream(lrSamples.get1()).mapToDouble(SampleInfo::getLabel), lSize);
        D rd = calc.calculateRegionInfo(Arrays.stream(lrSamples.get2()).mapToDouble(SampleInfo::getLabel), rSize);

        return new IgniteBiTuple<>(new Region<>(lrSamples.get1(), ld), new Region<>(lrSamples.get2(), rd));
    }
}
