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

import java.util.function.BiConsumer;
import java.util.function.Supplier;
import org.apache.ignite.ml.math.ExternalizeTest;
import org.apache.ignite.ml.math.Matrix;
import org.apache.ignite.ml.math.Vector;
import org.apache.ignite.ml.math.exceptions.CardinalityException;
import org.apache.ignite.ml.math.exceptions.ColumnIndexException;
import org.apache.ignite.ml.math.exceptions.IndexException;
import org.apache.ignite.ml.math.exceptions.RowIndexException;
import org.apache.ignite.ml.math.exceptions.UnsupportedOperationException;
import org.apache.ignite.ml.math.impls.matrix.DenseLocalOffHeapMatrix;
import org.apache.ignite.ml.math.impls.matrix.DenseLocalOnHeapMatrix;
import org.apache.ignite.ml.math.impls.matrix.RandomMatrix;
import org.apache.ignite.ml.math.impls.vector.DenseLocalOnHeapVector;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for {@link Matrix} implementations.
 */
public class MatrixImplementationsTest extends ExternalizeTest<Matrix> {
    /** */
    private static final double DEFAULT_DELTA = 0.000000001d;

    /** */
    private void consumeSampleMatrix(BiConsumer<Matrix, String> consumer) {
        new MatrixImplementationFixtures().consumeSampleMatrix(consumer);
    }

    /** */
    @Test
    public void externalizeTest() {
        consumeSampleMatrix((m, desc) -> externalizeTest(m));
    }

    /** */
    @Test
    public void testAssignSingleElement() {
        consumeSampleMatrix((m, desc) -> {

            final double assignVal = Math.random();

            m.assign(assignVal);

            for (int i = 0; i < m.rowSize(); i++)
                for (int j = 0; j < m.columnSize(); j++)
                    assertEquals("Unexpected value for " + desc + " at (" + i + "," + j + ")",
                        assignVal, m.get(i, j), 0.000001d);
        });
    }

    /** */
    @Test
    public void testAssignArray() {
        consumeSampleMatrix((m, desc) -> {


            double[][] data = new double[m.rowSize()][m.columnSize()];

            for (int i = 0; i < m.rowSize(); i++)
                for (int j = 0; j < m.columnSize(); j++)
                    data[i][j] = Math.random();

            m.assign(data);

            for (int i = 0; i < m.rowSize(); i++) {
                for (int j = 0; j < m.columnSize(); j++)
                    assertEquals("Unexpected value for " + desc + " at (" + i + "," + j + ")",
                        data[i][j], m.get(i, j), 0.000001d);
            }
        });
    }

    /** */
    @Test
    public void testAssignFunction() {
        consumeSampleMatrix((m, desc) -> {

            m.assign((i, j) -> (double)(i * m.columnSize() + j));

            for (int i = 0; i < m.rowSize(); i++) {
                for (int j = 0; j < m.columnSize(); j++)
                    assertEquals("Unexpected value for " + desc + " at (" + i + "," + j + ")",
                        (double)(i * m.columnSize() + j), m.get(i, j), 0.000001d);
            }
        });
    }

    /** */
    @Test
    public void testPlus() {
        consumeSampleMatrix((m, desc) -> {

            double[][] data = fillAndReturn(m);

            double plusVal = Math.random();

            Matrix plus = m.plus(plusVal);

            for (int i = 0; i < m.rowSize(); i++)
                for (int j = 0; j < m.columnSize(); j++)
                    assertEquals("Unexpected value for " + desc + " at (" + i + "," + j + ")",
                        data[i][j] + plusVal, plus.get(i, j), 0.000001d);
        });
    }

    /** */
    @Test
    public void testPlusMatrix() {
        consumeSampleMatrix((m, desc) -> {


            double[][] data = fillAndReturn(m);

            Matrix plus = m.plus(m);

            for (int i = 0; i < m.rowSize(); i++)
                for (int j = 0; j < m.columnSize(); j++)
                    assertEquals("Unexpected value for " + desc + " at (" + i + "," + j + ")",
                        data[i][j] * 2.0, plus.get(i, j), 0.000001d);
        });
    }

