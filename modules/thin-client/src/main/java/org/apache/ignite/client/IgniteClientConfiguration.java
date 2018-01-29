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
 * {@link IgniteClient} configuration.
 */
public class IgniteClientConfiguration {
    /** Host. */
    private String host;

    /**
     * Constructor.
     * @param host name or IP address of an Ignite server node to connect to.
     */
    public IgniteClientConfiguration(String host) {
        if (host == null)
            throw new IllegalArgumentException("host must not be null.");

        if (host.length() == 0)
            throw new IllegalArgumentException("host must not be empty.");

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
}
