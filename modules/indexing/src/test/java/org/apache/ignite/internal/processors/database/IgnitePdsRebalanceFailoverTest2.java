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
import javax.cache.Cache;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.affinity.rendezvous.RendezvousAffinityFunction;
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
import org.apache.ignite.internal.processors.affinity.GridAffinityAssignmentCache;
import org.apache.ignite.internal.processors.cache.distributed.dht.GridDhtLocalPartition;
import org.apache.ignite.internal.processors.cache.distributed.dht.GridDhtPartitionTopology;
import org.apache.ignite.internal.util.typedef.F;
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
public class IgnitePdsRebalanceFailoverTest2 extends GridCommonAbstractTest {
    /** */
    public static final int DISTINCT_CLASSES = 3;

    /** */
    public static final int CACHES_IN_GROUP = 1;

    /** */
    public static final int PARTS_CNT = 32;

    /** */
    public static final int KEYS_PER_NODE = 1000;

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
    public void testRebalanceFailover3() throws Exception {
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

                for (int k = 0; k < cnt; k++) {
                    String clsName = className(i);

                    final Base obj = U.newInstance(clsName);

                    assert obj != null;

                    obj.setVal("test" + k);

                    crd.cache(cfg.getName()).put(k, obj);
                }
            }

            startGrid(2);

            awaitPartitionMapExchange(true, true, null);

            List<List<ClusterNode>> a2 = crd.context().cache().context().affinity().
                affinity(CU.cacheId(TEST_GRP_NAME)).assignments(new AffinityTopologyVersion(3, 1));

            for (CacheConfiguration cfg : cfgs)
                assertEquals(cnt, crd.cache(cfg.getName()).size());

            List<Integer> movedFrom1To3 = new ArrayList<>(); // New primary.

            for (int p = 0; p < PARTS_CNT; p++) {
                List<ClusterNode> n1 = a1.get(p);
                List<ClusterNode> n2 = a2.get(p);

                if (!n1.equals(n2)) {
                    if ((n1.get(0).order() == 1 && n2.get(0).order()==3) ||
                        (n1.get(1).order() == 1 && n2.get(1).order()==3)
                        )
                        movedFrom1To3.add(p);
                }
            }

            Integer lastPartToMove = movedFrom1To3.get(movedFrom1To3.size() - 1);

            final int typeId = crd.binary().typeId(clsForPart(lastPartToMove));

            stopGrid(2);

            awaitPartitionMapExchange(true, true, null);

            System.out.println();
        }
        finally {
            System.clearProperty(IGNITE_PDS_CHECKPOINT_TEST_SKIP_SYNC);
        }
    }

    private static String clsForPart(int p) {
        return className(p % DISTINCT_CLASSES);
    }

    /** */
    private static String className(int clsForPart) {
        return "org.apache.ignite.internal.processors.database.IgnitePdsRebalanceFailoverTest2$Derived" +
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
}
