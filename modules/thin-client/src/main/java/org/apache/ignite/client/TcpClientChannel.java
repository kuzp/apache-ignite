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

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

/**
 * Implements {@link ClientChannel} over TCP.
 */
class TcpClientChannel implements ClientChannel {
    /** Channel. */
    private final Socket sock;

    /** Output stream. */
    private final OutputStream out;

    /** Input stream. */
    private final InputStream in;

    /** Version. */
    private final ProtocolVersion ver = new ProtocolVersion((short)1, (short)0, (short)0);

    private final AtomicLong reqId = new AtomicLong(1);

    /** Constructor. */
    TcpClientChannel(IgniteClientConfiguration cfg) throws IgniteClientException {
        validateConfiguration(cfg);

        try {
            sock = createSocket(cfg);

            out = sock.getOutputStream();
            in = sock.getInputStream();
        }
        catch (IOException e) {
            throw new IgniteClientException(
                String.format(
                    "TCP client failed to connect to cluster at %s:%s",
                    cfg.getHost(),
                    cfg.getPort()
                ),
                e
            );
        }

        handshake();
    }

    /** {@inheritDoc} */
    @Override public void close() throws Exception {
        in.close();
        out.close();
        sock.close();
    }

    /** {@inheritDoc} */
    @Override public void send(ClientOperation op, Consumer<BinaryOutputStream> payloadWriter)
        throws IgniteClientException {
        try (BinaryOutputStream req = new BinaryOffheapOutputStream(1024)) {
            req.writeInt(0); // reserve an integer for the request size

            req.writeByte(op.code());

            if (op != ClientOperation.HANDSHAKE)
                req.writeLong(reqId.getAndIncrement());

            payloadWriter.accept(req);

            req.writeInt(0, req.position() - 4); // actual size

            out.write(req.array(), 0, req.position());
        }
        catch (IOException e) {
            throw new IgniteClientException("TCP Ignite client failed to send data", e);
        }
    }

    /** {@inheritDoc} */
    @Override public byte[] receive() throws IgniteClientException {
        byte[] sizeBytes = new byte[4];
        int sizeBytesNum;

        try {
            sizeBytesNum = in.read(sizeBytes, 0, 4);
        }
        catch (IOException e) {
            throw new IgniteClientException("TCP Ignite client failed to receive response size", e);
        }

        if (sizeBytesNum < 0)
            throw new IgniteClientException("TCP Ignite client unexpectedly received no response size");

        if (sizeBytesNum < 4)
            throw new IgniteClientException(String.format(
                "TCP Ignite client received only %s bytes of response size but 4 bytes were expected",
                sizeBytesNum
            ));

        int resSize = new BinaryHeapInputStream(sizeBytes).readInt();

        if (resSize < 0)
            throw new IgniteClientException(
                String.format("TCP Ignite client received invalid response size: %s", resSize)
            ); 
     
        if (resSize == 0)
            return new byte[0];

        byte[] payloadBytes = new byte[resSize];
        int payloadBytesNum;

        try {
            payloadBytesNum = in.read(payloadBytes, 0, resSize);
        }
        catch (IOException e) {
            throw new IgniteClientException("TCP Ignite client failed to receive response payload", e);
        }

        if (payloadBytesNum < 0)
            throw new IgniteClientException("TCP Ignite client unexpectedly received no response payload");

        if (payloadBytesNum < resSize)
            throw new IgniteClientException(String.format(
                "TCP Ignite client received only %s bytes of response payload but %s bytes were expected",
                payloadBytesNum,
                resSize
            ));

        return payloadBytes;
    }

    /** Validate {@link IgniteClientConfiguration}. */
    private static void validateConfiguration(IgniteClientConfiguration cfg) {
        String error = null;

        if (cfg == null)
            error = "Ignite client configuration must be specified";
        else if (cfg.getHost() == null && cfg.getHost().length() == 0)
            error = "A cluster node must be specified in the Ignite client configuration";
        else if (cfg.getPort() < 1024 || cfg.getPort() > 49151)
            error = String.format("Ignite client port %s is out of valid ports range 1024...49151", cfg.getPort());

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
        send(ClientOperation.HANDSHAKE, req -> {
            req.writeShort(ver.major());
            req.writeShort(ver.minor());
            req.writeShort(ver.patch());
            req.writeByte((byte)2); // client code, always 2
        });

        // Response
        BinaryInputStream res = new BinaryHeapInputStream(receive());

        if (!res.readBoolean()) { // Success flag
            ProtocolVersion srvVer = new ProtocolVersion(res.readShort(), res.readShort(), res.readShort());

            BinaryReaderExImpl reader = new BinaryReaderExImpl(null, res, null, true);
            String err = reader.readString();

            throw new IgniteClientException(
                String.format("Client handshake failed: %s. Client version: %s. Server version: %s", err, ver, srvVer)
            );
        }
    }
}
