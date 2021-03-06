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

package org.apache.ignite;

import org.apache.ignite.cache.*;
import org.apache.ignite.cache.affinity.*;
import org.apache.ignite.cache.affinity.fair.*;
import org.apache.ignite.cache.affinity.rendezvous.*;
import org.apache.ignite.cluster.*;
import org.apache.ignite.configuration.*;
import org.apache.ignite.internal.processors.cache.*;

import java.util.*;

import static org.apache.ignite.cache.CacheAtomicityMode.*;
import static org.apache.ignite.cache.CacheDistributionMode.*;
import static org.apache.ignite.cache.CacheMode.*;

/**
 * Tests for {@link org.apache.ignite.internal.processors.affinity.GridAffinityProcessor.CacheAffinityProxy}.
 */
public class IgniteCacheAffinitySelfTest extends IgniteCacheAbstractTest {
    /** Initial grid count. */
    private int GRID_COUNT = 3;

    /** Cache name */
    private final String CACHE1 = "Fair";

    /** Cache name */
    private final String CACHE2 = "Rendezvous";

    /** {@inheritDoc} */
    @Override protected int gridCount() {
        return GRID_COUNT;
    }

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String gridName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(gridName);

        CacheConfiguration cache0 = cacheConfiguration(null);

        CacheConfiguration cache1 = cacheConfiguration(null);
        cache1.setName(CACHE1);
        cache1.setAffinity(new CachePartitionFairAffinity());

        CacheConfiguration cache2 = cacheConfiguration(null);
        cache2.setName(CACHE2);
        cache2.setAffinity(new CacheRendezvousAffinityFunction());

        if (gridName.contains("0"))
            cfg.setCacheConfiguration(cache0);
        else
            cfg.setCacheConfiguration(cache0, cache1, cache2);

