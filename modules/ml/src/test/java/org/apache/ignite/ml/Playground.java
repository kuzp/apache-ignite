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

package org.apache.ignite.ml;

import java.util.Arrays;
import java.util.Scanner;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.affinity.rendezvous.RendezvousAffinityFunction;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.internal.util.IgniteUtils;
import org.apache.ignite.ml.dlearn.DLearnContext;
import org.apache.ignite.ml.dlearn.context.cache.CacheDLearnContextFactory;
import org.apache.ignite.ml.dlearn.context.cache.CacheDLearnPartition;
import org.apache.ignite.ml.dlearn.dataset.DatasetMathUtils;
import org.apache.ignite.ml.dlearn.part.LabeledDatasetDLearnPartition;
import org.apache.ignite.ml.dlearn.part.cache.CacheLabeledDatasetDLearnPartitionTransformer;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;

/** */
public class Playground extends GridCommonAbstractTest {
    /** Number of nodes in grid */
    private static final int NODE_COUNT = 4;

    /** */
    private Ignite ignite;

    /** {@inheritDoc} */
    @Override protected void beforeTestsStarted() throws Exception {
        for (int i = 1; i <= NODE_COUNT; i++)
            startGrid(i);
    }

    /** {@inheritDoc} */
    @Override protected void afterTestsStopped() {
        stopAllGrids();
    }

    /**
     * {@inheritDoc}
     */
    @Override protected void beforeTest() throws Exception {
        /* Grid instance. */
        ignite = grid(NODE_COUNT);
        ignite.configuration().setPeerClassLoadingEnabled(true);
        IgniteUtils.setCurrentIgniteName(ignite.configuration().getIgniteInstanceName());
    }

    /** */
    public void testTrainOnBostonDataset() {
        IgniteCache<Integer, double[]> bostonDataset = loadDataset();

        // Initialization of d-learn context, after this step context cache will be created with partitions placed on
        // the same nodes as the upstream Ignite Cache (in this case bostonDataset).
        DLearnContext<CacheDLearnPartition<Integer, double[]>> cacheLearningCtx =
            new CacheDLearnContextFactory<>(ignite, bostonDataset).createContext();

        // Loading of the d-learn context. During this step data will be transferred from the upstream cache to context
        // cache with specified transformation (it will be performed locally because partitions are on the same nodes).
        // In this case for every partition in upstream cache will be created labeled dataset partition and this new
        // partition will be filled with help of specified feature and label extractors.
        DLearnContext<LabeledDatasetDLearnPartition<Double>> datasetLearningCtx = cacheLearningCtx
            .transform(
                new CacheLabeledDatasetDLearnPartitionTransformer<>(
                    (k, v) -> Arrays.copyOfRange(v, 1, v.length),      // specify feature extractor
                    (k, v) -> v[0]                                          // specify label extractor
                ),
                LabeledDatasetDLearnPartition::new
            );

        // Calculation of mean value. This calculation will be performed in map-reduce manner.
        double[] mean = DatasetMathUtils.mean(datasetLearningCtx, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        System.err.println("Mean values : " + Arrays.toString(mean));

        // Calculation of standard deviation. This calculation will be performed in map-reduce manner.
        double[] std = DatasetMathUtils.std(datasetLearningCtx, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        System.err.println("Std values : " + Arrays.toString(std));
    }

    /** */
    private IgniteCache<Integer, double[]> loadDataset() {
        CacheConfiguration<Integer, double[]> cc = new CacheConfiguration<>();
        cc.setName("BOSTON_DATASET");
        cc.setAffinity(new RendezvousAffinityFunction(true, 10));
        IgniteCache<Integer, double[]> cache = ignite.createCache(cc);

        int nvars = 13;
        Scanner scanner = new Scanner(this.getClass().getClassLoader().getResourceAsStream("datasets/regression/boston.csv"));
        int i = 0;
        while (scanner.hasNextLine()) {
            String row = scanner.nextLine();
            int j = 0;
            double[] r = new double[nvars + 1];
            for (String feature : row.split(",")) {
                r[j] = Double.parseDouble(feature);
                j++;
            }
            cache.put(i, r);
            i++;
        }

        return cache;
    }
}