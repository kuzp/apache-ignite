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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.cache.Cache;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.query.FieldsQueryCursor;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.cache.query.annotations.QuerySqlField;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.internal.util.lang.GridAbsPredicate;
import org.apache.ignite.testframework.GridTestUtils;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;

/**
 *
 */
public class GridReplicatedCacheRebalancingWithPdsEnabled extends GridCommonAbstractTest {

    /** Cache. */
    public static final String CACHE = "Cache";

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String igniteInstanceName) throws Exception {
        final IgniteConfiguration cfg = super.getConfiguration(igniteInstanceName);
        DataStorageConfiguration pCfg = new DataStorageConfiguration();

        pCfg.setDefaultDataRegionConfiguration(new DataRegionConfiguration().setPersistenceEnabled(true));
        cfg.setDataStorageConfiguration(pCfg);

        final CacheConfiguration cacheCfg = new CacheConfiguration();
        cacheCfg.setName(CACHE);
        cacheCfg.setCacheMode(CacheMode.REPLICATED);
        cacheCfg.setIndexedTypes(
            Integer.class, Organization.class,
            String.class, Organization.class);
        cfg.setCacheConfiguration(cacheCfg);
        return cfg;
    }

    /** {@inheritDoc} */
    @Override protected void beforeTest() throws Exception {
        super.beforeTest();

        GridTestUtils.deleteDbFiles();
    }

    /**
     * Tests following scenario:
     * 1. create a replicated cache with multiple indexedtypes, with some indexes
     * 2. Start first server node
     * 3. Insert data into cache (1000000 entries)
     * 4. Start second server node
     * At this point, seems all is ok, data is apparently successfully rebalanced
     * making sql queries (count(*))
     * 5. Stop server nodes
     * 6. Restart server nodes
     * 7. Doing sql queries (count(*)) returns less data
     *
     * @throws Exception if failed.
     */
    public void testHugeReplicatedCache() throws Exception {
        final Ignite ignite = startGrid(0);

        ignite.active(true);

        final IgniteCache<Object, Organization> cache = ignite.cache(CACHE);

        final Map<Integer, Organization> map = new HashMap<>();

        final int expCnt = 1_000_000;
        for (int i = 0; i < expCnt; i++)
            map.put(i, new Organization("Org " + i));

        cache.putAll(map);

        final Long cntBeforeRestart = countUsingFieldsQuery(ignite);
        assertEquals(expCnt, ignite.cache(CACHE).size());
        assertEquals(expCnt, cntBeforeRestart.intValue());

        final Ignite ignite1 = startGrid(1);

        GridTestUtils.waitForCondition(new GridAbsPredicate() {
            @Override public boolean apply() {
                return ignite1.cache(CACHE).size() > (expCnt / 2);
            }
        }, 1000);
        Thread.sleep(1000);

        // no call to awaitPartitionMapExchange(); and no call to igniteEx.cache(CACHE).rebalance().get();

        stopAllGrids();

        final IgniteEx ignRestart0 = startGrid(0);// start grid only for locking folder 0
        final IgniteEx ignRestart1 = startGrid(1);

        ignRestart1.active(true);

        final int szIgn0 = ignRestart0.cache(CACHE).size();
        final Long fldQryIgn0 = countUsingFieldsQuery(ignRestart0);
        final long scanQryIgn0 = countUsingScanQuery(ignRestart0);

        final int szIgn1 = ignRestart1.cache(CACHE).size();
        final Long fldQryIgn1 = countUsingFieldsQuery(ignRestart1);
        final long scanQryIgn1 = countUsingScanQuery(ignRestart1);

        if (log.isInfoEnabled())
            log.info("At grid 0: Cache .size(): [" + szIgn0 + "]," +
                " sql fields query count is [" + fldQryIgn0 + "], " +
                " sql scan query count is [" + scanQryIgn0 + "]");

        if (log.isInfoEnabled())
            log.info("At grid 1: Cache .size(): [" + szIgn1 + "]," +
                " sql fields query count is [" + fldQryIgn1 + "]" +
                " sql scan query count is [" + scanQryIgn1 + "]");

        assertEquals(expCnt, fldQryIgn1.intValue());
        assertEquals(expCnt, scanQryIgn1);
    }

    private Long countUsingFieldsQuery(Ignite ignite) {
        final IgniteCache<Object, Organization> cache = ignite.cache(CACHE);
        final SqlFieldsQuery qry = new SqlFieldsQuery("select count(*) from Organization");
        final FieldsQueryCursor<List<?>> qryRes = cache.query(qry);
        return (Long)qryRes.iterator().next().get(0);
    }

    private long countUsingScanQuery(Ignite ignite) {
        final IgniteCache<Object, Organization> cache = ignite.cache(CACHE);
        final ScanQuery<Object, Object> qry = new ScanQuery<>();

        long cnt = 0;
        try (final QueryCursor<Cache.Entry<Object, Object>> cursor = cache.query(qry)) {
            for (Cache.Entry<Object, Object> next : cursor) {
                cnt++;
            }
        }
        return cnt;
    }

    public static class Organization {
        @QuerySqlField
        public String name;

        public Organization(String name) {
            this.name = name;
        }
    }
}
