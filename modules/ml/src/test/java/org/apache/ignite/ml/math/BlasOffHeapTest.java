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

    /** Tests 'gemm' operation for off-heap matrices. */
    @Test
    public void testGemm() {
        // C := alpha * A * B + beta * C
        double alpha = 1.0;
        DenseLocalOffHeapMatrix a = new DenseLocalOffHeapMatrix(new double[][] {{10.0, 11.0}, {0.0, 1.0}});
        DenseLocalOffHeapMatrix b = new DenseLocalOffHeapMatrix(new double[][] {{1.0, 0.3}, {0.0, 1.0}});
        double beta = 0.0;
        DenseLocalOffHeapMatrix c = new DenseLocalOffHeapMatrix(new double[][] {{1.0, 2.0}, {2.0, 3.0}});

        Matrix tmp = a.times(b);//.times(alpha).plus(c.times(beta));
        DenseLocalOnHeapMatrix exp
            = (DenseLocalOnHeapMatrix)(new DenseLocalOnHeapMatrix(tmp.rowSize(), tmp.columnSize()).assign(tmp));

        //Blas.gemm(alpha, a, b, beta, c);
        BlasOffHeap.getInstance().dgemm("N", "N", a.rowSize(), b.columnSize(), a.columnSize(), alpha,
            a.ptr(), a.rowSize(), b.ptr(), b.rowSize(), beta, c.ptr(), c.rowSize());

        DenseLocalOnHeapMatrix obtained
            = (DenseLocalOnHeapMatrix)(new DenseLocalOnHeapMatrix(c.rowSize(), c.columnSize()).assign(c));

        Assert.assertEquals(exp, obtained);

        a.destroy();
        b.destroy();
        c.destroy();
    }
}
