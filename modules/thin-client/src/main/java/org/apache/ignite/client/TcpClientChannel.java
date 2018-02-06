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

    /** Request id. */
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
    @Override public long send(ClientOperation op, Consumer<BinaryOutputStream> payloadWriter)
        throws IgniteClientException {
        long id = reqId.getAndIncrement();

        try (BinaryOutputStream req = new BinaryOffheapOutputStream(1024)) {
            req.writeInt(0); // reserve an integer for the request size
            req.writeShort(op.code());
            req.writeLong(id);

            if (payloadWriter != null)
                payloadWriter.accept(req);

            req.writeInt(0, req.position() - 4); // actual size

            out.write(req.array(), 0, req.position());
        }
        catch (IOException e) {
            throw new IgniteClientException("TCP Ignite client failed to send data", e);
        }

        return id;
    }

    /** {@inheritDoc} */
    @Override public BinaryInputStream receive(ClientOperation op, long reqId) throws IgniteClientException {
        final int MIN_RES_SIZE = 8 + 4; // minimal response size: long (8 bytes) ID + int (4 bytes) status

        int resSize = new BinaryHeapInputStream(read(4)).readInt();

        if (resSize < 0)
            throw new IgniteClientException(
                String.format("TCP Ignite client received invalid response size: %s", resSize)
            );

        if (resSize == 0)
            return BinaryHeapInputStream.create(new byte[0], 0);

        BinaryInputStream resIn = new BinaryHeapInputStream(read(MIN_RES_SIZE));

        long resId = resIn.readLong();

        if (resId != reqId)
            throw new IgniteClientException(
                String.format("Unexpected response [%s] received, [%s] was expected", resId, reqId)
            );

        int status = resIn.readInt();

        if (status != 0) {
            String err = new BinaryReaderExImpl(null, resIn, null, true).readString();

            throw new IgniteClientException(
                String.format("Ignite failed to process client request [%s]: %s", reqId, err)
            );
        }

        byte[] payload = resSize > MIN_RES_SIZE ? read(resSize - MIN_RES_SIZE) : new byte[0];

        return BinaryHeapInputStream.create(payload, 0);
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
        handshakeReq();
        handshakeRes();
    }

    /** Send handshake request. */
    private void handshakeReq() throws IgniteClientException {
        try (BinaryOutputStream req = new BinaryOffheapOutputStream(32)) {
            req.writeInt(0); // reserve an integer for the request size
            req.writeByte((byte)1); // handshake code, always 1
            req.writeShort(ver.major());
            req.writeShort(ver.minor());
            req.writeShort(ver.patch());
            req.writeByte((byte)2); // client code, always 2
            req.writeInt(0, req.position() - 4); // actual size

            out.write(req.array(), 0, req.position());
        }
        catch (IOException e) {
            throw new IgniteClientException("TCP Ignite client failed to send handshake request", e);
        }
    }

    /** Receive and handle handshake response. */
    private void handshakeRes() throws IgniteClientException {
        int resSize = new BinaryHeapInputStream(read(4)).readInt();

        if (resSize <= 0)
            throw new IgniteClientException(
                String.format("TCP Ignite client received invalid handshake response size: %s", resSize)
            );

        BinaryInputStream res = new BinaryHeapInputStream(read(resSize));

        if (!res.readBoolean()) { // success flag
            ProtocolVersion srvVer = new ProtocolVersion(res.readShort(), res.readShort(), res.readShort());

            String err = new BinaryReaderExImpl(null, res, null, true).readString();

            throw new IgniteClientException(
                String.format("Client handshake failed: %s. Client version: %s. Server version: %s", err, ver, srvVer)
            );
        }
    }

    /** Read bytes from the input stream. */
    private byte[] read(int len) throws IgniteClientException {
        byte[] bytes = new byte[len];
        int bytesNum;

        try {
            bytesNum = in.read(bytes, 0, len);
        }
        catch (IOException e) {
            throw new IgniteClientException("TCP Ignite client failed to read response", e);
        }

        if (bytesNum < 0)
            throw new IgniteClientException("TCP Ignite client received no response");

        if (bytesNum < len)
            throw new IgniteClientException(String.format(
                "TCP Ignite client received only %s bytes of response data but %s bytes were expected",
                bytesNum,
                len
            ));

        return bytes;
    }
}