    /** */
    @Test
    public void testMinusMatrix() {
        consumeSampleMatrix((m, desc) -> {


            fillMatrix(m);

            Matrix minus = m.minus(m);

            for (int i = 0; i < m.rowSize(); i++)
                for (int j = 0; j < m.columnSize(); j++)
                    assertEquals("Unexpected value for " + desc + " at (" + i + "," + j + ")",
                        0.0, minus.get(i, j), 0.000001d);
        });
    }

    /** */
    @Test
    public void testTimes() {
        consumeSampleMatrix((m, desc) -> {

            double[][] data = fillAndReturn(m);

            double timeVal = Math.random();
            Matrix times = m.times(timeVal);

            for (int i = 0; i < m.rowSize(); i++)
                for (int j = 0; j < m.columnSize(); j++)
                    assertEquals("Unexpected value for " + desc + " at (" + i + "," + j + ")",
                        data[i][j] * timeVal, times.get(i, j), 0.000001d);
        });
    }

    /** */
    @Test
    public void testTimesVector() {
        consumeSampleMatrix((m, desc) -> {


            if (m instanceof DenseLocalOffHeapMatrix)
                return; //TODO: IGNITE-5535, waiting offheap support.

            double[][] data = fillAndReturn(m);

            double[] arr = fillArray(m.columnSize());

            Vector times = m.times(new DenseLocalOnHeapVector(arr));

            assertEquals("Unexpected vector size for " + desc, times.size(), m.rowSize());

            for (int i = 0; i < m.rowSize(); i++) {
                double exp = 0.0;

                for (int j = 0; j < m.columnSize(); j++)
                    exp += arr[j] * data[i][j];

                assertEquals("Unexpected value for " + desc + " at " + i,
                    times.get(i), exp, DEFAULT_DELTA);
            }

            testInvalidCardinality(() -> m.times(new DenseLocalOnHeapVector(m.columnSize() + 1)), desc);
        });
    }

    /** */
    @Test
    public void testTimesMatrix() {
        consumeSampleMatrix((m, desc) -> {

            if (m instanceof DenseLocalOffHeapMatrix)
                return;

            double[][] data = fillAndReturn(m);

            double[] arr = fillArray(m.columnSize());

            Matrix mult = m.like(m.columnSize(), 1);

            mult.setColumn(0, arr);

            Matrix times = m.times(mult);

            assertEquals("Unexpected rows for " + desc, times.rowSize(), m.rowSize());

            assertEquals("Unexpected cols for " + desc, times.columnSize(), 1);

            for (int i = 0; i < m.rowSize(); i++) {
                double exp = 0.0;

                for (int j = 0; j < m.columnSize(); j++)
                    exp += arr[j] * data[i][j];

                assertEquals("Unexpected value for " + desc + " at " + i,
                    exp, times.get(i, 0), DEFAULT_DELTA);
            }

            testInvalidCardinality(() -> m.times(new DenseLocalOnHeapMatrix(m.columnSize() + 1, 1)), desc);
        });
    }

    /** */
    @Test
    public void testDivide() {
        consumeSampleMatrix((m, desc) -> {


            double[][] data = fillAndReturn(m);

            double divVal = Math.random();

            Matrix divide = m.divide(divVal);

            for (int i = 0; i < m.rowSize(); i++)
                for (int j = 0; j < m.columnSize(); j++)
                    assertEquals("Unexpected value for " + desc + " at (" + i + "," + j + ")",
                        data[i][j] / divVal, divide.get(i, j), 0.000001d);
        });
    }

    /** */
    @Test
    public void testTranspose() {
        consumeSampleMatrix((m, desc) -> {


            fillMatrix(m);

            Matrix transpose = m.transpose();

            for (int i = 0; i < m.rowSize(); i++)
                for (int j = 0; j < m.columnSize(); j++)
                    assertEquals("Unexpected value for " + desc + " at (" + i + "," + j + ")",
                        m.get(i, j), transpose.get(j, i), 0.000001d);
        });
    }

