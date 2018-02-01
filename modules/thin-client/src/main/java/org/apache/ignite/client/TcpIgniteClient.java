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

import java.io.*;
import java.net.*;

/**
 * Implementation of {@link IgniteClient} over TCP protocol.
 */
class TcpIgniteClient implements IgniteClient, AutoCloseable {
    /** Socket. */
    private final Socket sock;

    /** Output stream. */
    private final OutputStream out;

    /** Input stream. */
    private final InputStream in;

    /** Writer. */
    private final BinaryWriter writer;

    /** Reader. */
    private final BinaryReader reader;

    /** Version. */
    private final ProtocolVersion ver = new ProtocolVersion((short)1, (short)0, (short)0);

    /**
     * Private constructor. Use {@link IgniteClient#start(IgniteClientConfiguration)} to create an instance of
     * {@link TcpIgniteClient}.
     */
    private TcpIgniteClient(IgniteClientConfiguration cfg) throws IgniteClientException {
        validateConfiguration(cfg);

        try {
            sock = createSocket(cfg);

            out = sock.getOutputStream();
            in = sock.getInputStream();
        }
        catch (IOException e) {
            throw new IgniteClientException(
                String.format(
                    "Ignite thin TCP client failed to connect to cluster at %s:%s",
                    cfg.getHost(),
                    cfg.getPort()
                ),
                e
            );
        }

        writer = new BinaryStreamWriter(out);
        reader = new BinaryStreamReader(in);

        handshake();
    }

    /** {@inheritDoc} */
    @Override public void close() throws Exception {
        in.close();
        out.close();
        sock.close();
    }

    /** {@inheritDoc} */
    @Override public <K, V> CacheClient<K, V> getOrCreateCache(String name) {
        if (name == null)
            throw new IllegalArgumentException("Cache name must not be null.");

        if (name.length() == 0)
            throw new IllegalArgumentException("Cache name must not be empty.");

        return null;
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

    /** Validate {@link IgniteClientConfiguration}. */
    private static void validateConfiguration(IgniteClientConfiguration cfg) {
        String error = null;

        if (cfg == null)
            error = "Ignite client configuration must be specified.";
        else if (cfg.getHost() == null && cfg.getHost().length() == 0)
            error = "A cluster node must be specified in the Ignite client configuration.";
        else if (cfg.getPort() <= 0)
            error = "Ignite client port must be a positive number";

        if (error != null)
            throw new IllegalArgumentException(error);
    }

    /** Create socket. */
    private static Socket createSocket(IgniteClientConfiguration cfg) throws IOException {
        Socket sock = new Socket(cfg.getHost(), cfg.getPort());

        sock.setTcpNoDelay(cfg.isTcpNoDelay());

        if (cfg.getTimeout() > 0)
            sock.setSoTimeout(cfg.getTimeout());

        if (cfg.getSendBufferSize() > 0)
            sock.setSendBufferSize(cfg.getSendBufferSize());

        if (cfg.getReceiveBufferSize() > 0)
            sock.setReceiveBufferSize(cfg.getReceiveBufferSize());

        return sock;
    }

    /** Client handshake. */
    private void handshake() throws IgniteClientException {
        // Handshake request

        writer.writeInt(1 + 2 + 2 + 2 + 1); // message size
        writer.writeByte((byte)1); // handshake code, always 1
        writer.writeShort(ver.major());
        writer.writeShort(ver.minor());
        writer.writeShort(ver.patch());
        writer.writeByte((byte)2); // client code, always 2
        writer.flush();

        // Handshake response
        int resSize = reader.readInt();

        if (reader.readBoolean()) // Success flag
            reader.readBytes(resSize - 1); // skip the rest of response
        else {
            ProtocolVersion srvVer = new ProtocolVersion(reader.readShort(), reader.readShort(), reader.readShort());
            String err = reader.readIgniteBinary();

            throw new IgniteClientException(
                String.format("Client handshake failed: %s. Client version: %s. Server version: %s", err, ver, srvVer)
            );
        }
    }
}
