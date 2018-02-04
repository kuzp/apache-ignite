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

/** Operation codes. */
enum ClientOperation {
    /** Cache get or create with name. */CACHE_GET_OR_CREATE_WITH_NAME(1052),
    /** Cache put. */CACHE_PUT(1001),
    /** Cache get. */CACHE_GET(1000);

    /** Code. */
    private final int code;

    /** Constructor. */
    ClientOperation(int code) {
        this.code = code;
    }

    /**
     * @return Code.
     */
    public short code() {
        return (short)code;
    }
}
