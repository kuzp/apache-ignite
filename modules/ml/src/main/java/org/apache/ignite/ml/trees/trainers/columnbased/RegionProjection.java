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

package org.apache.ignite.ml.trees.trainers.columnbased;

import org.apache.ignite.ml.trees.trainers.columnbased.vectors.SampleInfo;

/**
 * Projection of region on given feature.
 * @param <D> Data of region.
 */
public class RegionProjection<D> {
    /** Samples projections. */
    protected SampleInfo[] samples;

    /** Region data */
    protected D data;

    /** Depth of this region. */
    protected int depth;

    /**
     * @param samples Samples projections.
     * @param data Region data.
     * @param depth Depth of this region.
     */
    public RegionProjection(SampleInfo[] samples, D data, int depth) {
        this.samples = samples;
        this.data = data;
        this.depth = depth;
    }

    /**
     * Get samples projections.
     * @return Samples projections.
     */
    public SampleInfo[] samples() {
        return samples;
    }

    /**
     * Get region data.
     * @return Region data.
     */
    public D data() {
        return data;
    }

    /**
     * Get region depth.
     * @return Region depth.
     */
    public int depth() {
        return depth;
    }
}
