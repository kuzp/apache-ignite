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

package org.apache.ignite.ml.dlc.impl.cache;

import java.util.List;
import java.util.UUID;
import org.apache.ignite.cache.affinity.AffinityFunction;
import org.apache.ignite.cache.affinity.AffinityFunctionContext;
import org.apache.ignite.cluster.ClusterNode;

/**
 * Affinity function wrapper which uses key as a partition index and delegates all other functions to specified
 * delegate.
 */
public class DLCAffinityFunctionWrapper implements AffinityFunction {
    /** */
    private static final long serialVersionUID = -8233787063079973753L;

    /** Delegate which actually performs all methods except {@link #partition(Object)}. */
    private final AffinityFunction delegate;

    /**
     * Constructs a new instance of a DLC affinity function wrapper.
     *
     * @param delegate affinity function which actually performs all methods except {@link #partition(Object)}
     */
    public DLCAffinityFunctionWrapper(AffinityFunction delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override public void reset() {
        delegate.reset();
    }

    /** {@inheritDoc} */
    @Override public int partitions() {
        return delegate.partitions();
    }

    /**
     * Returns key as a partition index.
     *
     * @param key partition index
     * @return partition index
     */
    @Override public int partition(Object key) {
        return (Integer) key;
    }

    /** {@inheritDoc} */
    @Override public List<List<ClusterNode>> assignPartitions(AffinityFunctionContext affCtx) {
        return delegate.assignPartitions(affCtx);
    }

    /** {@inheritDoc} */
    @Override public void removeNode(UUID nodeId) {
        delegate.removeNode(nodeId);
    }
}
