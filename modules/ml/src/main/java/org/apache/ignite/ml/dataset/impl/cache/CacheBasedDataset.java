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

package org.apache.ignite.ml.dataset.impl.cache;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.ml.dataset.Dataset;
import org.apache.ignite.ml.dataset.PartitionDataBuilder;
import org.apache.ignite.ml.dataset.impl.cache.util.ComputeUtils;
import org.apache.ignite.ml.math.functions.IgniteBiFunction;
import org.apache.ignite.ml.math.functions.IgniteBinaryOperator;
import org.apache.ignite.ml.math.functions.IgniteFunction;
import org.apache.ignite.ml.math.functions.IgniteTriFunction;

/**
 *
 * @param <K>
 * @param <V>
 * @param <C>
 * @param <D>
 */
public class CacheBasedDataset<K, V, C extends Serializable, D extends AutoCloseable>
    implements Dataset<C, D> {
    /** */
    private static final int RETRIES = 100;

    /** */
    private static final int RETRY_INTERVAL = 500;

    /** */
    private final Ignite ignite;

    /** */
    private final IgniteCache<K, V> upstreamCache;

    /** */
    private final IgniteCache<Integer, C> datasetCache;

    /** */
    private final PartitionDataBuilder<K, V, C, D> partDataBuilder;

    /** */
    private final UUID datasetId;

    /**
     *
     * @param ignite
     * @param upstreamCache
     * @param datasetCache
     * @param partDataBuilder
     * @param datasetId
     */
    public CacheBasedDataset(Ignite ignite, IgniteCache<K, V> upstreamCache,
        IgniteCache<Integer, C> datasetCache, PartitionDataBuilder<K, V, C, D> partDataBuilder,
        UUID datasetId) {
        this.ignite = ignite;
        this.upstreamCache = upstreamCache;
        this.datasetCache = datasetCache;
        this.partDataBuilder = partDataBuilder;
        this.datasetId = datasetId;
    }

    /** {@inheritDoc} */
    @Override public <R> R computeWithCtx(IgniteTriFunction<C, D, Integer, R> map, IgniteBinaryOperator<R> reduce, R identity) {
        String upstreamCacheName = upstreamCache.getName();
        String datasetCacheName = datasetCache.getName();

        return computeForAllPartitions(part -> {
            C ctx = ComputeUtils.getContext(Ignition.localIgnite(), datasetCacheName, part);

            D data = ComputeUtils.getData(
                Ignition.localIgnite(),
                upstreamCacheName,
                datasetCacheName,
                datasetId,
                part,
                partDataBuilder
            );

            return map.apply(ctx, data, part);
        }, reduce, identity);
    }

    /** {@inheritDoc} */
    @Override public <R> R compute(IgniteBiFunction<D, Integer, R> map, IgniteBinaryOperator<R> reduce, R identity) {
        String upstreamCacheName = upstreamCache.getName();
        String datasetCacheName = datasetCache.getName();

        return computeForAllPartitions(part -> {
            D data = ComputeUtils.getData(
                Ignition.localIgnite(),
                upstreamCacheName,
                datasetCacheName,
                datasetId,
                part,
                partDataBuilder
            );

            return map.apply(data, part);
        }, reduce, identity);
    }

    /** {@inheritDoc} */
    @Override public void close() {
        datasetCache.destroy();
    }

    /**
     *
     * @param fun
     * @param reduce
     * @param identity
     * @param <R>
     * @return
     */
    private <R> R computeForAllPartitions(IgniteFunction<Integer, R> fun, IgniteBinaryOperator<R> reduce, R identity) {
        Collection<String> cacheNames = Arrays.asList(datasetCache.getName(), upstreamCache.getName());
        Collection<R> results = ComputeUtils.affinityCallWithRetries(ignite, cacheNames, fun, RETRIES, RETRY_INTERVAL);

        R res = identity;
        for (R partRes : results)
            res = reduce.apply(res, partRes);

        return res;
    }
}
