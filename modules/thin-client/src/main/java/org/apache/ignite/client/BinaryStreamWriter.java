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

import java.io.*;

/**
 * Implementation of {@link BinaryWriter} for {@link OutputStream}.
 * TODO: get rid of ignite-core dependency used for binary serializer abd Ignite Binary Object serializer.
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
    @Override public void flush() throws IgniteClientException {
        try {
            stream.flush();
        }
        catch (IOException e) {
            throw new IgniteClientException("Binary stream writer failed flushing data");
        }
    }

    /** {@inheritDoc} */
    @Override public void writeBytes(byte[] val) throws IgniteClientException {
        if (val != null && val.length > 0)
            writeBytes(val, val.length);
    }

    /** {@inheritDoc} */
    @Override public void writeByte(byte val) throws IgniteClientException {
        writeBytes(new byte[] {val});
    }

    /** {@inheritDoc} */
    @Override public void writeShort(short val) throws IgniteClientException {
        BinaryOutputStream s = new BinaryHeapOutputStream(2);

        s.writeShort(val);

        writeBytes(s.array(), s.position());
    }

    /** {@inheritDoc} */
    @Override public void writeInt(int val) throws IgniteClientException {
        BinaryOutputStream s = new BinaryHeapOutputStream(4);

        s.writeInt(val);

        writeBytes(s.array(), s.position());
    }

    /** Write bytes. */
    private void writeBytes(byte[] val, int len) throws IgniteClientException {
        try {
            stream.write(val, 0, len);
        }
        catch (IOException e) {
            throw new IgniteClientException("Binary stream writer failed writing data", e);
        }
    }
}
