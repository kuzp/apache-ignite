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

/**
 * Implements {@link BinaryReader} for reading {@link InputStream}.
 */
public class BinaryStreamReader implements BinaryReader {
    /** Stream. */
    private final InputStream stream;

    /** Constructor. */
    BinaryStreamReader(InputStream stream) {
        if (stream == null)
            throw new IllegalArgumentException("Input stream must be specified.");

        this.stream = stream;
    }

    /** {@inheritDoc} */
    @Override public int readInt() throws IOException {
        byte[] bytes = new byte[4];

        int bytesRead = stream.read(bytes, 0, 4);

        if (bytesRead != 4)
            throw new IOException("Blocking int read received less data than expected.");

        return new BytesReader(bytes).readInt();
    }

    /** {@inheritDoc} */
    @Override public byte[] readBytes(int size) throws IOException {
        byte[] bytes = new byte[size];

        int bytesRead = stream.read(bytes, 0, size);

        if (bytesRead != size)
            throw new IOException("Blocking bytes read received less data than expected.");

        return bytes;
    }

    /** {@inheritDoc} */
    @Override public boolean readBoolean() throws IOException {
        byte[] bytes = new byte[1];

        int bytesRead = stream.read(bytes, 0, 1);

        if (bytesRead != 1)
            throw new IOException("Blocking boolean read received less data than expected.");

        return bytes[0] != 0;
    }

    /** {@inheritDoc} */
    @Override public short readShort() throws IOException {
        byte[] bytes = new byte[2];

        int bytesRead = stream.read(bytes, 0, 2);

        if (bytesRead != 2)
            throw new IOException("Blocking short read received less data than expected.");

        return new BytesReader(bytes).readShort();
    }
}
