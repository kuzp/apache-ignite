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
import org.apache.ignite.lang.IgniteBiTuple;
import org.apache.ignite.ml.trees.ContinuousRegionInfo;
import org.apache.ignite.ml.trees.ContinuousSplitCalculator;
import org.apache.ignite.ml.trees.RegionInfo;
import org.apache.ignite.ml.trees.trainers.columnbased.RegionProjection;

import static org.apache.ignite.ml.trees.trainers.columnbased.vectors.Utils.splitByBitSet;

/**
 * Container of projection of samples on continuous feature.
 *
 * @param <D> Information about regions. Designed to contain information which will make computations of impurity
 * optimal.
 */
public class ContinuousFeatureProcessor<D extends ContinuousRegionInfo> implements
    FeatureProcessor<D, ContinuousSplitInfo<D>> {
    /** ContinuousSplitCalculator used for calculating of best split of each region. */
    private final ContinuousSplitCalculator<D> calc;

    /**
     * @param splitCalc Calculator used for calculating splits.
     * @param data Stream containing projection of samples on this feature in format (sample index, value of
     * projection).
     * @param samplesCnt Number of samples.
     * @param labels Labels of samples.
     */
    public ContinuousFeatureProcessor(ContinuousSplitCalculator<D> splitCalc) {
        this.calc = splitCalc;
    }

    /** {@inheritDoc} */
    @Override public SplitInfo<D> findBestSplit(RegionProjection<D> ri, int regIdx) {
        SplitInfo<D> res = calc.splitRegion(Arrays.stream(ri.samples()), regIdx, ri.data());

        if (res == null)
            return null;

        double lWeight = (double)res.leftData.getSize() / ri.samples().length;
        double rWeight = (double)res.rightData.getSize() / ri.samples().length;

        double infoGain = ri.data().impurity() - lWeight * res.leftData().impurity() - rWeight * res.rightData().impurity();
        res.setInfoGain(infoGain);

        return res;
    }

    @Override public RegionProjection<D> createInitialRegion(SampleInfo[] samples) {
        Arrays.sort(samples, Comparator.comparingDouble(SampleInfo::val));
        return new RegionProjection<>(samples, calc.calculateRegionInfo(Arrays.stream(samples).mapToDouble(SampleInfo::label), samples.length), 0);
    }

    /** {@inheritDoc} */
    @Override public SparseBitSet calculateOwnershipBitSet(RegionProjection<D> reg, ContinuousSplitInfo<D> s) {
        SparseBitSet res = new SparseBitSet();

        for (int i = 0; i < s.leftData().getSize(); i++)
            res.set(reg.samples()[i].sampleInd());

        return res;
    }

    /** {@inheritDoc} */
    @Override public IgniteBiTuple<RegionProjection, RegionProjection> performSplit(SparseBitSet bs, RegionProjection<D> reg, D leftData, D rightData) {
        int lSize = leftData.getSize();
        int rSize = rightData.getSize();
        int depth = reg.depth();

        IgniteBiTuple<SampleInfo[], SampleInfo[]> lrSamples = splitByBitSet(lSize, rSize, reg.samples(), bs);

        return new IgniteBiTuple<>(new RegionProjection<>(lrSamples.get1(), leftData, depth + 1), new RegionProjection<>(lrSamples.get2(), rightData, depth + 1));
    }

    /** {@inheritDoc} */
    @Override public IgniteBiTuple<RegionProjection, RegionProjection> performSplitGeneric(SparseBitSet bs, RegionProjection<D> reg, RegionInfo leftData,
        RegionInfo rightData) {
        int lSize = bs.cardinality();
        int rSize = reg.samples().length - lSize;
        int depth = reg.depth();

        IgniteBiTuple<SampleInfo[], SampleInfo[]> lrSamples = splitByBitSet(lSize, rSize, reg.samples(), bs);

        D ld = calc.calculateRegionInfo(Arrays.stream(lrSamples.get1()).mapToDouble(SampleInfo::label), lSize);
        D rd = calc.calculateRegionInfo(Arrays.stream(lrSamples.get2()).mapToDouble(SampleInfo::label), rSize);

        return new IgniteBiTuple<>(new RegionProjection<>(lrSamples.get1(), ld, depth + 1), new RegionProjection<>(lrSamples.get2(), rd, depth + 1));
    }
}
