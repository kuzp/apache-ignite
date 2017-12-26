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

import java.io.File;
import java.util.List;
import java.util.Random;
import javax.cache.Cache;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.affinity.rendezvous.RendezvousAffinityFunction;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.cache.query.SqlQuery;
import org.apache.ignite.cache.query.annotations.QuerySqlField;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.MemoryConfiguration;
import org.apache.ignite.configuration.MemoryPolicyConfiguration;
import org.apache.ignite.configuration.PersistentStoreConfiguration;
import org.apache.ignite.configuration.WALMode;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.internal.util.typedef.G;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.TcpDiscoveryIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;

import static org.apache.ignite.cache.CacheAtomicityMode.TRANSACTIONAL;
import static org.apache.ignite.cache.CacheMode.PARTITIONED;
import static org.apache.ignite.cache.CacheWriteSynchronizationMode.FULL_SYNC;
import static org.apache.ignite.internal.processors.cache.persistence.GridCacheDatabaseSharedManager.IGNITE_PDS_CHECKPOINT_TEST_SKIP_SYNC;

/**
 * Test rebalance resilience to fails.
 */
public class IgnitePdsRebalanceFailoverTest extends GridCommonAbstractTest {
    /** */
    public static final int GRIDS_CNT = 3;

    /** */
    public static final int CACHES_IN_GROUP = 3;

    /** */
    public static final int DISTINCT_CLASSES = 10;

    /** */
    public static final int PARTS_CNT = 128;

    /** */
    public static final int KEYS_PER_NODE = 200;

    /** */
    private static TcpDiscoveryIpFinder ipFinder = new TcpDiscoveryVmIpFinder(true);

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String igniteInstanceName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(igniteInstanceName);

        cfg.setMemoryConfiguration(new MemoryConfiguration().setDefaultMemoryPolicyName("d").
            setPageSize(1024).setMemoryPolicies(new MemoryPolicyConfiguration().setName("d").
            setInitialSize(50 * 1024 * 1024L).setMaxSize(50 * 1024 * 1024)));

        cfg.setPersistentStoreConfiguration(new PersistentStoreConfiguration().setWalMode(WALMode.LOG_ONLY));

        ((TcpDiscoverySpi) cfg.getDiscoverySpi()).setIpFinder(ipFinder);

        cfg.setCacheConfiguration(testCaches());

        cfg.setConsistentId(igniteInstanceName);

