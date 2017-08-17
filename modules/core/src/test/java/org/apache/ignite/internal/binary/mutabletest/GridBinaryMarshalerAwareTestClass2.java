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

package org.apache.ignite.internal.binary.mutabletest;

import org.apache.ignite.binary.BinaryObjectException;
import org.apache.ignite.binary.BinaryRawReader;
import org.apache.ignite.binary.BinaryRawWriter;
import org.apache.ignite.binary.BinaryReader;
import org.apache.ignite.binary.BinaryWriter;
import org.apache.ignite.binary.Binarylizable;
import org.apache.ignite.internal.util.typedef.internal.S;
import org.apache.ignite.testframework.GridTestUtils;

/**
 *
 */
public class GridBinaryMarshalerAwareTestClass2 implements Binarylizable {
    /** */
    public int i1;

    /** */
    public String s1;

    /** */
    public int i2;

    /** */
    public String s2;

    /** */
    public int i3;

    /** {@inheritDoc} */
    @Override public void writeBinary(BinaryWriter writer) throws BinaryObjectException {
        writer.writeInt("i1", i1);
        writer.writeString("s1", s1);

        BinaryRawWriter raw = writer.rawWriter();

        raw.writePackedInt(i2);
        raw.writeString(s2);
        raw.writePackedInt(i3);
    }

    /** {@inheritDoc} */
    @Override public void readBinary(BinaryReader reader) throws BinaryObjectException {
        i1 = reader.readInt("i1");
        s1 = reader.readString("s1");

        BinaryRawReader raw = reader.rawReader();

        i2 = raw.readPackedInt();
        s2 = raw.readString();
        i3 = raw.readPackedInt();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("FloatingPointEquality")
    @Override public boolean equals(Object other) {
        return this == other || GridTestUtils.deepEquals(this, other);
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(GridBinaryMarshalerAwareTestClass2.class, this);
    }
}
