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

package org.apache.ignite.client;

import org.apache.ignite.*;
import org.apache.ignite.cache.*;
import org.apache.ignite.configuration.*;
import org.apache.ignite.spi.discovery.tcp.*;
import org.apache.ignite.spi.discovery.tcp.ipfinder.*;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.*;
import org.junit.*;

import java.net.*;
import java.util.*;

import static org.junit.Assert.*;

/** System tests for {@link IgniteClient} */
public class FunctionalTest {
    /** Host. */
    private static final String HOST = "127.0.0.1";

    /** Name of the cache created by default in the cluster. */
    private static final String DEFAULT_CACHE_NAME = "default";

    /**
     * Tested API:
     * <ul>
     * <li>{@link IgniteClient#start(IgniteClientConfiguration)}</li>
     * <li>{@link IgniteClient#getOrCreateCache(String)}</li> with existing cache
     * <li>{@link IgniteCache#put(Object, Object)} with primitive key and object value</li>
     * <li>{@link IgniteCache#get(Object)} primitive key and object value</li>
     * </ul>
     */
    @Test
    public void testPutGet() throws Exception {
        try (Ignite ignored = Ignition.start(getServerConfiguration());
             IgniteClient client = IgniteClient.start(getClientConfiguration())
        ) {
            CacheClient<Integer, Person> cache = client.getOrCreateCache(DEFAULT_CACHE_NAME);

            Integer key = 1;

            Person val = new Person(key, "Joe");

            cache.put(key, val);

            Person cachedVal = cache.get(key);

            assertNotNull(cachedVal);
            assertEquals(key, cachedVal.getId());
            assertEquals(val.getName(), cachedVal.getName());
        }
    }

    /**
     * Tested API:
     * <ul>
     * <li>{@link IgniteClient#getOrCreateCache(String)}</li> with non-existing cache
     * <li>{@link IgniteCache#put(Object, Object)} with object key and primitive value</li>
     * <li>{@link IgniteCache#get(Object)} object key and primitive value</li>
     * </ul>
     */
    @Test
    public void testPutGet2() throws Exception {
        try (Ignite ignored = Ignition.start(getServerConfiguration());
             IgniteClient client = IgniteClient.start(getClientConfiguration())
        ) {
            CacheClient<Person, Integer> cache = client.getOrCreateCache("testPutGet2");

            Integer val = 1;

            Person key = new Person(val, "Joe");

            cache.put(key, val);

            Integer cachedVal = cache.get(key);

            assertEquals(val, cachedVal);
        }
    }

    /**
     * Tested API:
     * <ul>
     * <li>{@link IgniteClient#cache(String)}</li>
     * <li>{@link IgniteClient#getOrCreateCache(CacheClientConfiguration)} with non-existing cache</li>
     * <li></li>
     * <li></li>
     * </ul>
     */
    @Test
    public void testCacheManagement() throws Exception {
        try (Ignite ignored = Ignition.start(getServerConfiguration());
             IgniteClient client = IgniteClient.start(getClientConfiguration())
        ) {
            final String CACHE_NAME = "testCacheManagement";

            int key = 1;
            Person val = new Person(key, Integer.toString(key));

            try (Ignite ignored2 = Ignition.start(getServerConfiguration())) {
                CacheClientConfiguration cacheCfg = new CacheClientConfiguration(CACHE_NAME)
                    .setCacheMode(CacheMode.REPLICATED)
                    .setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC);

                CacheClient<Integer, Person> cache = client.getOrCreateCache(cacheCfg);

                cache.put(key, val);
            }

            CacheClient<Integer, Person> cache = client.cache(CACHE_NAME);

            Person cachedVal = cache.get(key);

            assertEquals(val, cachedVal);
        }
    }

    /** */
    private static IgniteConfiguration getServerConfiguration() {
        TcpDiscoveryIpFinder ipFinder = new TcpDiscoveryVmIpFinder();

        ipFinder.registerAddresses(Collections.singletonList(new InetSocketAddress(HOST, 47500)));

        TcpDiscoverySpi discoverySpi = new TcpDiscoverySpi();

        discoverySpi.setIpFinder(ipFinder);

        IgniteConfiguration igniteCfg = new IgniteConfiguration();

        igniteCfg.setDiscoverySpi(discoverySpi);

        CacheConfiguration<Integer, Person> dfltCacheCfg = new CacheConfiguration<>(DEFAULT_CACHE_NAME);

        igniteCfg.setCacheConfiguration(dfltCacheCfg);

        igniteCfg.setIgniteInstanceName(UUID.randomUUID().toString());

        return igniteCfg;
    }

    /** */
    private static IgniteClientConfiguration getClientConfiguration() {
        return new IgniteClientConfiguration(HOST);
    }
}