        return cfg;
    }

    /** {@inheritDoc} */
    @Override protected void beforeTest() throws Exception {
        super.beforeTest();

        deleteRecursively(U.resolveWorkDirectory(U.defaultWorkDirectory(), "db", false));
        deleteRecursively(U.resolveWorkDirectory(U.defaultWorkDirectory(), "binary_meta", false));
    }

    /** {@inheritDoc} */
    @Override protected void afterTest() throws Exception {
        stopAllGrids();

        deleteRecursively(U.resolveWorkDirectory(U.defaultWorkDirectory(), "db", false));
        deleteRecursively(U.resolveWorkDirectory(U.defaultWorkDirectory(), "binary_meta", false));

        super.afterTest();
    }

    /**
     * @param name          Name.
     * @param atomicityMode Atomicity mode.
     * @param cacheMode     Cache mode.
     * @param backups       Backups.
     * @param grp           Group.
     * @return Cache configuration.
     */
    private CacheConfiguration cacheConfiguration(String name,
        CacheAtomicityMode atomicityMode,
        CacheMode cacheMode,
        int backups,
        String grp) {
        CacheConfiguration ccfg = new CacheConfiguration(name);

        ccfg.setAtomicityMode(atomicityMode);
        ccfg.setWriteSynchronizationMode(FULL_SYNC);
        ccfg.setCacheMode(cacheMode);
        ccfg.setGroupName(grp);
        ccfg.setRebalanceBatchSize(20);

        ccfg.setAffinity(new RendezvousAffinityFunction(false, PARTS_CNT));

        for (int i = 0; i < DISTINCT_CLASSES; i++)
            ccfg.setIndexedTypes(Integer.class, U.classForName(className(i), null));

        if (cacheMode == PARTITIONED)
            ccfg.setBackups(backups);

        return ccfg;
    }

    private CacheConfiguration[] testCaches() {
        CacheConfiguration[] cfgs = new CacheConfiguration[CACHES_IN_GROUP];

        for (int i = 0; i < cfgs.length; i++)
            cfgs[i] = cacheConfiguration("test" + i, TRANSACTIONAL, PARTITIONED, 1, "testGrp");

        return cfgs;
    }

    /**
     * @throws Exception If failed.
     */
    public void testRebalanceFailover() throws Exception {
        try {
            System.setProperty(IGNITE_PDS_CHECKPOINT_TEST_SKIP_SYNC, "true");

            final CacheConfiguration[] cfgs = testCaches();

            Random r = new Random();

            final IgniteEx node = (IgniteEx)startGridsMultiThreaded(GRIDS_CNT);

            int keysPerNode = KEYS_PER_NODE;

            for (Ignite ignite : G.allGrids()) {
                for (CacheConfiguration cfg : cfgs) {
                    final IgniteCache<Object, Object> cache = ignite.cache(cfg.getName());

                    final List<Integer> keys = primaryKeys(cache, keysPerNode);

                    for (Integer key : keys) {
                        final int part = ignite.affinity(cfg.getName()).partition(key);

                        int clsForPart = part % DISTINCT_CLASSES;

                        String clsName = className(clsForPart);

                        final Base obj = U.newInstance(clsName);

                        assert obj != null;

                        obj.setVal("test" + key);

                        cache.put(key, obj);
                    }
                }
            }

            int cnt0 = countPartitions(node, cfgs[0].getName());

            assertEquals(keysPerNode * GRIDS_CNT, cnt0);

            final int badCls = DISTINCT_CLASSES - 1;

            final int metaId = node.binary().typeId(className(badCls));

            stopAllGrids();

            final File metaPath = U.resolveWorkDirectory(U.defaultWorkDirectory(), "binary_meta/" +
                getTestIgniteInstanceName(0).replace('.', '_'), false);

            assertTrue(new File(metaPath, metaId + ".bin").delete());

            final IgniteEx newNode = startGrid(0);

            startGridsMultiThreaded(1, GRIDS_CNT);

            newNode.active(true);

            awaitPartitionMapExchange();

//            int cnt1 = countPartitions(newNode, cfgs[0].getName());
//
//            assertEquals(cnt0, cnt1);
        }
        finally {
            System.clearProperty(IGNITE_PDS_CHECKPOINT_TEST_SKIP_SYNC);
        }
    }

    /**
     * @param node Node.
     * @param name Name.
     */
    private int countPartitions(IgniteEx node, String name) {
        int cnt = 0;

        for (int p = 0; p < PARTS_CNT; p++) {
            SqlQuery<Object, Object> q = new SqlQuery<>(U.classForName(clsForPart(p), null), "1=1").setPartitions(p);

            final List<Cache.Entry<Object, Object>> entries = node.cache(name).query(q).getAll();

            cnt += entries.size();

//                for (Cache.Entry<Object, Object> entry : entries)
//                    log.info("p=" + p + ", k=" + entry.getKey() + ", v=" + entry.getValue());
        }

        return cnt;
    }

    private static String clsForPart(int p) {
        return className(p % DISTINCT_CLASSES);
    }

    /** */
    private static String className(int clsForPart) {
        return "org.apache.ignite.internal.processors.database.IgnitePdsRebalanceFailoverTest$Derived" +
            clsForPart;
    }

    public static class Base {
        @QuerySqlField(index = true)
        private String val;

        public String getVal() {
            return val;
        }

        public void setVal(String val) {
            this.val = val;
        }

        @Override public String toString() {
            return getClass().getSimpleName() + ": [" +
                "val='" + val + '\'' +
                ']';
        }
    }

    public static class Derived0 extends Base {}
    public static class Derived1 extends Base {}
    public static class Derived2 extends Base {}
    public static class Derived3 extends Base {}
    public static class Derived4 extends Base {}
    public static class Derived5 extends Base {}
    public static class Derived6 extends Base {}
    public static class Derived7 extends Base {}
    public static class Derived8 extends Base {}
    public static class Derived9 extends Base {}
}
