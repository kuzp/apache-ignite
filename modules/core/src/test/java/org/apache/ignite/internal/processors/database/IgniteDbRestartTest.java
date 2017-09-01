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

package org.apache.ignite.internal.processors.database;

import org.apache.ignite.IgniteCache;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.PersistentStoreConfiguration;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.internal.processors.cache.CacheGroupContext;
import org.apache.ignite.internal.processors.cache.IgniteCacheOffheapManager;
import org.apache.ignite.internal.processors.cache.persistence.GridCacheDatabaseSharedManager;
import org.apache.ignite.internal.util.typedef.internal.CU;

/**
 *
 */
public class IgniteDbRestartTest extends IgniteDbAbstractTest {
    /** {@inheritDoc} */
    @Override protected int gridCount() {
        return 1;
    }

    /** {@inheritDoc} */
    @Override protected boolean indexingEnabled() {
        return false;
    }

    @Override protected IgniteConfiguration getConfiguration(String gridName) throws Exception {
        IgniteConfiguration configuration = super.getConfiguration(gridName);

        PersistentStoreConfiguration cfg = new PersistentStoreConfiguration();

        cfg.setCheckpointingFrequency(60000);

        configuration.setPersistentStoreConfiguration(cfg);

        return configuration;
    }

    /**
     *
     */
    public void testRestart() throws Exception {
        IgniteEx ignite = grid(0);

        IgniteCache<Object, Object> cache = ignite.cache(DEFAULT_CACHE_NAME);

        int CNT = 32 * 10;

        GridCacheDatabaseSharedManager dbMgr = (GridCacheDatabaseSharedManager)ignite.context().cache().context().database();

        dbMgr.enableCheckpoints(false).get();

        for (int i = 0; i < CNT; i++)
            cache.put(i, i);

        dbMgr.enableCheckpoints(true).get();
        dbMgr.waitForCheckpoint("");
        dbMgr.enableCheckpoints(false).get();

        assertEquals(CNT, cache.size());

        IgniteEx ex = startGrid(1);

        ((GridCacheDatabaseSharedManager)ex.context().cache().context().database()).enableCheckpoints(false).get();

        awaitPartitionMapExchange();

        stopAllGrids(true);

        ignite = startGrid(0);

        ignite.active(true);

        ignite = startGrid(1);

        awaitPartitionMapExchange();

        cache = ignite.cache(DEFAULT_CACHE_NAME);

        assertEquals(CNT, cache.size());

        CacheGroupContext context = ignite.context().cache().cacheGroup(CU.cacheId("group1"));

        assertNotNull(context);

        Iterable<IgniteCacheOffheapManager.CacheDataStore> stores = context.offheap().cacheDataStores();

        for (IgniteCacheOffheapManager.CacheDataStore store : stores)
            System.out.println(store.partId() + " - " + store.fullSize());
//            assertEquals(10, store.fullSize());

        ignite = grid(0);

        context = ignite.context().cache().cacheGroup(CU.cacheId("group1"));

        stores = context.offheap().cacheDataStores();

        for (IgniteCacheOffheapManager.CacheDataStore store : stores)
            System.out.println(store.partId() + " - " + store.fullSize());

        cache.put(1000, 1000);
    }
}