    /** */
    @Test
    public void testDeterminant() {
        consumeSampleMatrix((m, desc) -> {
            if (m.rowSize() != m.columnSize())
                return;


            double[][] doubles = fillIntAndReturn(m);

            if (m.rowSize() == 1) {
                assertEquals("Unexpected value " + desc, m.determinant(), doubles[0][0], 0.000001d);

                return;
            }

            if (m.rowSize() == 2) {
                double det = doubles[0][0] * doubles[1][1] - doubles[0][1] * doubles[1][0];
                assertEquals("Unexpected value " + desc, m.determinant(), det, 0.000001d);

                return;
            }

            if (m.rowSize() > 512)
                return; // IMPL NOTE if row size >= 30000 it takes unacceptably long for normal test run.

            Matrix diagMtx = m.like(m.rowSize(), m.columnSize());

            diagMtx.assign(0);
            for (int i = 0; i < m.rowSize(); i++)
                diagMtx.set(i, i, m.get(i, i));

            double det = 1;

            for (int i = 0; i < diagMtx.rowSize(); i++)
                det *= diagMtx.get(i, i);

            try {
                assertEquals("Unexpected value " + desc, det, diagMtx.determinant(), DEFAULT_DELTA);
            }
            catch (Exception e) {
                System.out.println(desc);
                throw e;
            }
        });
    }

    /** */
    @Test
    public void testInverse() {
        consumeSampleMatrix((m, desc) -> {
            if (m.rowSize() != m.columnSize())
                return;



            if (m.rowSize() > 256)
                return; // IMPL NOTE this is for quicker test run.

            fillNonSingularMatrix(m);

            assertTrue("Unexpected zero determinant " + desc, Math.abs(m.determinant()) > 0.000001d);

            Matrix inverse = m.inverse();

            Matrix mult = m.times(inverse);

            final double delta = 0.001d;

            assertEquals("Unexpected determinant " + desc, 1d, mult.determinant(), delta);

            assertEquals("Unexpected top left value " + desc, 1d, mult.get(0, 0), delta);

            if (m.rowSize() == 1)
                return;

            assertEquals("Unexpected center value " + desc,
                1d, mult.get(m.rowSize() / 2, m.rowSize() / 2), delta);

            assertEquals("Unexpected bottom right value " + desc,
                1d, mult.get(m.rowSize() - 1, m.rowSize() - 1), delta);

            assertEquals("Unexpected top right value " + desc,
                0.000001d, mult.get(0, m.rowSize() - 1), delta);

            assertEquals("Unexpected bottom left value " + desc,
                0.000001d, mult.get(m.rowSize() - 1, 0), delta);
        });
    }

    /** */
    @Test
    public void testMap() {
        consumeSampleMatrix((m, desc) -> {


            fillMatrix(m);

            m.map(x -> 10.000001d);

            for (int i = 0; i < m.rowSize(); i++)
                for (int j = 0; j < m.columnSize(); j++)
                    assertEquals("Unexpected value for " + desc + " at (" + i + "," + j + ")",
                        10.000001d, m.get(i, j), 0.000001d);
        });
    }

    /** */
    @Test
    public void testMapMatrix() {
        consumeSampleMatrix((m, desc) -> {

            double[][] doubles = fillAndReturn(m);

            testMapMatrixWrongCardinality(m, desc);

            Matrix cp = m.copy();

            m.map(cp, (m1, m2) -> m1 + m2);

            for (int i = 0; i < m.rowSize(); i++)
                for (int j = 0; j < m.columnSize(); j++)
                    assertEquals("Unexpected value for " + desc + " at (" + i + "," + j + ")",
                        m.get(i, j), doubles[i][j] * 2, 0.000001d);
        });
    }

