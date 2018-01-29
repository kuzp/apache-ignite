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
 * Ignite thin client.
 * <p>
 * Unlike Ignite client nodes, thin clients do not start Ignite infrastructure and communicate with Ignite cluster
 * over a fast and lightweight TCP binary protocol.
 * </p>
 */
public class IgniteClient implements AutoCloseable {
    /**
     * Private constructor. Use {@link IgniteClient#start(IgniteClientConfiguration)} to create an instance of
     * {@link IgniteClient}.
     */
    private IgniteClient() {
    }

    /**
     * Open thin client connection to the Ignite cluster.
     * @param cfg Thin client configuration.
     * @return Successfully opened thin client connection.
     */
    public static IgniteClient start(IgniteClientConfiguration cfg) {
        return null;
    }

    /** {@inheritDoc} */
    @Override public void close() throws Exception {
    }

    /**
     * Get thin client cache or create the cache if it does not exist.
     * @param name Cache name.
     * @return Instance of {@link CacheClient}.
     */
    public <K, V> CacheClient<K, V> getOrCreateCache(String name) {
        if (name == null)
            throw new IllegalArgumentException("Cache name must not be null.");

        if (name.length() == 0)
            throw new IllegalArgumentException("Cache name must not be empty.");

        return null;
    }
}