        return cfg;
    }

    /** {@inheritDoc} */
    @Override protected CacheMode cacheMode() {
        return PARTITIONED;
    }

    /** {@inheritDoc} */
    @Override protected CacheAtomicityMode atomicityMode() {
        return TRANSACTIONAL;
    }

    /** {@inheritDoc} */
    @Override protected CacheDistributionMode distributionMode() {
        return NEAR_PARTITIONED;
    }

    /**
     * Throws Exception if failed.
     */
    public void testAffinity() throws Exception {
        checkAffinity();

        stopGrid(gridCount() - 1);

        startGrid(gridCount() - 1);
        startGrid(gridCount());

        GRID_COUNT += 1;

        checkAffinity();
    }

    /**
     * Check CacheAffinityProxy methods.
     */
    private void checkAffinity() {
        checkAffinity(grid(0).affinity(null), cache(1, null).affinity());
        checkAffinity(grid(0).affinity(CACHE1), cache(1, CACHE1).affinity());
        checkAffinity(grid(0).affinity(CACHE1), cache(1, CACHE1).affinity());
        checkAffinity(grid(0).affinity(CACHE2), cache(1, CACHE2).affinity());
    }

    /**
     * @param testAff Cache affinity to test.
     * @param aff Cache affinity.
     */
    private void checkAffinity(CacheAffinity testAff, CacheAffinity aff) {
        checkAffinityKey(testAff, aff);
        checkPartitions(testAff, aff);
        checkIsBackupOrPrimary(testAff, aff);
        checkMapKeyToNode(testAff, aff);
        checkMapKeysToNodes(testAff, aff);
        checkMapPartitionToNode(testAff, aff);
        checkMapPartitionsToNodes(testAff, aff);
    }

    /**
     * Check affinityKey method.
     */
    private void checkAffinityKey(CacheAffinity testAff,CacheAffinity aff) {
        for (int i = 0; i < 10000; i++)
            assertEquals(testAff.affinityKey(i), aff.affinityKey(i));
    }

    /**
     * Check allPartitions, backupPartitions and primaryPartitions methods.
     */
    private void checkPartitions(CacheAffinity testAff, CacheAffinity aff) {
        for (ClusterNode n : nodes()) {
            checkEqualIntArray(testAff.allPartitions(n), aff.allPartitions(n));

            checkEqualIntArray(testAff.backupPartitions(n), aff.backupPartitions(n));

            checkEqualIntArray(testAff.primaryPartitions(n), aff.primaryPartitions(n));
        }
    }

    /**
     * Check isBackup, isPrimary and isPrimaryOrBackup methods.
     */
    private void checkIsBackupOrPrimary(CacheAffinity testAff, CacheAffinity aff) {
        for (int i = 0; i < 10000; i++)
            for (ClusterNode n : nodes()) {
                assertEquals(testAff.isBackup(n, i), aff.isBackup(n, i));

                assertEquals(testAff.isPrimary(n, i), aff.isPrimary(n, i));

                assertEquals(testAff.isPrimaryOrBackup(n, i), aff.isPrimaryOrBackup(n, i));
            }
    }

    /**
     * Check mapKeyToNode, mapKeyToPrimaryAndBackups methods.
     */
    private void checkMapKeyToNode(CacheAffinity testAff, CacheAffinity aff) {
        for (int i = 0; i < 10000; i++) {
            assertEquals(testAff.mapKeyToNode(i).id(), aff.mapKeyToNode(i).id());

            checkEqualCollection(testAff.mapKeyToPrimaryAndBackups(i), aff.mapKeyToPrimaryAndBackups(i));
        }
    }

    /**
     * Check mapPartitionToPrimaryAndBackups and mapPartitionToNode methods.
     */
    private void checkMapPartitionToNode(CacheAffinity testAff, CacheAffinity aff) {
        assertEquals(aff.partitions(), testAff.partitions());

        for (int part = 0; part < aff.partitions(); ++part) {
            assertEquals(testAff.mapPartitionToNode(part).id(), aff.mapPartitionToNode(part).id());

            checkEqualCollection(testAff.mapPartitionToPrimaryAndBackups(part),
                aff.mapPartitionToPrimaryAndBackups(part));
        }
    }

    /**
     * Check mapKeysToNodes methods.
     */
    private void checkMapKeysToNodes(CacheAffinity testAff, CacheAffinity aff) {
        List<Integer> keys = new ArrayList<>(10000);

        for (int i = 0; i < 10000; ++i)
            keys.add(i);

        checkEqualMaps(testAff.mapKeysToNodes(keys), aff.mapKeysToNodes(keys));
    }

    /**
     * Check mapPartitionsToNodes methods.
     */
    private void checkMapPartitionsToNodes(CacheAffinity testAff, CacheAffinity aff) {
        List<Integer> parts = new ArrayList<>(aff.partitions());

        for (int i = 0; i < aff.partitions(); ++i)
            parts.add(i);

        checkEqualPartitionMaps(testAff.mapPartitionsToNodes(parts), aff.mapPartitionsToNodes(parts));
    }

    /**
     * Check equal arrays.
     */
    private static void checkEqualIntArray(int[] arr1, int[] arr2) {
        assertEquals(arr1.length, arr2.length);

        Collection<Integer> col1 = new HashSet<>();

        for (int anArr1 : arr1)
            col1.add(anArr1);

        for (int anArr2 : arr2) {
            assertTrue(col1.contains(anArr2));

            col1.remove(anArr2);
        }

        assertEquals(0, col1.size());
    }

    /**
     * Check equal collections.
     */
    private static void checkEqualCollection(Collection<ClusterNode> col1, Collection<ClusterNode> col2) {
        assertEquals(col1.size(), col2.size());

        for (ClusterNode node : col1)
            assertTrue(col2.contains(node));
    }

    /**
     * Check equal maps.
     *
     * @param map1 Map1.
     * @param map2 Map2.
     */
    private static void checkEqualMaps(Map<ClusterNode, Collection> map1, Map<ClusterNode, Collection> map2) {
        assertEquals(map1.size(), map2.size());

        for (ClusterNode node : map1.keySet()) {
            assertTrue(map2.containsKey(node));

            assertEquals(map1.get(node).size(), map2.get(node).size());
        }
    }

    /**
     * Check equal maps.
     *
     * @param map1 Map1.
     * @param map2 Map2.
     */
    private static void checkEqualPartitionMaps(Map<Integer, ClusterNode> map1, Map<Integer, ClusterNode> map2) {
        assertEquals(map1.size(), map2.size());

        for (Integer i : map1.keySet()) {
            assertTrue(map2.containsKey(i));

            assertEquals(map1.get(i), map2.get(i));
        }
    }

    /**
     * @return Cluster nodes.
     */
    private Collection<ClusterNode> nodes() {
        return grid(0).cluster().nodes();
    }
}