    /** */
    @Test
    public void testViewRow() {
        consumeSampleMatrix((m, desc) -> {

                fillMatrix(m);

            for (int i = 0; i < m.rowSize(); i++) {
                Vector vector = m.viewRow(i);
                assert vector != null;

                for (int j = 0; j < m.columnSize(); j++)
                    assertEquals("Unexpected value for " + desc + " at (" + i + "," + j + ")",
                        m.get(i, j), vector.get(j), 0.000001d);
            }
        });
    }

    /** */
    @Test
    public void testViewCol() {
        consumeSampleMatrix((m, desc) -> {

                fillMatrix(m);

            for (int i = 0; i < m.columnSize(); i++) {
                Vector vector = m.viewColumn(i);
                assert vector != null;

                for (int j = 0; j < m.rowSize(); j++)
                    assertEquals("Unexpected value for " + desc + " at (" + i + "," + j + ")",
                        m.get(j, i), vector.get(j), 0.000001d);
            }
        });
    }

    /** */
    @Test
    public void testFoldRow() {
        consumeSampleMatrix((m, desc) -> {


            fillMatrix(m);

            Vector foldRows = m.foldRows(Vector::sum);

            for (int i = 0; i < m.rowSize(); i++) {
                Double locSum = 0.000001d;

                for (int j = 0; j < m.columnSize(); j++)
                    locSum += m.get(i, j);

                assertEquals("Unexpected value for " + desc + " at " + i,
                    foldRows.get(i), locSum, 0.000001d);
            }
        });
    }

    /** */
    @Test
    public void testFoldCol() {
        consumeSampleMatrix((m, desc) -> {

            fillMatrix(m);

            Vector foldCols = m.foldColumns(Vector::sum);

            for (int j = 0; j < m.columnSize(); j++) {
                Double locSum = 0.000001d;

                for (int i = 0; i < m.rowSize(); i++)
                    locSum += m.get(i, j);

                assertEquals("Unexpected value for " + desc + " at " + j,
                    foldCols.get(j), locSum, 0.000001d);
            }
        });
    }

    /** */
    @Test
    public void testSum() {
        consumeSampleMatrix((m, desc) -> {
            double[][] data = fillAndReturn(m);

            double sum = m.sum();

            double rawSum = 0;
            for (double[] anArr : data)
                for (int j = 0; j < data[0].length; j++)
                    rawSum += anArr[j];

            assertEquals("Unexpected value for " + desc,
                rawSum, sum, 0.00001d);
        });
    }

    /** */
    @Test
    public void testMax() {
        consumeSampleMatrix((m, desc) -> {
            double[][] doubles = fillAndReturn(m);
            double max = Double.NEGATIVE_INFINITY;

            for (int i = 0; i < m.rowSize(); i++)
                for (int j = 0; j < m.columnSize(); j++)
                    max = max < doubles[i][j] ? doubles[i][j] : max;

            assertEquals("Unexpected value for " + desc, m.maxValue(), max, 0.000001d);
        });
    }

    /** */
    @Test
    public void testMin() {
        consumeSampleMatrix((m, desc) -> {
            double[][] doubles = fillAndReturn(m);
            double min = Double.MAX_VALUE;

            for (int i = 0; i < m.rowSize(); i++)
                for (int j = 0; j < m.columnSize(); j++)
                    min = min > doubles[i][j] ? doubles[i][j] : min;

            assertEquals("Unexpected value for " + desc, m.minValue(), min, 0.000001d);
        });
    }

