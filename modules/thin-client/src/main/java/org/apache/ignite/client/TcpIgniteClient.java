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

import org.apache.ignite.internal.binary.*;
import org.apache.ignite.internal.binary.streams.*;

/**
 * Implementation of {@link IgniteClient} over TCP protocol.
 */
class TcpIgniteClient implements IgniteClient, AutoCloseable {
    /** Channel. */
    private final ClientChannel ch;

    /**
     * Private constructor. Use {@link IgniteClient#start(IgniteClientConfiguration)} to create an instance of
     * {@link TcpClientChannel}.
     */
    private TcpIgniteClient(IgniteClientConfiguration cfg) throws IgniteClientException {
        this.ch = new TcpClientChannel(cfg);
    }

    /** {@inheritDoc} */
    @Override public void close() throws Exception {
        ch.close();
    }

    /** {@inheritDoc} */
    @Override public <K, V> CacheClient<K, V> getOrCreateCache(String name) throws IgniteClientException {
        if (name == null)
            throw new IllegalArgumentException("Cache name must not be null.");

        if (name.length() == 0)
            throw new IllegalArgumentException("Cache name must not be empty.");

        createCacheIfNotExists(name);

        return new TcpCacheClient<>(name, ch);
    }

    /**
     * Open thin client connection to the Ignite cluster.
     *
     * @param cfg Thin client configuration.
     * @return Successfully opened thin client connection.
     */
    static IgniteClient start(IgniteClientConfiguration cfg) throws IgniteClientException {
        return new TcpIgniteClient(cfg);
    }

    /** */
    private void createCacheIfNotExists(String name) throws IgniteClientException {
        final ClientOperation OP = ClientOperation.CACHE_GET_OR_CREATE_WITH_NAME;

        long id = ch.send(OP, req -> {
            try (BinaryRawWriterEx ser = new BinaryWriterExImpl(null, new BinaryHeapOutputStream(128), null, null)) {
                ser.writeString(name);
                req.writeByteArray(ser.out().array());
            }
        });

        ch.receive(OP, id); // ignore empty response
    }
}
