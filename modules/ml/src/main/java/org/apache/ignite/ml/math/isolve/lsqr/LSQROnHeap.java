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

package org.apache.ignite.ml.math.isolve.lsqr;

import com.github.fommil.netlib.BLAS;
import java.util.Arrays;
import org.apache.ignite.ml.dataset.Dataset;
import org.apache.ignite.ml.dataset.DatasetBuilder;
import org.apache.ignite.ml.dataset.PartitionDataBuilder;
import org.apache.ignite.ml.math.isolve.LinSysPartitionDataOnHeap;

/**
 * Distributed implementation of LSQR algorithm based on.
 */
public class LSQROnHeap<K, V> extends AbstractLSQR implements AutoCloseable {

    private final Dataset<LSQRPartitionContext, LinSysPartitionDataOnHeap> dataset;

    /**
     * Constructs a new instance of OnHeap LSQR algorithm implementation.
     *
     * @param datasetBuilder Dataset builder.
     * @param partDataBuilder Partition data builder.
     */
    public LSQROnHeap(DatasetBuilder<K, V> datasetBuilder,
        PartitionDataBuilder<K, V, LSQRPartitionContext, LinSysPartitionDataOnHeap> partDataBuilder) {
        this.dataset = datasetBuilder.build(
            (upstream, upstreamSize) -> new LSQRPartitionContext(),
            partDataBuilder
        );
    }

    /** {@inheritDoc} */
    @Override protected double bnorm() {
        double res = dataset.computeWithCtx((ctx, data) -> {
            ctx.setU(Arrays.copyOf(data.getY(), data.getY().length));
            return BLAS.getInstance().dnrm2(data.getY().length, data.getY(), 1);
        }, (a, b) -> a == null ? b : Math.sqrt(a * a + b * b));
        return res;
    }

    /** {@inheritDoc} */
    @Override protected double beta(double[] x, double alfa, double beta) {
        double bb = dataset.computeWithCtx((ctx, data) -> {
            BLAS.getInstance().dgemv("N", data.getRows(), data.getCols(), alfa, data.getX(), data.getRows(), x, 1, beta, ctx.getU(), 1);
            return BLAS.getInstance().dnrm2(ctx.getU().length, ctx.getU(), 1);
        }, (a, b) -> a == null ? b : Math.sqrt(a * a + b * b));
        return bb;
    }

    /** {@inheritDoc} */
    @Override protected double[] iter(double bnorm, double[] target) {
        double[] res = dataset.computeWithCtx((ctx, data) -> {
            BLAS.getInstance().dscal(ctx.getU().length, 1 / bnorm, ctx.getU(), 1);
            double[] v = new double[data.getCols()];
            BLAS.getInstance().dgemv("T", data.getRows(), data.getCols(), 1.0, data.getX(), data.getRows(), ctx.getU(), 1, 0, v, 1);
            return v;
        }, (a, b) -> {
            if (a == null)
                return b;
            else {
                BLAS.getInstance().daxpy(a.length, 1.0, a, 1, b, 1);
                return b;
            }
        });
        BLAS.getInstance().daxpy(res.length, 1.0, res, 1, target, 1);
        return target;
    }

    /**
     * Returns number of columns in dataset.
     *
     * @return number of columns
     */
    @Override protected int getColumns() {
        return dataset.compute(LinSysPartitionDataOnHeap::getCols, (a, b) -> a == null ? b : a);
    }

    /** {@inheritDoc} */
    @Override public void close() throws Exception {
        dataset.close();
    }
}