    /** */
    @Test
    public void testGetElement() {
        consumeSampleMatrix((m, desc) -> {

                fillMatrix(m);

            for (int i = 0; i < m.rowSize(); i++)
                for (int j = 0; j < m.columnSize(); j++) {
                    final Matrix.Element e = m.getElement(i, j);

                    final String details = desc + " at [" + i + "," + j + "]";

                    assertEquals("Unexpected element row " + details, i, e.row());
                    assertEquals("Unexpected element col " + details, j, e.column());

                    final double val = m.get(i, j);

                    assertEquals("Unexpected value for " + details, val, e.get(), 0.000001d);

                    boolean expECaught = false;

                    final double newVal = val * 2.0;

                    try {
                        e.set(newVal);
                    }
                    catch (UnsupportedOperationException uoe) {

                        expECaught = true;
                    }


                    assertEquals("Unexpected value set for " + details, newVal, m.get(i, j), 0.000001d);
                }
        });
    }

    /** */
    @Test
    public void testGetX() {
        consumeSampleMatrix((m, desc) -> {

                fillMatrix(m);

            for (int i = 0; i < m.rowSize(); i++)
                for (int j = 0; j < m.columnSize(); j++)
                    assertEquals("Unexpected value for " + desc + " at [" + i + "," + j + "]",
                        m.get(i, j), m.getX(i, j), 0.000001d);
        });
    }

    /** */
    @Test
    public void testGetMetaStorage() {
        consumeSampleMatrix((m, desc) -> assertNotNull("Null meta storage in " + desc, m.getMetaStorage()));
    }

    /** */
    @Test
    public void testGuid() {
        consumeSampleMatrix((m, desc) -> assertNotNull("Null guid in " + desc, m.guid()));
    }

    /** */
    @Test
    public void testSwapRows() {
        consumeSampleMatrix((m, desc) -> {


            double[][] doubles = fillAndReturn(m);

            final int swap_i = m.rowSize() == 1 ? 0 : 1;
            final int swap_j = 0;

            Matrix swap = m.swapRows(swap_i, swap_j);

            for (int col = 0; col < m.columnSize(); col++) {
                assertEquals("Unexpected value for " + desc + " at col " + col + ", swap_i " + swap_i,
                    swap.get(swap_i, col), doubles[swap_j][col], 0.000001d);

                assertEquals("Unexpected value for " + desc + " at col " + col + ", swap_j " + swap_j,
                    swap.get(swap_j, col), doubles[swap_i][col], 0.000001d);
            }

            testInvalidRowIndex(() -> m.swapRows(-1, 0), desc + " negative first swap index");
            testInvalidRowIndex(() -> m.swapRows(0, -1), desc + " negative second swap index");
            testInvalidRowIndex(() -> m.swapRows(m.rowSize(), 0), desc + " too large first swap index");
            testInvalidRowIndex(() -> m.swapRows(0, m.rowSize()), desc + " too large second swap index");
        });
    }

    /** */
    @Test
    public void testSwapColumns() {
        consumeSampleMatrix((m, desc) -> {


            double[][] doubles = fillAndReturn(m);

            final int swap_i = m.columnSize() == 1 ? 0 : 1;
            final int swap_j = 0;

            Matrix swap = m.swapColumns(swap_i, swap_j);

            for (int row = 0; row < m.rowSize(); row++) {
                assertEquals("Unexpected value for " + desc + " at row " + row + ", swap_i " + swap_i,
                    swap.get(row, swap_i), doubles[row][swap_j], 0.000001d);

                assertEquals("Unexpected value for " + desc + " at row " + row + ", swap_j " + swap_j,
                    swap.get(row, swap_j), doubles[row][swap_i], 0.000001d);
            }

            testInvalidColIndex(() -> m.swapColumns(-1, 0), desc + " negative first swap index");
            testInvalidColIndex(() -> m.swapColumns(0, -1), desc + " negative second swap index");
            testInvalidColIndex(() -> m.swapColumns(m.columnSize(), 0), desc + " too large first swap index");
            testInvalidColIndex(() -> m.swapColumns(0, m.columnSize()), desc + " too large second swap index");
        });
    }

