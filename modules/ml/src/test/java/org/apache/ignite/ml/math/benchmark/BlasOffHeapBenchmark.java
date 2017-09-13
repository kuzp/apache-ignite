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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
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

    /** */
    private static final Map<Integer, Integer> scalRunParams = new HashMap<Integer, Integer>() {{
        put(100,  100_000);
        put(1_000, 100_000);
        put(10_000, 10_000);
        put(100_000, 1_000);
        put(1_000_000, 1_00);
        put(10_000_000, 1_00);
    }};

    /** */
    private static final Map<Integer, Integer> gemmRunParams = new HashMap<Integer, Integer>() {{
        put(16, 16_000);
        put(32, 16_000);
        put(64, 1600);
        put(128, 1600);
        put(256, 1600);
        put(512, 160);
        put(1024, 160);
    }};

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
        scalRunParams.forEach(this::benchmarkScalOnHeap);
    }

    /** */
    @Test
    @Ignore("Benchmark tests are intended only for manual execution")
    public void testScalOffHeap() throws Exception {
        scalRunParams.forEach(this::benchmarkScalOffHeap);
    }

    /** */
    @Test
    @Ignore("Benchmark tests are intended only for manual execution")
    public void testGemmOnHeapSquare() throws Exception {
        gemmRunParams.forEach((size, numRuns) -> benchmarkGemmSquare(size, numRuns, "On heap",
            DenseLocalOnHeapMatrix::new, (a, b, c) -> Blas.gemm(1.0, a, b, 0.0, c)));
    }

    /** */
    @Test
    @Ignore("Benchmark tests are intended only for manual execution")
    public void testGemmOffHeapSquare() throws Exception {
        gemmRunParams.forEach((size, numRuns) -> benchmarkGemmSquare(size, numRuns, "Off heap",
            DenseLocalOffHeapMatrix::new, this::gemmOffHeap));
    }

    /** */
    @Test
    @Ignore("Benchmark tests are intended only for manual execution")
    public void testGemmOnHeapRect() throws Exception {
        gemmRunParams.forEach((size, numRuns) -> benchmarkGemmRect(size, numRuns, "On heap",
            DenseLocalOnHeapMatrix::new, (a, b, c) -> Blas.gemm(1.0, a, b, 0.0, c)));
    }

    /** */
    @Test
    @Ignore("Benchmark tests are intended only for manual execution")
    public void testGemmOffHeapRect() throws Exception {
        gemmRunParams.forEach((size, numRuns) -> benchmarkGemmRect(size, numRuns, "Off heap",
            DenseLocalOffHeapMatrix::new, this::gemmOffHeap));
    }

    /** */
    private interface GemmConsumer<T extends Matrix> {
        /** */
        void accept(T a, T b, T c);
    }

    /** */
    @SuppressWarnings("unchecked")
    private<T extends Matrix> void benchmarkGemmSquare(int size, int numRuns, String tag,
        BiFunction<Integer, Integer, T> newMtx, GemmConsumer<T> gemm) {
        // todo add unit tests for rectangle matrices reproducing benchmark here
        if (size > 1024)
            return; // larger sizes took too long in trial runs

        T a = newMtx.apply(size, size);
        a.assign((i, j) -> i < j - 1 ?  0.0 : (double) (((i % 20) + 1 ) * ((j % 20) + 1)) / 400.0
            + (Objects.equals(i, j) ? 20.0 : 0)); // IMPL NOTE non-singular

        T b = (T)newMtx.apply(size, size).assign(a.inverse());

        T c = newMtx.apply(size, size);

        AtomicReference<Double> sum = new AtomicReference<>(0.0);

        runBenchmarkCode(tag + " " + size, numRuns, () -> {
            gemm.accept(a, b, c);
            sum.accumulateAndGet(c.get(0, 0) + c.get(size - 1, size - 1), (prev, x) -> prev + x);
        });

        Assert.assertNotNull(c.inverse());

        System.out.println("------- " + sum.get());

        a.destroy();
        b.destroy();
        c.destroy();
    }

    /** */
    @SuppressWarnings("unchecked")
    private<T extends Matrix> void benchmarkGemmRect(int size, int numRuns, String tag,
        BiFunction<Integer, Integer, T> newMtx, GemmConsumer<T> gemm) {
        if (size > 1024)
            return; // larger sizes took too long in trial runs

        T a1 = newMtx.apply(size, size);
        a1.assign((i, j) -> i < j - 1 ?  0.0 : (double) (((i % 20) + 1 ) * ((j % 20) + 1)) / 400.0
            + (Objects.equals(i, j) ? 20.0 : 0)); // IMPL NOTE non-singular

        T b1 = (T)newMtx.apply(size, size).assign(a1.inverse());

        int half = size / 2;

        T a = newMtx.apply(size, half);
        a.assign(a1::get);

        T b = newMtx.apply(half, size);
        b.assign(b::get);

        T c = newMtx.apply(size, size);

        AtomicReference<Double> sum = new AtomicReference<>(0.0);

        runBenchmarkCode(tag + " " + size, numRuns,() -> {
            gemm.accept(a, b, c);
            sum.accumulateAndGet(c.get(0, 0) + c.get(size - 1, size - 1), (prev, x) -> prev + x);
        });

        Assert.assertNotNull(sum.get());

        System.out.println("------- " + sum.get());

        a1.destroy();
        b1.destroy();
        c.destroy();
    }

    /** */
    private void gemmOffHeap(DenseLocalOffHeapMatrix a, DenseLocalOffHeapMatrix b, DenseLocalOffHeapMatrix c) {
        BlasOffHeap.getInstance().dgemm("N", "N", a.rowSize(), b.columnSize(), a.columnSize(), 1.0,
            a.ptr(), a.rowSize(), b.ptr(), b.rowSize(), 0.0, c.ptr(), c.rowSize());
    }

    /** */
    private void benchmarkScalOnHeap(int size, int numRuns) {
        Vector v = new DenseLocalOnHeapVector(size);
        VectorContent vc = new VectorContent(v);

        vc.init();

        runBenchmarkCode("On heap " + size, numRuns, () -> {
            Blas.scal(0.5, v);
            Blas.scal(2.0, v);
        });

        assertTrue(vc.verify());
    }

    /** */
    private void benchmarkScalOffHeap(int size, int numRuns) {
        DenseLocalOffHeapVector v = new DenseLocalOffHeapVector(size);
        VectorContent vc = new VectorContent(v);

        vc.init();

        runBenchmarkCode("Off heap " + size, numRuns, () -> {
            blasOffHeap.dscal(v.size(), 0.5, v.ptr(), 1);
            blasOffHeap.dscal(v.size(), 2.0, v.ptr(), 1);
        });

        assertTrue(vc.verify());

        v.destroy();
    }

    /** */
    private void runBenchmarkCode(String benchmarkName, int numRuns, MathBenchmark.BenchmarkCode code) {
        try {
            new MathBenchmark(benchmarkName).outputToConsole().measurementTimes(numRuns).warmUpTimes(1).execute(code);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
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
