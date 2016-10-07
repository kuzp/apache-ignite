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

package org.apache.ignite.internal.trace.atomic;

import org.apache.ignite.binary.BinaryObjectException;
import org.apache.ignite.binary.BinaryRawReader;
import org.apache.ignite.binary.BinaryRawWriter;
import org.apache.ignite.binary.BinaryReader;
import org.apache.ignite.binary.BinaryWriter;
import org.apache.ignite.binary.Binarylizable;
import org.apache.ignite.internal.util.typedef.internal.S;

import java.io.Serializable;

/**
 * Part of client processing.
 */
public class AtomicTraceClientSend implements Serializable, Binarylizable {
    /** */
    private static final long serialVersionUID = 0L;

    /** Future hash. */
    public int futHash;

    /** Request hash. */
    public int reqHash;

    /** Start time. */
    public long started;

    /** Duration between future creation and passing request to IO. */
    public long mapped;

    /** Send duration. */
    public long offered;

    /**
     * Default constructor.
     */
    public AtomicTraceClientSend() {
        // No-op.
    }

    /**
     * Constructor.
     *
     * @param futHash Future hash.
     * @param reqHash Request hash.
     * @param started Start time.
     * @param mapped Mapped time.
     * @param offered Sent time.
     */
    public AtomicTraceClientSend(int futHash, int reqHash, long started, long mapped, long offered) {
        this.futHash = futHash;
        this.reqHash = reqHash;
        this.started = started;
        this.mapped = mapped;
        this.offered = offered;
    }

    /** {@inheritDoc} */
    @Override public void writeBinary(BinaryWriter writer) throws BinaryObjectException {
        BinaryRawWriter rawWriter = writer.rawWriter();

        rawWriter.writeInt(futHash);
        rawWriter.writeInt(reqHash);
        rawWriter.writeLong(started);
        rawWriter.writeLong(mapped);
        rawWriter.writeLong(offered);
    }

    /** {@inheritDoc} */
    @Override public void readBinary(BinaryReader reader) throws BinaryObjectException {
        BinaryRawReader rawReader = reader.rawReader();

        futHash = rawReader.readInt();
        reqHash = rawReader.readInt();
        started = rawReader.readLong();
        mapped = rawReader.readLong();
        offered = rawReader.readLong();
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(AtomicTraceClientSend.class, this);
    }
}
