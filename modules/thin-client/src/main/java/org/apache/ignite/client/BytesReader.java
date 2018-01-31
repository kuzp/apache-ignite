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

import java.util.*;

/**
 * Implements {@link BinaryReader} for reading a byte array.
 */
class BytesReader implements BinaryReader {
    /** Stream. */
    private final byte[] bytes;

    /** Reading position. */
    private int pos = 0;

    /** Constructor. */
    BytesReader(byte[] bytes) {
        if (bytes == null || bytes.length == 0)
            throw new IllegalArgumentException("Not-empty bytes array must be specified.");

        this.bytes = bytes;
    }

    /** {@inheritDoc} */
    @Override public int readInt() {
        if (bytes.length - pos < 4)
            throw new IllegalStateException("Not enough bytes to read an integer");

        return ((bytes[pos++] & 0xff) << 24) |
            ((bytes[pos++] & 0xff) << 16) |
            ((bytes[pos++] & 0xff) << 8) |
            (bytes[pos++] & 0xff);
    }

    /** {@inheritDoc} */
    @Override public byte[] readBytes(int size) {
        if (bytes.length - pos < size)
            throw new IllegalStateException(String.format("Not enough bytes to read %s bytes", size));

        return Arrays.copyOfRange(bytes, pos, pos += size);
    }

    /** {@inheritDoc} */
    @Override public boolean readBoolean() {
        if (bytes.length - pos < 1)
            throw new IllegalStateException("Not enough bytes to read an boolean");

        boolean res = bytes[pos] != 0;

        pos++;

        return res;
    }

    /** {@inheritDoc} */
    @Override public short readShort() {
        if (bytes.length - pos < 2)
            throw new IllegalStateException("Not enough bytes to read a short");

        return (short)(((bytes[pos++] & 0xff) << 8) | (bytes[pos++] & 0xff));
    }
}
