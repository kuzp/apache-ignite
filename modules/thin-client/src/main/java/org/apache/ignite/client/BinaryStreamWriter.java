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
 * Implementation of {@link BinaryWriter} for {@link OutputStream}.
 */
class BinaryStreamWriter implements BinaryWriter {
    /** Stream. */
    private final BufferedOutputStream stream;

    /** Constructor. */
    BinaryStreamWriter(OutputStream stream) {
        if (stream == null)
            throw new IllegalArgumentException("Output stream must be specified.");

        this.stream = stream instanceof BufferedOutputStream ?
            (BufferedOutputStream)stream :
            new BufferedOutputStream(stream);
    }

    /** {@inheritDoc} */
    @Override public void flush() throws IOException {
        stream.flush();
    }

    /** {@inheritDoc} */
    @Override public void writeByte(byte val) throws IOException {
        stream.write(new byte[] {val});
    }

    /** {@inheritDoc} */
    @Override public void writeShort(short val) throws IOException {
        stream.write(new byte[] {(byte)(val & 0xff), (byte)(val >> 8 & 0xff)});
    }

    /** {@inheritDoc} */
    @Override public void writeInt(int val) throws IOException {
        stream.write(new byte[] {
            (byte)(val & 0xff),
            (byte)(val >> 8 & 0xff),
            (byte)(val >> 16 & 0xff),
            (byte)(val >> 24 & 0xff)
        });
    }
}
