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

import org.apache.ignite.*;
import org.apache.ignite.internal.binary.*;
import org.apache.ignite.internal.binary.streams.*;
import org.apache.ignite.marshaller.*;

import java.io.*;

/**
 * Implements {@link BinaryReader} for reading {@link InputStream}.
 * TODO: get rid of ignite-core dependency used for binary deserializer abd Ignite Binary Object deserializer.
 */
class BinaryStreamReader implements BinaryReader {
    /** Ignite Binary Object serializer/deserializer. */
    private static final Marshaller igniteBinDes = new BinaryMarshaller();

    /** Stream. */
    private final InputStream stream;

    /** Constructor. */
    BinaryStreamReader(InputStream stream) {
        if (stream == null)
            throw new IllegalArgumentException("Input stream must be specified.");

        this.stream = stream;
    }

    /** {@inheritDoc} */
    @Override public int readInt() throws IgniteClientException {
        return new BinaryHeapInputStream(readBytes(4)).readInt();
    }

    /** {@inheritDoc} */
    @Override public byte[] readBytes(int size) throws IgniteClientException {
        if (size == 0)
            return new byte[0];

        byte[] bytes = new byte[size];
        int bytesRead;

        try {
            bytesRead = stream.read(bytes, 0, size);
        }
        catch (IOException e) {
            throw new IgniteClientException("Binary stream reader failed reading data", e);
        }

        if (bytesRead != size)
            throw new IgniteClientException(
                bytesRead < 0 ?
                    "Binary stream reader unexpectedly reached end of stream" :
                    String.format("Binary stream reader received %s bytes but expected %s bytes", bytesRead, size)
            );

        return bytes;
    }

    /** {@inheritDoc} */
    @Override public boolean readBoolean() throws IgniteClientException {
        byte[] bytes = readBytes(1);

        return bytes[0] != 0;
    }

    /** {@inheritDoc} */
    @Override public short readShort() throws IgniteClientException {
        return new BinaryHeapInputStream(readBytes(2)).readShort();
    }

    /** {@inheritDoc} */
    @Override public <T> T readIgniteBinary() throws IgniteClientException {
        try {
            return igniteBinDes.unmarshal(stream, null);
        }
        catch (IgniteCheckedException e) {
            throw new IgniteClientException("Binary stream reader failed deserializing Ignite binary object", e);
        }
    }
}
