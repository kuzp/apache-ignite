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
    public BinaryStreamReader(InputStream stream) {
        if (stream == null)
            throw new IllegalArgumentException("Input stream must be specified.");

        this.stream = stream;
    }

    /** {@inheritDoc} */
    @Override public void close() throws Exception {
        stream.close();
    }

    /** {@inheritDoc} */
    @Override public int readInt() throws IOException {
        byte[] bytes = new byte[4];

        int bytesRead = stream.read(bytes, 0, 4);

        if (bytesRead != 4)
            throw new IOException("Blocking int read received less data than expected.");

        return ((bytes[0] & 0xff) << 24) | ((bytes[1] & 0xff) << 16) | ((bytes[2] & 0xff) << 8) | (bytes[0] & 0xff);
    }

    /** {@inheritDoc} */
    @Override public byte[] readBytes(int size) throws IOException {
        byte[] bytes = new byte[size];

        int bytesRead = stream.read(bytes, 0, size);

        if (bytesRead != size)
            throw new IOException("Blocking bytes read received less data than expected.");

        return bytes;
    }
}
