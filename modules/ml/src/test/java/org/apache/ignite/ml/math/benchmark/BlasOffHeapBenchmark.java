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

package org.apache.ignite.ml.math.benchmark;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.apache.ignite.ml.math.Blas;
import org.apache.ignite.ml.math.BlasOffHeap;
import org.apache.ignite.ml.math.Matrix;
import org.apache.ignite.ml.math.Vector;
import org.apache.ignite.ml.math.impls.matrix.DenseLocalOffHeapMatrix;
import org.apache.ignite.ml.math.impls.matrix.DenseLocalOnHeapMatrix;
import org.apache.ignite.ml.math.impls.vector.DenseLocalOffHeapVector;
import org.apache.ignite.ml.math.impls.vector.DenseLocalOnHeapVector;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/** */
public class BlasOffHeapBenchmark {
    /** */
    private static final BlasOffHeap blasOffHeap = BlasOffHeap.getInstance();

    /** Test Blas availability necessary for this benchmark. */
    @Test
    @Ignore("Benchmark tests are intended only for manual execution")
    public void testBlasOffHeap() throws ClassNotFoundException {
        Assert.assertNotNull("Unexpected null BlasOffHeap instance.", BlasOffHeap.getInstance());

        Assert.assertNotNull("Unexpected null native netlib Blas instance.",
            Class.forName("com.github.fommil.netlib.NativeSystemBLAS"));
    }

    /** */
    @Test
    @Ignore("Benchmark tests are intended only for manual execution")
    public void testScalOnHeap() throws Exception {
        benchmarkScalOnHeap(100, 100_000);
        benchmarkScalOnHeap(1_000, 100_000);
        benchmarkScalOnHeap(10_000, 10_000);
        benchmarkScalOnHeap(100_000, 1_000);
        benchmarkScalOnHeap(1_000_000, 1_00);
        benchmarkScalOnHeap(10_000_000, 1_00);
    }

    /** */
    @Test
    @Ignore("Benchmark tests are intended only for manual execution")
    public void testScalOffHeap() throws Exception {
        benchmarkScalOffHeap(100, 100_000);
        benchmarkScalOffHeap(1_000, 100_000);
        benchmarkScalOffHeap(10_000, 10_000);
        benchmarkScalOffHeap(100_000, 1_000);
        benchmarkScalOffHeap(1_000_000, 1_00);
        benchmarkScalOffHeap(10_000_000, 1_00);
    }

    /** */
    @Test
    //@Ignore("Benchmark tests are intended only for manual execution")
    public void testGemmOnHeap() throws Exception {
        benchmarkGemmOnHeap(16, 16_000);
        benchmarkGemmOnHeap(32, 16_000);
        benchmarkGemmOnHeap(64, 1600);
        benchmarkGemmOnHeap(128, 1600);
        benchmarkGemmOnHeap(256, 1600);
        benchmarkGemmOnHeap(512, 160);
        benchmarkGemmOnHeap(1024, 160);
    }

    /** */
    @Test
    //@Ignore("Benchmark tests are intended only for manual execution")
    public void testGemmOffHeap() throws Exception {
        benchmarkGemmOffHeap(16, 16_000);
        benchmarkGemmOffHeap(32, 16_000);
        benchmarkGemmOffHeap(64, 1600);
        benchmarkGemmOffHeap(128, 1600);
        benchmarkGemmOffHeap(256, 1600);
        benchmarkGemmOffHeap(512, 160);
        benchmarkGemmOffHeap(1024, 160);
    }

    /** */
    private void benchmarkGemmOnHeap(int size, int numRuns) throws Exception {
        benchmarkGemm(size, numRuns, "On heap",
            sz -> new DenseLocalOnHeapMatrix(sz, sz),
            (a, b, c) -> Blas.gemm(1.0, a, b, 0.0, c));
    }

    /** */
    private void benchmarkGemmOffHeap(int size, int numRuns) throws Exception {
        benchmarkGemm(size, numRuns, "Off heap",
            sz -> new DenseLocalOffHeapMatrix(sz, sz),
            this::gemmOffHeap);
    }

    /** */
    private interface GemmConsumer<T extends Matrix> {
        /** */
        void accept(T a, T b, T c);
    }

    /** */
    @SuppressWarnings("unchecked")
    private<T extends Matrix> void benchmarkGemm(int size, int numRuns, String tag, Function<Integer, T> newMtx,
        GemmConsumer<T> gemm) throws Exception {
        if (size > 1024)
            return; // larger sizes took too long in trial runs

        T m = newMtx.apply(size);
        m.assign((i, j) -> i < j - 1 ?  0.0 : (double) (((i % 20) + 1 ) * ((j % 20) + 1)) / 400.0
            + (Objects.equals(i, j) ? 20.0 : 0)); // IMPL NOTE non-singular

        T inv = (T)newMtx.apply(size).assign(m.inverse());

        T tmp = newMtx.apply(size);
        T c = newMtx.apply(size);

        AtomicReference<Double> sum = new AtomicReference<>(0.0);

        new MathBenchmark(tag + " " + size).outputToConsole().measurementTimes(numRuns).warmUpTimes(1)
            .execute(() -> {
                gemm.accept(m, inv, tmp);
                gemm.accept(tmp, m, c);
                sum.accumulateAndGet(c.get(0, 0) + c.get(size - 1, size - 1), (prev, x) -> prev + x);
            });

        Assert.assertNotNull(tmp.inverse());

        System.out.println("------- " + sum.get());

        m.destroy();
        inv.destroy();
        tmp.destroy();
        c.destroy();
    }

    /** */
    private void gemmOffHeap(DenseLocalOffHeapMatrix a, DenseLocalOffHeapMatrix b, DenseLocalOffHeapMatrix c) {
        BlasOffHeap.getInstance().dgemm("N", "N", a.rowSize(), b.columnSize(), a.columnSize(), 1.0,
            a.ptr(), a.rowSize(), b.ptr(), b.rowSize(), 0.0, c.ptr(), c.rowSize());

    }

    /** */
    private void benchmarkScalOnHeap(int size, int numRuns) throws Exception {
        Vector v = new DenseLocalOnHeapVector(size);
        VectorContent vc = new VectorContent(v);

        vc.init();

        new MathBenchmark("On heap " + size).outputToConsole().measurementTimes(numRuns).warmUpTimes(1)
            .execute(() -> {
                Blas.scal(0.5, v);
                Blas.scal(2.0, v);
            });

        assertTrue(vc.verify());
    }

    /** */
    private void benchmarkScalOffHeap(int size, int numRuns) throws Exception {
        DenseLocalOffHeapVector v = new DenseLocalOffHeapVector(size);
        VectorContent vc = new VectorContent(v);

        vc.init();

        new MathBenchmark("Off heap " + size).outputToConsole().measurementTimes(numRuns).warmUpTimes(1)
            .execute(() -> {
                blasOffHeap.dscal(v.size(), 0.5, v.ptr(), 1);
                blasOffHeap.dscal(v.size(), 2.0, v.ptr(), 1);
            });

        assertTrue(vc.verify());

        v.destroy();
    }

    /** */
    private static class VectorContent {
        /** */
        private final Vector v;

        /** */
        VectorContent(Vector v) {
            this.v = v;
        }

        /** */
        void init() {
            v.assign((i) -> i);
        }

        /** */
        boolean verify() {
            return (v.assign((i) -> Math.abs(v.get(i) - i))).getLengthSquared() < 1.0;
        }
    }
}
