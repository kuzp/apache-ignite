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

package org.apache.ignite.ml.math;

import org.apache.ignite.ml.math.impls.matrix.DenseLocalOffHeapMatrix;
import org.apache.ignite.ml.math.impls.matrix.DenseLocalOnHeapMatrix;
import org.apache.ignite.ml.math.impls.vector.DenseLocalOffHeapVector;
import org.apache.ignite.ml.math.impls.vector.DenseLocalOnHeapVector;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/** Tests for BLAS off-heap operations. */
public class BlasOffHeapTest {
    /** Test off-heap blas availability. Todo remove later or make it OS-independent. */
    @Test
    public void testBlasOffHeap() {
        Assert.assertNotNull("Unexpected null BlasOffHeap instance.", BlasOffHeap.getInstance());
    }

    /** Test off-heap 'daxpy' operation for two vectors. */
    @Test
    public void testDaxpyArrayArray() {
        DenseLocalOffHeapVector y = new DenseLocalOffHeapVector(new double[] {1.0, 2.0});
        double a = 2.0;
        DenseLocalOffHeapVector x = new DenseLocalOffHeapVector(new double[] {1.0, 2.0});

        Vector exp = x.times(a).plus(y);
        BlasOffHeap.getInstance().daxpy(x.size(), a, x.ptr(), 1, y.ptr(), 1);

        Assert.assertEquals(y, exp);

        x.destroy();
        y.destroy();
    }

    /** Test 'dscal' operation for a vector. */
    @Test
    public void testScal() {
        double[] data = new double[] {1.0, 1.0};
        double alpha = 2.0;

        DenseLocalOffHeapVector v = new DenseLocalOffHeapVector(data);
        Vector exp = new DenseLocalOnHeapVector(data, true).times(alpha);
        BlasOffHeap.getInstance().dscal(v.size(), alpha, v.ptr(), 1);

        Assert.assertEquals(new DenseLocalOnHeapVector(v.size()).assign(v), exp);

        v.destroy();
    }

    /** Tests 'gemm' operation for off-heap square matrices. */
    @Test
    public void testGemmSquare() {
        // C := alpha (1.0) * A * B + beta (0.0) * C
        DenseLocalOffHeapMatrix a = new DenseLocalOffHeapMatrix(new double[][] {{10.0, 11.0}, {0.0, 1.0}});
        DenseLocalOffHeapMatrix b = new DenseLocalOffHeapMatrix(new double[][] {{1.0, 0.3}, {0.0, 1.0}});
        DenseLocalOffHeapMatrix c = new DenseLocalOffHeapMatrix(new double[][] {{1.0, 2.0}, {2.0, 3.0}});

        Matrix tmp = a.times(b);//.times(alpha).plus(c.times(beta));
        DenseLocalOnHeapMatrix exp
            = (DenseLocalOnHeapMatrix)(new DenseLocalOnHeapMatrix(tmp.rowSize(), tmp.columnSize()).assign(tmp));

        DenseLocalOnHeapMatrix obtained = gemmOffHeap(a, b, c);

        Assert.assertEquals(exp, obtained);

        a.destroy();
        b.destroy();
        c.destroy();
    }

    /** Tests 'gemm' operation for large off-heap square matrices that won't fit into JVM memory. */
    @Test
    @Ignore("If needed manually run this stress test")
    public void testGemmSquareLarge() {
        // C := alpha (1.0) * A * B + beta (0.0) * C
        int largeSize = 23_000; // IMPL NOTE use smaller values for smoke testing, like 10, 100, 1000
        DenseLocalOffHeapMatrix a = new DenseLocalOffHeapMatrix(largeSize, largeSize);
        DenseLocalOffHeapMatrix b = new DenseLocalOffHeapMatrix(largeSize, largeSize);
        DenseLocalOffHeapMatrix c = new DenseLocalOffHeapMatrix(largeSize, largeSize);

        a.assign((row, col) -> row.equals(col) ? 2.0 : 0.0);

        b.assign((row, col) -> row.equals(col) ? 2.0 : 0.0);

        dgemmOffHeap(a, b, c);

        Assert.assertEquals(4, c.get(largeSize - 1, largeSize - 1), 0.0);

        a.destroy();
        b.destroy();
        c.destroy();
    }

    /** Tests 'gemm' operation for off-heap non-square matrices. */
    @Test
    public void testGemmRect() {
        // C := alpha (1.0) * A * B + beta (0.0) * C
        DenseLocalOffHeapMatrix a = new DenseLocalOffHeapMatrix(new double[][] {{10.0, 11.0}, {0.0, 1.0}, {0.0, 1.0}});
        DenseLocalOffHeapMatrix b = new DenseLocalOffHeapMatrix(new double[][] {{1.0, 0.3, 1.0}, {0.0, 1.0, 1.0}});
        DenseLocalOffHeapMatrix c = new DenseLocalOffHeapMatrix(new double[][] {{0.0, 0.0, 0.0}, {0.0, 0.0, 0.0},
            {0.0, 0.0, 0.0}});

        Matrix tmp = a.times(b);//.times(alpha).plus(c.times(beta));
        DenseLocalOnHeapMatrix exp
            = (DenseLocalOnHeapMatrix)(new DenseLocalOnHeapMatrix(tmp.rowSize(), tmp.columnSize()).assign(tmp));

        DenseLocalOnHeapMatrix obtained = gemmOffHeap(a, b, c);

        Assert.assertEquals(exp, obtained);

        a.destroy();
        b.destroy();
        c.destroy();
    }

    /** Tests (reference) 'gemm' operation for dense matrix A, dense matrix B and dense matrix C. */
    @Test
    public void testGemmDenseDenseDenseRect() {
        // C := alpha * A * B + beta * C
        double alpha = 1.0;
        DenseLocalOnHeapMatrix a = new DenseLocalOnHeapMatrix(new double[][] {{10.0, 11.0}, {0.0, 1.0}, {0.0, 1.0}});
        DenseLocalOnHeapMatrix b = new DenseLocalOnHeapMatrix(new double[][] {{1.0, 0.3, 1.0}, {0.0, 1.0, 1.0}});
        double beta = 0.0;
        DenseLocalOnHeapMatrix c = new DenseLocalOnHeapMatrix(new double[][] {{0.0, 0.0, 0.0}, {0.0, 0.0, 0.0},
            {0.0, 0.0, 0.0}});

        DenseLocalOnHeapMatrix exp = (DenseLocalOnHeapMatrix)a.times(b);//.times(alpha).plus(c.times(beta));

        Blas.gemm(alpha, a, b, beta, c);

        Assert.assertEquals(exp, c);
    }

    /** */
    private DenseLocalOnHeapMatrix gemmOffHeap(DenseLocalOffHeapMatrix a, DenseLocalOffHeapMatrix b,
        DenseLocalOffHeapMatrix c) {
        dgemmOffHeap(a, b, c);

        return (DenseLocalOnHeapMatrix)(new DenseLocalOnHeapMatrix(c.rowSize(), c.columnSize()).assign(c));
    }

    /** */
    private void dgemmOffHeap(DenseLocalOffHeapMatrix a, DenseLocalOffHeapMatrix b,
        DenseLocalOffHeapMatrix c) {
        BlasOffHeap.getInstance().dgemm("N", "N", a.rowSize(), b.columnSize(), a.columnSize(), 1.0,
            a.ptr(), a.rowSize(), b.ptr(), b.rowSize(), 0.0, c.ptr(), c.rowSize());
    }
}
