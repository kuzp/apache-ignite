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
import java.util.ArrayList;
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
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.MemoryConfiguration;
import org.apache.ignite.configuration.MemoryPolicyConfiguration;
import org.apache.ignite.configuration.PersistentStoreConfiguration;
import org.apache.ignite.configuration.WALMode;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.internal.processors.affinity.AffinityTopologyVersion;
import org.apache.ignite.internal.processors.cache.distributed.dht.GridDhtLocalPartition;
import org.apache.ignite.internal.processors.cache.distributed.dht.GridDhtPartitionTopology;
import org.apache.ignite.internal.util.typedef.G;
import org.apache.ignite.internal.util.typedef.internal.CU;
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
    public static final int DISTINCT_CLASSES = 10;

    /** */
    public static final int PARTS_CNT = 32;

    /** */
    public static final int KEYS_PER_NODE = 1000;

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

        cfg.setCacheConfiguration(cacheConfiguration(DEFAULT_CACHE_NAME, TRANSACTIONAL, PARTITIONED, 1, DEFAULT_CACHE_NAME));

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
        //ccfg.setRebalanceBatchSize(20);

        ccfg.setAffinity(new RendezvousAffinityFunction(false, PARTS_CNT));

        for (int i = 0; i < DISTINCT_CLASSES; i++)
            ccfg.setIndexedTypes(Integer.class, U.classForName(className(i), null));

        if (cacheMode == PARTITIONED)
            ccfg.setBackups(backups);

        return ccfg;
    }

    /**
     * @throws Exception If failed.
     */
    public void testRebalanceFailover() throws Exception {
        try {
            System.setProperty(IGNITE_PDS_CHECKPOINT_TEST_SKIP_SYNC, "true");

            IgniteEx crd = startGrid(0);

            crd.active(true);

            startGrid(1);

            awaitPartitionMapExchange();

            List<List<ClusterNode>> a1 = crd.context().cache().context().affinity().
                affinity(CU.cacheId(DEFAULT_CACHE_NAME)).assignments(new AffinityTopologyVersion(2, 1));

            final int cnt = 1000;

            for (int i = 0; i < cnt; i++) {
                final int part = crd.affinity(DEFAULT_CACHE_NAME).partition(i);

                int clsForPart = part % DISTINCT_CLASSES;

                String clsName = className(clsForPart);

                final Base obj = U.newInstance(clsName);

                assert obj != null;

                obj.setVal("test" + i);

                crd.cache(DEFAULT_CACHE_NAME).put(i, obj);
            }

            startGrid(2);

            awaitPartitionMapExchange();

            List<List<ClusterNode>> a2 = crd.context().cache().context().affinity().
                affinity(CU.cacheId(DEFAULT_CACHE_NAME)).assignments(new AffinityTopologyVersion(3, 1));

            assertEquals(cnt, crd.cache(DEFAULT_CACHE_NAME).size());

            List<Integer> primaryMovedFrom1To3 = new ArrayList<>(); // New primary.

            for (int p = 0; p < PARTS_CNT; p++) {
                List<ClusterNode> n1 = a1.get(p);
                List<ClusterNode> n2 = a2.get(p);
                if (!n1.equals(n2)) {
                    log.info("Diff: p=" + p);

                    if (n1.get(0).order() == 1 && n2.get(0).order() == 3)
                        primaryMovedFrom1To3.add(p);
                }
            }

            Integer lastPartToMove = primaryMovedFrom1To3.get(primaryMovedFrom1To3.size() - 1);

            final int typeId = crd.binary().typeId(clsForPart(lastPartToMove));

            // stop without rebalance.
            crd.active(false);

            stopAllGrids(); // Remove binary meta from memory.

            final String baseName = "binary_meta/" + getTestIgniteInstanceName(0).replace('.', '_');
            final File metaPath = U.resolveWorkDirectory(U.defaultWorkDirectory(), baseName, false);

            assertTrue(new File(metaPath, typeId + ".bin").delete());

            IgniteEx crd1 = startGrid(0);

            startGridsMultiThreaded(1, 2);

            crd1.active(true);

            awaitPartitionMapExchange();

            stopGrid(2); // trigger rebalance to

            doSleep(1_000);

            assertEquals(cnt, crd1.cache(DEFAULT_CACHE_NAME).size());

            GridDhtPartitionTopology top = crd1.context().cache().context().cache().cacheGroup(CU.cacheId(DEFAULT_CACHE_NAME)).topology();

            List<GridDhtLocalPartition> parts = top.localPartitions();

            for (GridDhtLocalPartition part : parts)
                log.info("Part id=" + part.id() + ", state=" + part.state());

            crd1.active(false);

            stopAllGrids();

            IgniteEx crd2 = startGrid(0);

            startGrid(1);

            crd2.active(true);

            doSleep(1_000);

            printTop(crd2);

            assertEquals(cnt, crd2.cache(DEFAULT_CACHE_NAME).size());


        }
        finally {
            System.clearProperty(IGNITE_PDS_CHECKPOINT_TEST_SKIP_SYNC);
        }
    }

    private void printTop(IgniteEx node) {
        GridDhtPartitionTopology top = node.context().cache().context().cache().cacheGroup(CU.cacheId(DEFAULT_CACHE_NAME)).topology();

        List<GridDhtLocalPartition> parts = top.localPartitions();

        for (GridDhtLocalPartition part : parts)
            log.info("Part id=" + part.id() + ", state=" + part.state());
    }

    /**
     * @throws Exception If failed.
     */
    public void testRebalanceFailover2() throws Exception {
        try {
            System.setProperty(IGNITE_PDS_CHECKPOINT_TEST_SKIP_SYNC, "true");

            IgniteEx crd = startGrid(0);

            crd.active(true);

            startGrid(1);

            awaitPartitionMapExchange();

            List<List<ClusterNode>> a1 = crd.context().cache().context().affinity().
                affinity(CU.cacheId(DEFAULT_CACHE_NAME)).assignments(new AffinityTopologyVersion(2, 1));

            final int cnt = 1000;

            for (int i = 0; i < cnt; i++) {
                final int part = crd.affinity(DEFAULT_CACHE_NAME).partition(i);

                int clsForPart = part % DISTINCT_CLASSES;

                String clsName = className(clsForPart);

                final Base obj = U.newInstance(clsName);

                assert obj != null;

                obj.setVal("test" + i);

                crd.cache(DEFAULT_CACHE_NAME).put(i, obj);
            }

            startGrid(2);

            awaitPartitionMapExchange();

            List<List<ClusterNode>> a2 = crd.context().cache().context().affinity().
                affinity(CU.cacheId(DEFAULT_CACHE_NAME)).assignments(new AffinityTopologyVersion(3, 1));

            assertEquals(cnt, crd.cache(DEFAULT_CACHE_NAME).size());

            List<Integer> primaryMovedFrom1To3 = new ArrayList<>(); // New primary.

            for (int p = 0; p < PARTS_CNT; p++) {
                List<ClusterNode> n1 = a1.get(p);
                List<ClusterNode> n2 = a2.get(p);
                if (!n1.equals(n2)) {
                    if (n1.get(0).order() == 1 && n2.get(0).order() == 3)
                        primaryMovedFrom1To3.add(p);
                }
            }

            Integer lastPartToMove = primaryMovedFrom1To3.get(primaryMovedFrom1To3.size() - 1);

            final int typeId = crd.binary().typeId(clsForPart(lastPartToMove));

            // stop without rebalance.
            crd.active(false);

            stopAllGrids(); // Remove binary meta from memory.

            final String baseName = "binary_meta/" + getTestIgniteInstanceName(0).replace('.', '_');
            final File metaPath = U.resolveWorkDirectory(U.defaultWorkDirectory(), baseName, false);

            assertTrue(new File(metaPath, typeId + ".bin").delete());

            IgniteEx crd1 = startGrid(0);

            startGridsMultiThreaded(1, 2);

            crd1.active(true);

            awaitPartitionMapExchange();

            stopGrid(2); // trigger rebalance to

            doSleep(1_000);
            //awaitPartitionMapExchange();

            assertEquals(cnt, crd1.cache(DEFAULT_CACHE_NAME).size());

            printTop(crd1);

            // TODO compare states
            startGrid(2); // Trigger evicts

            awaitPartitionMapExchange();

            printTop(crd1);

            assertEquals(cnt, crd1.cache(DEFAULT_CACHE_NAME).size());

            // Restart again
            crd1.active(false);

            stopAllGrids();

            IgniteEx crd2 = startGrid(0);

            startGrid(1);

            startGrid(2);

            crd2.active(true);

            awaitPartitionMapExchange();

            printTop(crd2);
        }
        finally {
            System.clearProperty(IGNITE_PDS_CHECKPOINT_TEST_SKIP_SYNC);
        }
    }

    private boolean move(File metaPath, int metaId, String ext1, String ext2) {
        final File file10 = new File(metaPath, metaId + ext1);
        final File file11 = new File(metaPath, metaId + ext2);

        return file10.renameTo(file11);
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
