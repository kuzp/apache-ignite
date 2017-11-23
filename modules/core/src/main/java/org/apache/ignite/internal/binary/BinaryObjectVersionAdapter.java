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

package org.apache.ignite.internal.binary;

import java.util.Map;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.binary.BinaryObjectBuilder;
import org.apache.ignite.binary.BinaryObjectException;
import org.apache.ignite.binary.BinaryType;

/** */
public class BinaryObjectVersionAdapter implements BinaryObject {
    /** */
    BinaryObject obj;

    /** */
    Map<String, BinarySchemaFieldState> fieldStates;

    /**
     *
     * @param obj
     */
    public BinaryObjectVersionAdapter(BinaryObject obj, Map<String, BinarySchemaFieldState> fieldStates) {
        this.obj = obj;
        this.fieldStates = fieldStates;
    }

    /** {@inheritDoc} */
    @Override public BinaryType type() throws BinaryObjectException {
        return obj.type();
    }

    /** {@inheritDoc} */
    @Override public <F> F field(String fieldName) throws BinaryObjectException {
        BinarySchemaFieldState st = fieldStates.get(fieldName);

        if (st != BinarySchemaFieldState.OK)
            return null;

        return obj.field(fieldName);
    }

    /** {@inheritDoc} */
    @Override public boolean hasField(String fieldName) {
        BinarySchemaFieldState st = fieldStates.get(fieldName);

        if (st != BinarySchemaFieldState.OK)
            return false;

        return obj.hasField(fieldName);
    }

    /** {@inheritDoc} */
    @Override public <T> T deserialize() throws BinaryObjectException {
        return obj.deserialize();
    }

    /** {@inheritDoc} */
    @Override public BinaryObject clone() throws CloneNotSupportedException {
        return obj.clone();
    }

    /** {@inheritDoc} */
    @Override public BinaryObjectBuilder toBuilder() throws BinaryObjectException {
        return obj.toBuilder();
    }

    /** {@inheritDoc} */
    @Override public int enumOrdinal() throws BinaryObjectException {
        return obj.enumOrdinal();
    }

    /** {@inheritDoc} */
    @Override public String enumName() throws BinaryObjectException {
        return obj.enumName();
    }
}
