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
import org.apache.ignite.internal.processors.platform.utils.*;

import java.util.*;
import java.util.function.*;

/**
 * Implementation of {@link IgniteClient} over TCP protocol.
 */
class TcpIgniteClient implements IgniteClient {
    /** Channel. */
    private final ClientChannel ch;

    /** Ignite Binary Object serializer/deserializer. */
    private final GridBinaryMarshaller marsh = PlatformUtils.marshaller();

    /** Config serializer/deserializer. */
    private final CacheClientConfigurationSerdes cfgSerdes = new CacheClientConfigurationSerdes(marsh);

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
        ensureCacheName(name);

        request(ClientOperation.CACHE_GET_OR_CREATE_WITH_NAME, req -> req.writeByteArray(marsh.marshal(name)));

        return new TcpCacheClient<>(name, ch);
    }

    /** {@inheritDoc} */
    @Override public <K, V> CacheClient<K, V> getOrCreateCache(
        CacheClientConfiguration cfg) throws IgniteClientException {
        ensureCacheConfiguration(cfg);

        request(ClientOperation.CACHE_GET_OR_CREATE_WITH_CONFIGURATION, req -> cfgSerdes.write(cfg, req));

        return new TcpCacheClient<>(cfg.getName(), ch);
    }

    /** {@inheritDoc} */
    @Override public <K, V> CacheClient<K, V> cache(String name) {
        ensureCacheName(name);

        return new TcpCacheClient<>(name, ch);
    }

    /** {@inheritDoc} */
    @Override public Collection<String> cacheNames() throws IgniteClientException {
        return service(ClientOperation.CACHE_GET_NAMES, res -> Arrays.asList(BinaryUtils.doReadStringArray(res)));
    }

    /** {@inheritDoc} */
    @Override public void destroyCache(String name) throws IgniteClientException {
        ensureCacheName(name);

        request(ClientOperation.CACHE_DESTROY, req -> req.writeInt(CacheClient.cacheId(name)));
    }

    /** {@inheritDoc} */
    @Override public <K, V> CacheClient<K, V> createCache(String name) throws IgniteClientException {
        ensureCacheName(name);

        request(ClientOperation.CACHE_CREATE_WITH_NAME, req -> req.writeByteArray(marsh.marshal(name)));

        return new TcpCacheClient<>(name, ch);
    }

    /** {@inheritDoc} */
    @Override public <K, V> CacheClient<K, V> createCache(CacheClientConfiguration cfg) throws IgniteClientException {
        ensureCacheConfiguration(cfg);

        request(ClientOperation.CACHE_CREATE_WITH_CONFIGURATION, req -> cfgSerdes.write(cfg, req));

        return new TcpCacheClient<>(cfg.getName(), ch);
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

    /** @throws IllegalArgumentException if the specified cache name is invalid. */
    private static void ensureCacheName(String name) {
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("Cache name must be specified");
    }

    /** @throws IllegalArgumentException if the specified cache name is invalid. */
    private static void ensureCacheConfiguration(CacheClientConfiguration cfg) {
        if (cfg == null)
            throw new IllegalArgumentException("Cache configuration must be specified");

        ensureCacheName(cfg.getName());
    }

    /** Send request and handle response. */
    private <T> T service(
        ClientOperation op,
        Consumer<BinaryOutputStream> payloadWriter,
        Function<BinaryInputStream, T> payloadReader
    ) throws IgniteClientException {
        long id = ch.send(op, payloadWriter);

        return ch.receive(op, id, payloadReader);
    }

    /** Send request and handle response without payload. */
    private void request(ClientOperation op, Consumer<BinaryOutputStream> payloadWriter) throws IgniteClientException {
        service(op, payloadWriter, null);
    }

    /** Send request without payload and handle response. */
    private <T> T service(ClientOperation op, Function<BinaryInputStream, T> payloadReader)
        throws IgniteClientException {
        return service(op, null, payloadReader);
    }
}
