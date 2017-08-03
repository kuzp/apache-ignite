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

package org.apache.ignite.ml.trees.trainers.columnbased;

import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.ignite.lang.IgniteBiTuple;
import org.apache.ignite.lang.IgniteUuid;
import org.apache.ignite.ml.math.functions.IgniteFunction;
import org.apache.ignite.ml.math.impls.matrix.SparseDistributedMatrix;
import org.apache.ignite.ml.math.impls.storage.matrix.SparseDistributedMatrixStorage;
import org.jetbrains.annotations.NotNull;

/**
 * Adapter of SparseDistributedMatrix to ColumnDecisionTreeInput.
 * Sparse SparseDistributedMatrix should be in {@see org.apache.ignite.ml.math.StorageConstants#COLUMN_STORAGE_MODE} and
 * should contain samples in rows last position in row being label of this sample.
 */
public class ColumnDecisionTreeMatrixInput extends ColumnDecisionTreeCacheInput<IgniteBiTuple<Integer, IgniteUuid>, Map<Integer, Double>> {
    /**
     * @param m Sparse SparseDistributedMatrix should be in {@see org.apache.ignite.ml.math.StorageConstants#COLUMN_STORAGE_MODE}
     *      containing samples in rows last position in row being label of this sample.
     * @param catFeaturesInfo Information about which features are categorical in form of feature index -> number of
     * categories.
     */
    public ColumnDecisionTreeMatrixInput(SparseDistributedMatrix m, Map<Integer, Integer> catFeaturesInfo) {
        // TODO: Separate labmdas.
        super(((SparseDistributedMatrixStorage)m.getStorage()).cache(),
            new IgniteBiTuple<>(m.columnSize() - 1, ((SparseDistributedMatrixStorage)m.getStorage()).getUUID()),
            valuesMapper(m),
            labels(m),
            keyMapper(m),
            catFeaturesInfo,
            m.columnSize() - 1,
            m.rowSize());
    }

    @NotNull private static IgniteFunction<Map<Integer, Double>, Stream<IgniteBiTuple<Integer, Double>>> valuesMapper(
        SparseDistributedMatrix m) {
        return map -> IntStream.range(0, m.rowSize()).mapToObj(k -> new IgniteBiTuple<>(k, map.getOrDefault(k, 0.0)));
    }

    @NotNull
    private static IgniteFunction<Integer, IgniteBiTuple<Integer, IgniteUuid>> keyMapper(SparseDistributedMatrix m) {
        return i -> new IgniteBiTuple<>(i, ((SparseDistributedMatrixStorage)m.getStorage()).getUUID());
    }

    @NotNull private static IgniteFunction<Map<Integer, Double>, double[]> labels(SparseDistributedMatrix m) {
        return mp -> {
            double[] res = new double[m.rowSize()];

            IntStream.range(0, m.rowSize()).forEach(k -> res[k] = mp.getOrDefault(k, 0.0));
            return res;
        };
    }
}
