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
import org.apache.ignite.internal.binary.streams.BinaryOutputStream;
import org.apache.ignite.internal.processors.platform.utils.PlatformUtils;

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

    /** Constructor. */
    TcpCacheClient(String name, ClientChannel ch) {
        this.cacheId = cacheId(name);
        this.ch = ch;
    }

    /** {@inheritDoc} */
    public V get(K key) throws IgniteClientException {
        if (key == null)
            throw new NullPointerException("key");

        final ClientOperation OP = ClientOperation.CACHE_GET;

        long id = ch.send(OP, req -> {
            req.writeInt(cacheId);
            req.writeByte((byte)0); // TODO: support for KEEP_BINARY
            writeObject(req, key);
        });

        return ch.receive(OP, id, res -> {
            Object val = marsh.unmarshal(res);
            return (V)(val instanceof BinaryObject ? ((BinaryObject)val).deserialize() : val);
        });
    }

    /** {@inheritDoc} */
    public void put(K key, V val) throws IgniteClientException {
        if (key == null)
            throw new NullPointerException("key");

        if (val == null)
            throw new NullPointerException("val");

        final ClientOperation OP = ClientOperation.CACHE_PUT;

        long id = ch.send(OP, req -> {
            req.writeInt(cacheId);
            req.writeByte((byte)0); // presently flags are not supported
            writeObject(req, key);
            writeObject(req, val);
        });

        ch.receive(OP, id, null); // ignore empty response
    }

    /** Get cache ID by cache name. */
    private static int cacheId(String name) {
        return name.hashCode();
    }

    /** Write Ignite binary object to output stream. */
    private void writeObject(BinaryOutputStream out, Object obj) {
        out.writeByteArray(marsh.marshal(obj));
    }
}
