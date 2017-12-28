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
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.affinity.rendezvous.RendezvousAffinityFunction;
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
import org.apache.ignite.internal.util.lang.GridAbsClosure;
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
public class IgnitePdsRebalanceFailoverSelfTest extends GridCommonAbstractTest {
    /** */
    public static final int CACHES_IN_GROUP = 1;

    /** */
    public static final int PARTS_CNT = 32;

    /** */
    public static final String TEST_GRP_NAME = "testGrp";

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

    private CacheConfiguration[] testCaches() {
        CacheConfiguration[] cfgs = new CacheConfiguration[CACHES_IN_GROUP];

        for (int i = 0; i < cfgs.length; i++)
            cfgs[i] = cacheConfiguration("test" + i, TRANSACTIONAL, PARTITIONED, 1, "testGrp");

        return cfgs;
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
        ccfg.setIndexedTypes(Integer.class, Product.class);

        ccfg.setAffinity(new RendezvousAffinityFunction(false, PARTS_CNT));

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

            awaitPartitionMapExchange(true, true, null);

            List<List<ClusterNode>> a1 = crd.context().cache().context().affinity().
                affinity(CU.cacheId(TEST_GRP_NAME)).assignments(new AffinityTopologyVersion(2, 1));

            final int cnt = 1000;

            final CacheConfiguration[] cfgs = testCaches();

            for (int i = 0; i < cfgs.length; i++) {
                CacheConfiguration cfg = cfgs[i];

                for (int k = 0; k < cnt; k++)
                    crd.cache(cfg.getName()).put(k, k);
            }

            // Add more keys to single partition.

            int badKeys = 0;

            for (int i = 0; i < cfgs.length; i++) {
                CacheConfiguration cfg = cfgs[i];

                List<Integer> keys = new ArrayList<>();

                for (int k = cnt; k < cnt * 4; k++) {
                    int p = crd.affinity(cfg.getName()).partition(k);

                    if (p == 15) {
                        crd.cache(cfg.getName()).put(k, k);

                        keys.add(k);
                    }
                }

                for (int j = 0; j < 10; j++) {
                    final Integer kk = keys.get(keys.size() - 1 - j);
                    crd.cache(cfg.getName()).put(kk, new Product(kk, "product" + kk));

                    badKeys++;
                }
            }

            //assertEquals(cfgs.length * 3, badKeys * cfgs.length);

            printTop(crd);

            startGrid(2);

            awaitPartitionMapExchange(true, true, null);

            List<List<ClusterNode>> a2 = crd.context().cache().context().affinity().
                affinity(CU.cacheId(TEST_GRP_NAME)).assignments(new AffinityTopologyVersion(3, 1));

//            for (CacheConfiguration cfg : cfgs)
//                assertEquals(cnt, crd.cache(cfg.getName()).size());

            List<Integer> movedFromCrd = new ArrayList<>(); // New primary.
            List<Integer> movedToCrd = new ArrayList<>(); // New primary.

            for (int p = 0; p < PARTS_CNT; p++) {
                List<ClusterNode> n1 = a1.get(p);
                List<ClusterNode> n2 = a2.get(p);

                if (!n1.equals(n2)) {
                    if (
                        n1.get(0).order() == 1 && n2.get(0).order() == 3 ||
                        n1.get(1).order() == 1 && n2.get(1).order() == 3 ||
                        n1.get(0).order() == 1 && n2.get(0).order() == 2 ||
                        n1.get(1).order() == 1 && n2.get(1).order() == 2)
                        movedFromCrd.add(p);

                    if (n2.get(1).order() == 1)
                        movedToCrd.add(p);
                }
            }

            final ArrayList<Integer> lostByCrd = new ArrayList<>(movedFromCrd);
            lostByCrd.removeAll(movedToCrd);

            //printTop(crd);

            // get type id before grid stop
            final int typeId = crd.binary().typeId(Product.class.getName());

            // Stop all to reset metadata buffer
            crd.active(false);
            stopAllGrids();

            // delete metadata
            final String baseName = "binary_meta/" + getTestIgniteInstanceName(0).replace('.', '_');
            final File metaPath = U.resolveWorkDirectory(U.defaultWorkDirectory(), baseName, false);
            final File f1 = new File(metaPath, typeId + ".bin");
            final File f2 = new File(U.resolveWorkDirectory(U.defaultWorkDirectory(), "binary_meta", false), f1.getName());
            assertTrue(f1.renameTo(f2));

            //assertTrue(f1.delete());

            // Start all no rebalance
            IgniteEx crd1 = startGrid(0);
            startGridsMultiThreaded(1, 2);
            crd1.active(true);
            awaitPartitionMapExchange();

            // trigger rebalance and error
            stopGrid(2);

            doSleep(1_000);

            printTop(crd1);

            // Restart on lesser topology to repair mapping
//            IgniteEx crd2 = restartAllNoRebalance(crd1, 1, new GridAbsClosure() {
//                @Override public void apply() {
//                    assertTrue(f2.renameTo(f1));
//                }
//            });
//
//            printTop(crd2);

            // Add clean node
            assertTrue(f2.renameTo(f1));

            clearData(2);
            IgniteEx newN = startGrid(2);
            awaitPartitionMapExchange();

            printTop(newN);

            int[] badPartsInRebalanceOrder = new int[] {0, 16, 20, 6, 9, 25, 15};

//            2017-12-27 22:47:50,889][INFO ][sys-#90%database.IgnitePdsRebalanceFailoverSelfTest0%][GridDhtPartitionDemander] Done partition: id=0
//                [2017-12-27 22:47:50,891][INFO ][sys-#90%database.IgnitePdsRebalanceFailoverSelfTest0%][GridDhtPartitionDemander] Done partition: id=16
//                [2017-12-27 22:47:50,892][INFO ][sys-#90%database.IgnitePdsRebalanceFailoverSelfTest0%][GridDhtPartitionDemander] Done partition: id=20
//                [2017-12-27 22:47:50,893][INFO ][sys-#90%database.IgnitePdsRebalanceFailoverSelfTest0%][GridDhtPartitionDemander] Done partition: id=6
//                [2017-12-27 22:47:50,895][INFO ][sys-#90%database.IgnitePdsRebalanceFailoverSelfTest0%][GridDhtPartitionDemander] Done partition: id=9
//                [2017-12-27 22:47:50,896][INFO ][sys-#90%database.IgnitePdsRebalanceFailoverSelfTest0%][GridDhtPartitionDemander] Done partition: id=25
//                [2017-12-27 22:47:50,897][INFO ][sys-#90%database.IgnitePdsRebalanceFailoverSelfTest0%][GridDhtPartitionDemander] Done partition: id=15
        }
        finally {
            System.clearProperty(IGNITE_PDS_CHECKPOINT_TEST_SKIP_SYNC);
        }
    }

    public void clearData(int nodeId) throws IgniteCheckedException {
        final String baseName2 = "db/" + getTestIgniteInstanceName(nodeId).replace('.', '_');
        final File metaPath2 = U.resolveWorkDirectory(U.defaultWorkDirectory(), baseName2, false);
        deleteRecursively(metaPath2);
    }

    public IgniteEx restartAllNoRebalance(IgniteEx crd, int nodesToStart, GridAbsClosure clo) throws Exception {
        crd.active(false);
        stopAllGrids();

        clo.apply();

        IgniteEx crd2 = startGrid(0);
        startGridsMultiThreaded(1, nodesToStart);
        crd2.active(true);
        awaitPartitionMapExchange();

        return crd2;
    }

    private void printTop(IgniteEx node) {
        log.info(">>>>>>>>>>>>>> Printing topology: order=" + node.localNode().order());

        GridDhtPartitionTopology top = node.context().cache().context().cache().cacheGroup(CU.cacheId(TEST_GRP_NAME)).topology();

        List<GridDhtLocalPartition> parts = top.localPartitions();

        for (GridDhtLocalPartition part : parts)
            log.info("Part id=" + part.id() + ", state=" + part.state() + ", size=" + part.dataStore().fullSize());
    }

    public static final class Product {
        @QuerySqlField(index = true)
        private int id;

        @QuerySqlField(index = true)
        private String name;

        public Product(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
