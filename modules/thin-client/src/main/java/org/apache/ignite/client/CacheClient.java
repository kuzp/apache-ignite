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

/**
 * Thin client cache.
 */
public interface CacheClient<K, V> {
    /** Get cache ID by cache name. */
    public static int cacheId(String name) {
        return name.hashCode();
    }

    /**
     * Gets an entry from the cache.
     *
     * @param key the key whose associated value is to be returned
     * @return the element, or null, if it does not exist.
     * @throws NullPointerException if the key is null.
     */
    public V get(K key) throws IgniteClientException;

    /**
     * Associates the specified value with the specified key in the cache.
     * <p>
     * If the {@link CacheClient} previously contained a mapping for the key, the old
     * value is replaced by the specified value.
     *
     * @param key key with which the specified value is to be associated
     * @param val value to be associated with the specified key.
     * @throws NullPointerException if key is null or if value is null.
     */
    public void put(K key, V val) throws IgniteClientException;

    /**
     * Determines if the {@link CacheClient} contains an entry for the specified key.
     * <p>
     * More formally, returns <tt>true</tt> if and only if this cache contains a
     * mapping for a key <tt>k</tt> such that <tt>key.equals(k)</tt>.
     * (There can be at most one such mapping)
     *
     * @param key key whose presence in this cache is to be tested.
     * @return <tt>true</tt> if this map contains a mapping for the specified key.
     */
    public boolean containsKey(K key) throws IgniteClientException;

    /**
     * @return The name of the cache.
     */
    public String getName();

    /**
     * @return The cache configuration.
     */
    public CacheClientConfiguration getConfiguration() throws IgniteClientException;
}
