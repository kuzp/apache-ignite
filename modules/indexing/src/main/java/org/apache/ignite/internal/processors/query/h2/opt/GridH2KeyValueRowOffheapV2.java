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

package org.apache.ignite.internal.processors.query.h2.opt;

import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.internal.processors.cache.persistence.CacheDataRowAdapter;
import org.apache.ignite.internal.processors.cache.version.GridCacheVersion;
import org.apache.ignite.internal.processors.query.GridQueryTypeDescriptor;
import org.jetbrains.annotations.Nullable;

/**
 * Offheap based table row implementation based on {@link GridQueryTypeDescriptor}.
 */
public class GridH2KeyValueRowOffheapV2 extends GridH2KeyValueRowOnheap {
    /** Lock. */
    private CacheDataRowAdapter.OffheapPageLocker lock;

    /**
     * Constructor.
     *
     * @param desc Row descriptor.
     * @param key Key.
     * @param keyType Key type.
     * @param val Value.
     * @param valType Value type.
     * @param ver Version.
     * @param expirationTime Expiration time.
     * @param lock Offheap lock.
     * @throws IgniteCheckedException If failed.
     */
    public GridH2KeyValueRowOffheapV2(GridH2RowDescriptor desc, Object key, int keyType, @Nullable Object val, int valType,
        GridCacheVersion ver, long expirationTime, CacheDataRowAdapter.OffheapPageLocker lock) throws IgniteCheckedException {
        super(desc, key, keyType, val, valType, ver, expirationTime);

        this.lock = lock;
    }

    /** {@inheritDoc} */
    @Override public void unlock() {
        lock.unlock();

        lock = null;
    }
}