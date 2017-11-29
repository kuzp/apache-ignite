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

package org.apache.ignite.internal.processors.cache.binary;

import org.apache.ignite.IgniteCache;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.binary.BinaryObjectBuilder;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.internal.util.typedef.F;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.TcpDiscoveryIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;

/** */
public class GridCacheBinaryObjectMetadataVersionTest extends GridCommonAbstractTest {
    /** */
    protected static TcpDiscoveryIpFinder IP_FINDER = new TcpDiscoveryVmIpFinder(true);

    /** */
    private static final String NODE_CLIENT = "client";

    /** */
    private static final int NODE_COUNT = 4;

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String gridName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(gridName);

        ((TcpDiscoverySpi)cfg.getDiscoverySpi()).setIpFinder(IP_FINDER);

        CacheConfiguration ccfg = new CacheConfiguration(DEFAULT_CACHE_NAME);

        ccfg.setCacheMode(CacheMode.PARTITIONED);

        cfg.setCacheConfiguration(ccfg);

        if (F.eq(gridName, NODE_CLIENT))
            cfg.setClientMode(true);

        return cfg;
    }

    /** {@inheritDoc} */
    @Override protected void beforeTest() throws Exception {
        super.beforeTest();

        startGrids(NODE_COUNT);

        startGrid(NODE_CLIENT);
    }

    /** {@inheritDoc} */
    @Override protected void afterTest() throws Exception {
        super.afterTest();

        stopAllGrids();
    }

    /**
     * Change a type few times.
     * Write initial version.
     * Read final version.
     *
     * @throws Exception if failed.
     */
    public void testCacheGetAfterABAChange() throws Exception {
        final String typeName = "TestType";
        final String cacheName = DEFAULT_CACHE_NAME;

        IgniteEx client = grid(NODE_CLIENT);

        client.binary().registerVersionedType(typeName,
            F.asMap("A", Integer.class.getName(), "B", String.class.getName()));

        IgniteCache<Integer, Object> cache = client.cache(cacheName).withKeepBinary();

        BinaryObjectBuilder bld = client.binary().builder(typeName);

        bld.setField("A", 1);
        bld.setField("B", "testString");

        cache.put(1, bld.build());

        client.binary().modifyVersionedType(typeName, null, F.asList("A"));

        BinaryObject result = (BinaryObject)cache.get(1);

        assertFalse(result.hasField("A"));
        assertNull(result.field("A"));
        assertEquals("testString", result.field("B"));

        client.binary().modifyVersionedType(typeName, F.asMap("A", Long.class.getName()), null);

        result = (BinaryObject)cache.get(1);
        assertFalse(result.hasField("A"));
        assertNull(result.field("A"));
        assertEquals("testString", result.field("B"));
    }

    /**
     * Change a type few times.
     * Write after each change.
     * Read final version.
     *
     * @throws Exception if failed.
     */
    public void testCacheGetAfterABAChange2() throws Exception {
        final String typeName = "TestType2";
        final String cacheName = DEFAULT_CACHE_NAME;

        IgniteEx client = grid(NODE_CLIENT);

        client.binary().registerVersionedType(typeName,
            F.asMap("A", Integer.class.getName(), "B", String.class.getName()));

        IgniteCache<Integer, Object> cache = client.cache(cacheName).withKeepBinary();

        BinaryObjectBuilder bld = client.binary().builder(typeName);

        bld.setField("A", 1);
        bld.setField("B", "line1");

        cache.put(1, bld.build());

        client.binary().modifyVersionedType(typeName, null, F.asList("A"));

        bld = client.binary().builder(typeName);

        bld.setField("B", "line2");

        cache.put(2, bld.build());

        client.binary().modifyVersionedType(typeName, F.asMap("A", Long.class.getName()), null);

        bld = client.binary().builder(typeName);

        bld.setField("A", 1L);
        bld.setField("B", "line3");

        cache.put(3, bld.build());

        // 1: v0(A:I, B:S) -> v2(A:L, B:S)
        BinaryObject b1 = (BinaryObject)cache.get(1);
        assertFalse(b1.hasField("A"));
        assertNull(b1.field("A"));
        assertTrue(b1.hasField("B"));
        assertEquals("line1", b1.field("B"));

        // 2: v1(B:S) -> v2(A:L, B:S)
        BinaryObject b2 = (BinaryObject)cache.get(2);
        assertFalse(b2.hasField("A"));
        assertTrue(b2.hasField("B"));
        assertNull(b2.field("A"));
        assertEquals("line2", b2.field("B"));

        // 3: v2(A:L, B:S) -> v2(A:L, B:S)
        BinaryObject b3 = (BinaryObject)cache.get(3);
        assertTrue(b3.hasField("A"));
        assertEquals(1L, b3.field("A"));
        assertTrue(b3.hasField("B"));
        assertEquals("line3", b3.field("B"));
    }

    /**
     * Change type few times.
     * Write from different node every time.
     * Read from every node.
     *
     * @throws Exception if failed.
     */
    public void testMultipleNodesWrite() throws Exception {
        final String typeName = "TestType3";
        final String cacheName = DEFAULT_CACHE_NAME;

        IgniteEx client = grid(NODE_CLIENT);

        client.binary().registerVersionedType(typeName, F.asMap("A", Integer.class.getName()));

        IgniteCache<Integer, Object> cache = client.cache(cacheName).withKeepBinary();

        BinaryObjectBuilder bld = client.binary().builder(typeName);
        bld.setField("A", 1);
        cache.put(1, bld.build());

        for (int node = 0; node < NODE_COUNT; node++) {
            IgniteCache<Integer, Object> cache1 = grid(node).cache(cacheName).withKeepBinary();

            BinaryObject binObj = (BinaryObject)cache1.get(1);

            assertNotNull(binObj);
            assertEquals(1, binObj.field("A"));
        }

        // Every node adds new field and removes previous than build value.
        for (int node = 0; node < NODE_COUNT; node++) {
            String addField = "" + (char)('A' + node + 1);
            String removeField = "" + (char)('A' + node);

            grid(node).binary().modifyVersionedType(typeName, F.asMap(addField, Integer.class.getName()),
                F.asList(removeField));

            bld = grid(node).binary().builder(typeName);

            bld.setField(addField, node);

            grid(node).cache(cacheName).withKeepBinary().put(node + 1, bld.build());
        }

        // Read from every node
        for (int node = 0; node < NODE_COUNT; node++) {
            // Read every key written
            for (int key = 1; key <= NODE_COUNT; key++) {
                BinaryObject binObj = (BinaryObject)grid(node).cache(cacheName).withKeepBinary().get(key);

                assertNotNull(binObj);

                // Check every field: A, B, C ...
                for (int idx = 1; idx <= NODE_COUNT; idx++) {
                    String fldName = "" + (char)('A' + idx);

                    if (key == NODE_COUNT && idx == NODE_COUNT)
                        assertEquals(key - 1, binObj.field(fldName));
                    else
                        assertNull(binObj.field(fldName));
                }
            }
        }
    }

    /**
     *
     * @throws Exception if failed.
     */
    public void testTypeChanges() throws Exception {
        final String typeName = "TestType4";

        IgniteEx client = grid(NODE_CLIENT);

        client.binary().registerVersionedType(typeName,
            F.asMap("A", Integer.class.getName(), "B", String.class.getName()));

        client.binary().modifyVersionedType(typeName, null, F.asList("A"));

        client.binary().modifyVersionedType(typeName, F.asMap("C", Long.class.getName()), null);

        client.binary().modifyVersionedType(typeName, F.asMap("E", Long.class.getName()), null);

        client.binary().modifyVersionedType(typeName, null, F.asList("E"));

        for (int node = 0; node < NODE_COUNT; node++) {
            assertEqualsCollections(F.asList("B", "C"),
                grid(node).binary().type(typeName).fieldNames());
        }
    }
}