    /** */
    @Test
    public void testSetRow() {
        consumeSampleMatrix((m, desc) -> {


            fillMatrix(m);

            int rowIdx = m.rowSize() / 2;

            double[] newValues = fillArray(m.columnSize());

            m.setRow(rowIdx, newValues);

            for (int col = 0; col < m.columnSize(); col++)
                assertEquals("Unexpected value for " + desc + " at " + col,
                    newValues[col], m.get(rowIdx, col), 0.000001d);

            testInvalidCardinality(() -> m.setRow(rowIdx, new double[m.columnSize() + 1]), desc);
        });
    }

    /** */
    @Test
    public void testSetColumn() {
        consumeSampleMatrix((m, desc) -> {


            fillMatrix(m);

            int colIdx = m.columnSize() / 2;

            double[] newValues = fillArray(m.rowSize());

            m.setColumn(colIdx, newValues);

            for (int row = 0; row < m.rowSize(); row++)
                assertEquals("Unexpected value for " + desc + " at " + row,
                    newValues[row], m.get(row, colIdx), 0.000001d);

            testInvalidCardinality(() -> m.setColumn(colIdx, new double[m.rowSize() + 1]), desc);
        });
    }

    /** */
    @Test
    public void testViewPart() {
        consumeSampleMatrix((m, desc) -> {


            fillMatrix(m);

            int rowOff = m.rowSize() < 3 ? 0 : 1;
            int rows = m.rowSize() < 3 ? 1 : m.rowSize() - 2;
            int colOff = m.columnSize() < 3 ? 0 : 1;
            int cols = m.columnSize() < 3 ? 1 : m.columnSize() - 2;

            Matrix view1 = m.viewPart(rowOff, rows, colOff, cols);
            Matrix view2 = m.viewPart(new int[] {rowOff, colOff}, new int[] {rows, cols});

            String details = desc + " view [" + rowOff + ", " + rows + ", " + colOff + ", " + cols + "]";

            for (int i = 0; i < rows; i++)
                for (int j = 0; j < cols; j++) {
                    assertEquals("Unexpected view1 value for " + details + " at (" + i + "," + j + ")",
                        m.get(i + rowOff, j + colOff), view1.get(i, j), 0.000001d);

                    assertEquals("Unexpected view2 value for " + details + " at (" + i + "," + j + ")",
                        m.get(i + rowOff, j + colOff), view2.get(i, j), 0.000001d);
                }
        });
    }

    /** */
    @Test
    public void testDensity() {
        consumeSampleMatrix((m, desc) -> {


            assertTrue("Unexpected density with threshold 0 for " + desc, m.density(0.0));

            assertFalse("Unexpected density with threshold 1 for " + desc, m.density(1.0));
        });
    }

    /** */
    @Test
    public void testMaxAbsRowSumNorm() {
        consumeSampleMatrix((m, desc) -> {

                fillMatrix(m);

            assertEquals("Unexpected value for " + desc,
                maxAbsRowSumNorm(m), m.maxAbsRowSumNorm(), 0.000001d);
        });
    }

    /** */
    @Test
    public void testAssignRow() {
        consumeSampleMatrix((m, desc) -> {


            fillMatrix(m);

            int rowIdx = m.rowSize() / 2;

            double[] newValues = fillArray(m.columnSize());

            m.assignRow(rowIdx, new DenseLocalOnHeapVector(newValues));

            for (int col = 0; col < m.columnSize(); col++)
                assertEquals("Unexpected value for " + desc + " at " + col,
                    newValues[col], m.get(rowIdx, col), 0.000001d);

            testInvalidCardinality(() -> m.assignRow(rowIdx, new DenseLocalOnHeapVector(m.columnSize() + 1)), desc);
        });
    }

    /** */
    @Test
    public void testAssignColumn() {
        consumeSampleMatrix((m, desc) -> {


            fillMatrix(m);

            int colIdx = m.columnSize() / 2;

            double[] newValues = fillArray(m.rowSize());

            m.assignColumn(colIdx, new DenseLocalOnHeapVector(newValues));

            for (int row = 0; row < m.rowSize(); row++)
                assertEquals("Unexpected value for " + desc + " at " + row,
                    newValues[row], m.get(row, colIdx), 0.000001d);
        });
    }

