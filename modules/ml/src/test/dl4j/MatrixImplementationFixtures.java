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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.apache.ignite.ml.dl4j.ND4JMatrix;
import org.apache.ignite.ml.math.Matrix;
import org.jetbrains.annotations.NotNull;

/** */
class MatrixImplementationFixtures {
    /** */
    private static final List<Supplier<Iterable<Matrix>>> suppliers = Arrays.asList(
        (Supplier<Iterable<Matrix>>)ND4JMatrixFixture::new
    );

    /** */
    void consumeSampleMatrix(BiConsumer<Matrix, String> consumer) {
        for (Supplier<Iterable<Matrix>> fixtureSupplier : suppliers) {
            final Iterable<Matrix> fixture = fixtureSupplier.get();

            for (Matrix matrix : fixture) {
                consumer.accept(matrix, fixture.toString());

                matrix.destroy();
            }
        }
    }

    private static class ND4JMatrixFixture extends MatrixSizeIterator{
        ND4JMatrixFixture(){
            super(ND4JMatrix::new, "ND4JMatrix");
        }
    }
    /** */
    private static class MatrixSizeIterator implements Iterable<Matrix> {
        /** */
        private final Integer[] rows = new Integer[] {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 512, 1024, null};
        /** */
        private final Integer[] cols = new Integer[] {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 1024, 512, null};
        /** */
        private int sizeIdx = 0;

        /** */
        private BiFunction<Integer, Integer, ? extends Matrix> constructor;
        /** */
        private String desc;

        /** */
        MatrixSizeIterator(BiFunction<Integer, Integer, ? extends Matrix> constructor, String desc) {
            this.constructor = constructor;
            this.desc = desc;
        }

        /** */
        public BiFunction<Integer, Integer, ? extends Matrix> getConstructor() {
            return constructor;
        }

        /** */
        int getSizeIdx() {
            return sizeIdx;
        }

        /** */
        @Override public String toString() {
            return desc + "{rows=" + rows[sizeIdx] + ", cols=" + cols[sizeIdx] + "}";
        }

        /** */
        boolean hasNextRow(int idx) {
            return rows[idx] != null;
        }

        /** */
        boolean hasNextCol(int idx) {
            return cols[idx] != null;
        }

        /** */
        Integer getRow(int idx) {
            return rows[idx];
        }

        /** */
        int getCol(int idx) {
            return cols[idx];
        }

        /** {@inheritDoc} */
        @NotNull
        @Override public Iterator<Matrix> iterator() {
            return new Iterator<Matrix>() {
                /** {@inheritDoc} */
                @Override public boolean hasNext() {
                    return hasNextCol(sizeIdx) && hasNextRow(sizeIdx);
                }

                /** {@inheritDoc} */
                @Override public Matrix next() {
                    Matrix matrix = constructor.apply(rows[sizeIdx], cols[sizeIdx]);

                    nextIdx();

                    return matrix;
                }
            };
        }

        /** */
        void nextIdx() {
            sizeIdx++;
        }
    }
}
