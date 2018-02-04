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

import org.apache.ignite.internal.binary.streams.*;

import java.util.function.*;

/**
 * Processing thin client requests and responses.
 */
interface ClientChannel extends AutoCloseable {
    /**
     * @param op            Operation.
     * @param payloadWriter Payload writer.
     * @return Request ID.
     */
    public long send(ClientOperation op, Consumer<BinaryOutputStream> payloadWriter) throws IgniteClientException;

    /**
     * @param op        Operation.
     * @param reqId ID of the request to receive the response for.
     * @return Received operation payload bytes.
     */
    public byte[] receive(ClientOperation op, long reqId) throws IgniteClientException;
}
