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

package org.apache.ignite.ml.dl4j;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.apache.ignite.ml.math.VectorStorage;
import org.apache.ignite.ml.math.exceptions.UnsupportedOperationException;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * TODO: add description.
 */
public class Nd4jVectorStorage implements VectorStorage{
    private int size;
    INDArray array;

    /** */
    Nd4jVectorStorage(){

    }

    /** */
    Nd4jVectorStorage(int size){
        array = Nd4j.create(size, 1);
        this.size = size;
    }

    /** {@inheritDoc} */
    @Override public int size() {
        return size;
    }

    /** {@inheritDoc} */
    @Override public double get(int i) {
        return array.getDouble(i);
    }

    /** {@inheritDoc} */
    @Override public void set(int i, double v) {
        array.putScalar(i, v);
    }

    /** {@inheritDoc} */
    @Override public double[] data() {
        return array.data().asDouble();
    }

    /** {@inheritDoc} */
    @Override public void writeExternal(ObjectOutput out) throws IOException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override public boolean isSequentialAccess() {
        return false;
    }

    /** {@inheritDoc} */
    @Override public boolean isRandomAccess() {
        return true;
    }

    /** {@inheritDoc} */
    @Override public boolean isDense() {
        return false;
    }

    /** {@inheritDoc} */
    @Override public boolean isArrayBased() {
        return false;
    }

    /** {@inheritDoc} */
    @Override public boolean isDistributed() {
        return false;
    }
}
