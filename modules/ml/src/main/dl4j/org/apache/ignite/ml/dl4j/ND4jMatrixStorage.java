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
import org.apache.ignite.ml.math.MatrixStorage;
import org.apache.ignite.ml.math.StorageConstants;
import org.apache.ignite.ml.math.exceptions.UnsupportedOperationException;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * TODO: add description.
 */
public class ND4jMatrixStorage implements MatrixStorage {
    private INDArray array;
    private int row;
    private int col;

    ND4jMatrixStorage(int row, int col){
        this.array = Nd4j.create(row, col);
        this.row = row;
        this.col = col;
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
        return true;
    }

    /** {@inheritDoc} */
    @Override public boolean isArrayBased() {
        return true;
    }

    /** {@inheritDoc} */
    @Override public boolean isDistributed() {
        return false;
    }

    /** {@inheritDoc} */
    @Override public double get(int x, int y) {
        return array.getDouble(x, y);
    }

    /** {@inheritDoc} */
    @Override public void set(int x, int y, double v) {
        array.putScalar(x, y, v);
    }

    /** {@inheritDoc} */
    @Override public int columnSize() {
        return col;
    }

    /** {@inheritDoc} */
    @Override public int rowSize() {
        return row;
    }

    /** {@inheritDoc} */
    @Override public int storageMode() {
        return StorageConstants.ROW_STORAGE_MODE;
    }

    /** {@inheritDoc} */
    @Override public int accessMode() {
        return StorageConstants.RANDOM_ACCESS_MODE;
    }

    /** {@inheritDoc} */
    @Override public double[] data() {
        return Nd4j.toFlattened(array).data().asDouble();
    }

    /** {@inheritDoc} */
    @Override public void writeExternal(ObjectOutput out) throws IOException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        throw new UnsupportedOperationException();
    }
}
