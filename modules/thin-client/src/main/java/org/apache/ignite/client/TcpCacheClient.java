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

import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.internal.binary.GridBinaryMarshaller;
import org.apache.ignite.internal.binary.streams.*;
import org.apache.ignite.internal.processors.platform.utils.PlatformUtils;

import java.util.function.*;

/**
 * Implementation of {@link CacheClient} over TCP protocol.
 */
class TcpCacheClient<K, V> implements CacheClient<K, V> {
    /** Ignite Binary Object serializer/deserializer. */
    private static final GridBinaryMarshaller marsh = PlatformUtils.marshaller();

    /** Cache id. */
    private final int cacheId;

    /** Channel. */
    private final ClientChannel ch;

    /** Cache name. */
    private final String name;

    /** Constructor. */
    TcpCacheClient(String name, ClientChannel ch) {
        this.name = name;
        this.cacheId = CacheClient.cacheId(name);
        this.ch = ch;
    }

    /** {@inheritDoc} */
    public V get(K key) throws IgniteClientException {
        if (key == null)
            throw new NullPointerException("key");

        return service(
            ClientOperation.CACHE_GET,
            req -> {
                req.writeInt(cacheId);
                req.writeByte((byte)0); // TODO: support for KEEP_BINARY
                writeObject(req, key);
            },
            res -> {
                Object val = marsh.unmarshal(res);
                return (V)(val instanceof BinaryObject ? ((BinaryObject)val).deserialize() : val);
            }
        );
    }

    /** {@inheritDoc} */
    public void put(K key, V val) throws IgniteClientException {
        if (key == null)
            throw new NullPointerException("key");

        if (val == null)
            throw new NullPointerException("val");

        request(
            ClientOperation.CACHE_PUT,
            req -> {
                req.writeInt(cacheId);
                req.writeByte((byte)0); // presently flags are not supported
                writeObject(req, key);
                writeObject(req, val);
            }
        );
    }

    /** {@inheritDoc} */
    @Override public boolean containsKey(K key) throws IgniteClientException {
        if (key == null)
            throw new NullPointerException("key");

        return service(
            ClientOperation.CACHE_CONTAINS_KEY,
            req -> {
                req.writeInt(cacheId);
                req.writeByte((byte)0); // presently flags are not supported
                writeObject(req, key);
            },
            BinaryInputStream::readBoolean
        );
    }

    /** {@inheritDoc} */
    @Override public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override public CacheClientConfiguration getConfiguration() throws IgniteClientException {
        return service(
            ClientOperation.CACHE_GET_CONFIGURATION,
            req -> {
                req.writeInt(cacheId);
                req.writeByte((byte)0); // presently flags are not supported
            },
            res -> new CacheClientConfigurationSerdes(marsh).read(res)
        );
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

    /** Write Ignite binary object to output stream. */
    private void writeObject(BinaryOutputStream out, Object obj) {
        out.writeByteArray(marsh.marshal(obj));
    }
}
