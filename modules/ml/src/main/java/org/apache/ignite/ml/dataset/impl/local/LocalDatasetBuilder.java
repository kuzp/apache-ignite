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

package org.apache.ignite.ml.dataset.impl.local;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.ignite.ml.dataset.DatasetBuilder;
import org.apache.ignite.ml.dataset.PartitionContextBuilder;
import org.apache.ignite.ml.dataset.PartitionDataBuilder;
import org.apache.ignite.ml.dataset.UpstreamEntry;
import org.apache.ignite.ml.math.functions.IgniteFunction;

/**
 * A dataset builder that makes {@link LocalDataset}. Encapsulate logic of building local dataset such as allocation
 * required data structures and initialization of {@code context} part of partitions.
 *
 * @param <K> Type of a key in {@code upstream} data.
 * @param <V> Type of a value in {@code upstream} data.
 * @param <C> Type of a partition {@code context}.
 * @param <D> Type of a partition {@code data}.
 */
public class LocalDatasetBuilder<K, V, C extends Serializable, D extends AutoCloseable>
    implements DatasetBuilder<C, D> {
    /** {@code Map} with upstream data. */
    private final Map<K, V> upstreamMap;

    /** Number of partitions. */
    private final int partitions;

    /** Partition {@code context} builder. */
    private final PartitionContextBuilder<K, V, C> partCtxBuilder;

    /** Partition {@code data} builder. */
    private final PartitionDataBuilder<K, V, C, D> partDataBuilder;

    /**
     * Constructs a new instance of local dataset builder that makes {@link LocalDataset}.
     *
     * @param upstreamMap {@code Map} with upstream data.
     * @param partitions Number of partitions.
     * @param partCtxBuilder Partition {@code context} builder.
     * @param partDataBuilder Partition {@code data} builder.
     */
    public LocalDatasetBuilder(Map<K, V> upstreamMap, int partitions,
        PartitionContextBuilder<K, V, C> partCtxBuilder, PartitionDataBuilder<K, V, C, D> partDataBuilder) {
        this.upstreamMap = upstreamMap;
        this.partitions = partitions;
        this.partCtxBuilder = partCtxBuilder;
        this.partDataBuilder = partDataBuilder;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override public LocalDataset<C, D> build() {
        List<C> ctxList = new ArrayList<>();
        List<D> dataList = new ArrayList<>();

        int partSize = upstreamMap.size() / partitions;

        Iterator<K> firstKeysIter = upstreamMap.keySet().iterator();
        Iterator<K> secondKeysIter = upstreamMap.keySet().iterator();

        for (int part = 0; part < partitions; part++) {
            int cnt = Math.min((part + 1) * partSize, upstreamMap.size()) - part * partSize;

            C ctx = partCtxBuilder.build(
                new IteratorWindow<>(firstKeysIter, k -> new UpstreamEntry<>(k, upstreamMap.get(k)), cnt),
                cnt
            );

            D data = partDataBuilder.build(
                new IteratorWindow<>(secondKeysIter, k -> new UpstreamEntry<>(k, upstreamMap.get(k)), cnt),
                cnt,
                ctx
            );

            ctxList.add(ctx);
            dataList.add(data);
        }

        return new LocalDataset<>(ctxList, dataList);
    }

    /**
     * Utils class that wraps iterator so that it produces only specified number of entries and allows to transform
     * entries from one type to another.
     *
     * @param <K> Initial type of entries.
     * @param <T> Target type of entries.
     */
    private static class IteratorWindow<K, T> implements Iterator<T> {
        /** Delegate iterator. */
        private final Iterator<K> delegate;

        /** Transformer that transforms entries from one type to another. */
        private final IgniteFunction<K, T> map;

        /** Count of entries to produce. */
        private final int cnt;

        /** Number of already produced entries. */
        private int ptr;

        /**
         * Constructs a new instance of iterator window wrapper.
         *
         * @param delegate Delegate iterator.
         * @param map Transformer that transforms entries from one type to another.
         * @param cnt Count of entries to produce.
         */
        IteratorWindow(Iterator<K> delegate, IgniteFunction<K, T> map, int cnt) {
            this.delegate = delegate;
            this.map = map;
            this.cnt = cnt;
        }

        /** {@inheritDoc} */
        @Override public boolean hasNext() {
            return delegate.hasNext() && ptr < cnt;
        }

        /** {@inheritDoc} */
        @Override public T next() {
            ++ptr;

            return map.apply(delegate.next());
        }
    }
}
