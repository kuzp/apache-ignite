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

import java.util.Collection;

/**
 * Ignite thin client.
 * <p>
 * Unlike Ignite client nodes, thin clients do not start Ignite infrastructure and communicate with Ignite cluster
 * over a fast and lightweight protocol.
 * </p>
 */
public interface IgniteClient extends AutoCloseable {
    /**
     * Open thin client connection to the Ignite cluster.
     *
     * @param cfg Thin client configuration.
     * @return Successfully opened thin client connection.
     */
    public static IgniteClient start(IgniteClientConfiguration cfg) throws IgniteClientException {
        return TcpIgniteClient.start(cfg);
    }

    /**
     * Get existing cache or create the cache if it does not exist.
     *
     * @param name Cache name.
     */
    public <K, V> CacheClient<K, V> getOrCreateCache(String name) throws IgniteClientException;

    /**
     * Get existing cache or create the cache if it does not exist.
     *
     * @param cfg Cache configuration.
     */
    public <K, V> CacheClient<K, V> getOrCreateCache(CacheClientConfiguration cfg) throws IgniteClientException;

    /**
     * Get existing cache.
     *
     * @param name Cache name.
     */
    public <K, V> CacheClient<K, V> cache(String name);

    /**
     * @return Collection of names of currently available caches or an empty collection if no caches are available.
     */
    public Collection<String> cacheNames() throws IgniteClientException;

    /**
     * Destroy cache.
     */
    public void destroyCache(String name) throws IgniteClientException;

    /**
     * Create cache.
     *
     * @param name Cache name.
     */
    public <K, V> CacheClient<K, V> createCache(String name) throws IgniteClientException;

    /**
     * Create cache.
     *
     * @param cfg Cache configuration.
     */
    public <K, V> CacheClient<K, V> createCache(CacheClientConfiguration cfg) throws IgniteClientException;
}