    /** */
    @Test
    public void testGetRowCol(){
        consumeSampleMatrix((m,desc)-> {
            if (! (m instanceof RandomMatrix))
                for (int i = 0; i < m.rowSize(); i++)
                    for (int j = 0; j < m.columnSize(); j++)
                        m.setX(i, j, i + j);

            for (int i = 0; i < m.rowSize(); i++)
                assertNotNull("Unexpected value for " + desc + " at row " + i, m.getRow(i));

            for (int i = 0; i < m.columnSize(); i++)
                assertNotNull("Unexpected value for " + desc + " at col " + i, m.getCol(i));
        });
    }

    /** */
    private double[] fillArray(int len) {
        double[] newValues = new double[len];

        for (int i = 0; i < newValues.length; i++)
            newValues[i] = newValues.length - i;
        return newValues;
    }

    /** */
    private double maxAbsRowSumNorm(Matrix m) {
        double max = 0.0;

        for (int x = 0; x < m.rowSize(); x++) {
            double sum = 0;

            for (int y = 0; y < m.columnSize(); y++)
                sum += Math.abs(m.getX(x, y));

            if (sum > max)
                max = sum;
        }

        return max;
    }

    /** */
    private void testInvalidRowIndex(Supplier<Matrix> supplier, String desc) {
        try {
            supplier.get();
        }
        catch (RowIndexException | IndexException ie) {
            return;
        }

        fail("Expected exception was not caught for " + desc);
    }

    /** */
    private void testInvalidColIndex(Supplier<Matrix> supplier, String desc) {
        try {
            supplier.get();
        }
        catch (ColumnIndexException | IndexException ie) {
            return;
        }

        fail("Expected exception was not caught for " + desc);
    }

    /** */
    private void testMapMatrixWrongCardinality(Matrix m, String desc) {
        for (int rowDelta : new int[] {-1, 0, 1})
            for (int colDelta : new int[] {-1, 0, 1}) {
                if (rowDelta == 0 && colDelta == 0)
                    continue;

                int rowNew = m.rowSize() + rowDelta;
                int colNew = m.columnSize() + colDelta;

                if (rowNew < 1 || colNew < 1)
                    continue;

                testInvalidCardinality(() -> m.map(new DenseLocalOnHeapMatrix(rowNew, colNew), (m1, m2) -> m1 + m2),
                    desc + " wrong cardinality when mapping to size " + rowNew + "x" + colNew);
            }
    }

    /** */
    private void testInvalidCardinality(Supplier<Object> supplier, String desc) {
        try {
            supplier.get();
        }
        catch (CardinalityException ce) {
            return;
        }

        fail("Expected exception was not caught for " + desc);
    }


    /** */
    private double[][] fillIntAndReturn(Matrix m) {
        double[][] data = new double[m.rowSize()][m.columnSize()];


            for (int i = 0; i < m.rowSize(); i++)
                for (int j = 0; j < m.columnSize(); j++)
                    data[i][j] = i * m.rowSize() + j + 1;

            m.assign(data);

        return data;
    }

    /** */
    private double[][] fillAndReturn(Matrix m) {
        double[][] data = new double[m.rowSize()][m.columnSize()];

            for (int i = 0; i < m.rowSize(); i++)
                for (int j = 0; j < m.columnSize(); j++)
                    data[i][j] = -0.5d + Math.random();

            m.assign(data);
        return data;
    }

    /** */
    private void fillNonSingularMatrix(Matrix m) {
        for (int i = 0; i < m.rowSize(); i++) {
            m.set(i, i, 10);

            for (int j = 0; j < m.columnSize(); j++)
                if (j != i)
                    m.set(i, j, 0.01d);
        }
    }

    /** */
    private void fillMatrix(Matrix m) {
        for (int i = 0; i < m.rowSize(); i++)
            for (int j = 0; j < m.columnSize(); j++)
                m.set(i, j, Math.random());
    }

}
