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

import java.util.List;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.binary.BinaryObjectBuilder;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.internal.processors.query.GridQueryProcessor;
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
     *
     * @throws Exception if failed.
     */
    public void testCacheGetAfterABAChange() throws Exception {
        final String typeName = "TestType";
        final String cacheName = DEFAULT_CACHE_NAME;

        IgniteEx client = grid(NODE_CLIENT);

        client.binary().registerChangeControlledType(typeName,
            F.asMap("A", Integer.class.getName(), "B", String.class.getName()));

        IgniteCache<Integer, Object> cache = client.cache(cacheName).withKeepBinary();

        BinaryObjectBuilder bld = client.binary().builder(typeName, cacheName);

        bld.setField("A", 1);
        bld.setField("B", "testString");

        cache.put(1, bld.build());

        client.binary().removeField(typeName, cacheName, "A");

        BinaryObject result = (BinaryObject)cache.get(1);

        assertFalse(result.hasField("A"));
        assertNull(result.field("A"));
        assertEquals("testString", result.field("B"));

        client.binary().addField(typeName, cacheName, "A", Long.class.getName());

        result = (BinaryObject)cache.get(1);
        assertFalse(result.hasField("A"));
        assertNull(result.field("A"));
        assertEquals("testString", result.field("B"));

        bld = client.binary().builder(typeName, cacheName);
        bld.setField("A", 2L);
        bld.setField("B", "testLine");
    }

    /**
     *
     * @throws Exception if failed.
     */
    public void testTypeChanges() throws Exception {
        final String typeName = "TestType2";
        final String cacheName = DEFAULT_CACHE_NAME;

        IgniteEx client = grid(NODE_CLIENT);

        client.binary().registerChangeControlledType(typeName,
            F.asMap("A", Integer.class.getName(), "B", String.class.getName()));

        client.binary().removeField(typeName, cacheName, "A");

        client.binary().addField(typeName, cacheName, "C", Long.class.getName());

        client.binary().addField(typeName, cacheName, "E", Long.class.getName());

        client.binary().removeField(typeName, cacheName, "E");

        for (int node = 0; node < NODE_COUNT; node++) {
            assertEqualsCollections(F.asList("B", "C"),
                grid(node).binary().type(typeName, cacheName).fieldNames());
        }
    }

    /**
     *
     * @throws Exception if failed.
     */
    public void testTwoCachesTypeChanges() throws Exception {
        final String cacheNameA = "cacheA";
        final String cacheNameB = "cacheB";

        final String typeName = "TestType2C";

        IgniteEx client = grid(NODE_CLIENT);

        client.binary().registerChangeControlledType(typeName,
            F.asMap("A", Integer.class.getName(), "B", String.class.getName()));

        //Cache A: (A, B) => (A, C)
        client.binary().removeField(typeName, cacheNameA, "B");
        client.binary().addField(typeName, cacheNameA, "C", String.class.getName());

        //Cache B: (A, B) => (B, C)
        client.binary().removeField(typeName, cacheNameB, "A");
        client.binary().addField(typeName, cacheNameB, "C", Integer.class.getName());

        for (int node = 0; node < NODE_COUNT; node++) {
            assertEqualsCollections(F.asList("A", "C"), grid(node).binary().type(typeName, cacheNameA).fieldNames());
            assertEqualsCollections(F.asList("B", "C"), grid(node).binary().type(typeName, cacheNameB).fieldNames());
        }
    }

    /**
     *
     * @throws Exception if failed.
     */
    public void testSqlQuery() throws Exception {
        fail("IGNITE-5949");

        IgniteEx client = grid(NODE_CLIENT);

        GridQueryProcessor qryProc = client.context().query();

        qryProc.querySqlFieldsNoCache(
            new SqlFieldsQuery("CREATE TABLE test(a INT PRIMARY KEY, b INT, c CHAR)"), true).getAll();

        qryProc.querySqlFieldsNoCache(
            new SqlFieldsQuery("INSERT INTO test VALUES (1, 1, \"one\"), (2, 2, \"two\")"), true).getAll();

        List<List<?>> res = qryProc.querySqlFieldsNoCache(new SqlFieldsQuery("SELECT * FROM test"), true).getAll();
        assertEquals(2, res.size());
        assertEqualsCollections(F.asList(1, 1, "one"), res.get(0));
        assertEqualsCollections(F.asList(2, 2, "two"), res.get(1));

        qryProc.querySqlFieldsNoCache(
            new SqlFieldsQuery("ALTER TABLE test DROP COLUMN b"), true).getAll();

        qryProc.querySqlFieldsNoCache(
            new SqlFieldsQuery("INSERT INTO test VALUES (3, \"three\"), (4, \"four\")"), true).getAll();

        res = qryProc.querySqlFieldsNoCache(new SqlFieldsQuery("SELECT * FROM test"), true).getAll();
        assertEquals(4, res.size());
        assertEqualsCollections(F.asList(1, "one"), res.get(0));
        assertEqualsCollections(F.asList(2, "two"), res.get(1));
        assertEqualsCollections(F.asList(3, "three"), res.get(2));
        assertEqualsCollections(F.asList(4, "four"), res.get(3));

        qryProc.querySqlFieldsNoCache(
            new SqlFieldsQuery("ALTER TABLE test ADD COLUMN b CHAR"), true).getAll();

        qryProc.querySqlFieldsNoCache(
            new SqlFieldsQuery("INSERT INTO test VALUES (5, \"five\", \"five\"), (6, \"six\", \"six\")"),
            true).getAll();

        res = qryProc.querySqlFieldsNoCache(new SqlFieldsQuery("SELECT * FROM test"), true).getAll();
        assertEquals(6, res.size());
        assertEqualsCollections(F.asList(1, "one", null), res.get(0));
        assertEqualsCollections(F.asList(2, "two", null), res.get(1));
        assertEqualsCollections(F.asList(3, "three", null), res.get(2));
        assertEqualsCollections(F.asList(4, "four", null), res.get(3));
        assertEqualsCollections(F.asList(5, "five", "five"), res.get(4));
        assertEqualsCollections(F.asList(6, "six", "six"), res.get(5));
    }

}
