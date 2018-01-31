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
 * {@link TcpIgniteClient} configuration.
 */
public class IgniteClientConfiguration {
    /** Host. */
    private String host;

    /** Port. */
    private int port = 10800;

    /** Tcp no delay. */
    private boolean tcpNoDelay = false;

    /** Timeout. 0 means infinite. */
    private int timeout = 0;

    /** Send buffer size. 0 means system default. */
    private int sndBufSize = 0;

    /** Receive buffer size. 0 means system default. */
    private int rcvBufSize = 0;

    /**
     * Constructor.
     * @param host name or IP address of an Ignite server node to connect to.
     */
    public IgniteClientConfiguration(String host) {
        if (host == null || host.length() == 0)
            throw new IllegalArgumentException("host must be specified.");

        this.host = host;
    }

    /**
     * @return name or IP address of an Ignite server node to connect to.
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host name or IP address of an Ignite server node to connect to.
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return Ignite server port to connect to. Port 10800 is used by default.
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port Ignite server port to connect to. Port 10800 is used by default.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return Whether Nagle's algorithm is enabled.
     */
    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    /**
     * @param tcpNoDelay whether Nagle's algorithm is enabled.
     */
    public void tcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    /**
     * @return Send/receive timeout in milliseconds.
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * @param timeout Send/receive timeout in milliseconds.
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * @return Send buffer size.
     */
    public int getSendBufferSize() {
        return sndBufSize;
    }

    /**
     * @param sndBufSize Send buffer size.
     */
    public void setSendBufferSize(int sndBufSize) {
        this.sndBufSize = sndBufSize;
    }

    /**
     * @return Send buffer size.
     */
    public int getReceiveBufferSize() {
        return rcvBufSize;
    }

    /**
     * @param rcvBufSize Send buffer size.
     */
    public void setReceiveBufferSize(int rcvBufSize) {
        this.rcvBufSize = rcvBufSize;
    }
}
