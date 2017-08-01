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

import com.zaxxer.sparsebits.SparseBitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.cache.Cache;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.cache.affinity.Affinity;
import org.apache.ignite.cache.affinity.AffinityKeyMapped;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.lang.IgniteBiTuple;
import org.apache.ignite.lang.IgniteUuid;
import org.apache.ignite.ml.Trainer;
import org.apache.ignite.ml.math.Destroyable;
import org.apache.ignite.ml.math.Vector;
import org.apache.ignite.ml.math.functions.IgniteFunction;
import org.apache.ignite.ml.math.functions.IgniteSupplier;
import org.apache.ignite.ml.math.impls.CacheUtils;
import org.apache.ignite.ml.trees.ContinuousRegionInfo;
import org.apache.ignite.ml.trees.ContinuousSplitCalculator;
import org.apache.ignite.ml.trees.models.DecisionTreeModel;
import org.apache.ignite.ml.trees.nodes.DecisionTreeNode;
import org.apache.ignite.ml.trees.nodes.Leaf;
import org.apache.ignite.ml.trees.nodes.SplitNode;
import org.apache.ignite.ml.trees.trainers.columnbased.vectors.CategoricalFeatureVector;
import org.apache.ignite.ml.trees.trainers.columnbased.vectors.ContinuousFeatureVector;
import org.apache.ignite.ml.trees.trainers.columnbased.vectors.FeatureVector;
import org.apache.ignite.ml.trees.trainers.columnbased.vectors.SampleInfo;
import org.apache.ignite.ml.trees.trainers.columnbased.vectors.SplitInfo;
import org.jetbrains.annotations.NotNull;

/**
 * This trainer stores observations as columns and features as rows.
 * Ideas from https://github.com/fabuzaid21/yggdrasil are used here.
 */
public class ColumnDecisionTreeTrainer<D extends ContinuousRegionInfo> implements
    Trainer<DecisionTreeModel, ColumnDecisionTreeInput>, Destroyable {
    /** Function used to assign a value to a region. */
    private final IgniteFunction<DoubleStream, Double> regCalc;

    /** Function used to calculate impurity in regions used by categorical features. */
    private final IgniteFunction<DoubleStream, Double> catImpCalc;

    /** Cache used for storing data for training. */
    private IgniteCache<RegionKey, Region> cache;

    /** Minimal information gain. */
    private static double MIN_INFO_GAIN = 1E-10;

    /** Maximal depth of the decision tree. */
    private int maxDepth;

    /** Name of cache which is used for storing data for training. */
    public static final String COLUMN_DECISION_TREE_TRAINER_CACHE_NAME = "COLUMN_DECISION_TREE_TRAINER_CACHE_NAME";

    /** Calculator used for calculations of split on continuous features. */
    private ContinuousSplitCalculator<D> calc;

    /**
     * @param maxDepth Maximal depth of the decision tree.
     * @param calc Calculator used for calculations of split on continuous features.
     * @param catImpCalc Function used to calculate impurity in regions used by categorical features.
     * @param regCalc Function used to assign a value to a region.
     */
    public ColumnDecisionTreeTrainer(int maxDepth, ContinuousSplitCalculator<D> calc,
        IgniteFunction<DoubleStream, Double> catImpCalc, IgniteFunction<DoubleStream, Double> regCalc) {
        this.maxDepth = maxDepth;
        this.catImpCalc = catImpCalc;
        this.calc = calc;
        this.regCalc = regCalc;
    }

    /** Utility class used to get index of feature by which split is done and split info. */
    private static class IndexAndSplitInfo {
        /** Index of feature by which split is done. */
        private int featureIdx;

        /** Split information. */
        private SplitInfo info;

        /**
         * @param featureIdx Index of feature by which split is done.
         * @param info Split information.
         */
        public IndexAndSplitInfo(int featureIdx, SplitInfo info) {
            this.featureIdx = featureIdx;
            this.info = info;
        }
    }

    /**
     * Class used as key in the internal cache of this trainer.
     */
    private static class FeatureVectorKey {
        /** Affinity key used to guarantee internal cache entry collocation with entries from trainer input. */
        @AffinityKeyMapped
        private Object parentRowKey;

        /** Key of feature row. */
        private IgniteBiTuple<Integer, IgniteUuid> rowKey;

        /**
         * @param parentRowKey Affinity key used to guarantee internal cache entry collocation with entries from trainer
         * input.
         * @param rowKey Key of feature row.
         */
        public FeatureVectorKey(Object parentRowKey,
            IgniteBiTuple<Integer, IgniteUuid> rowKey) {
            this.parentRowKey = parentRowKey;
            this.rowKey = rowKey;
        }

        /** {@inheritDoc} */
        @Override public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            FeatureVectorKey key = (FeatureVectorKey)o;

            if (parentRowKey != null ? !parentRowKey.equals(key.parentRowKey) : key.parentRowKey != null)
                return false;
            return rowKey != null ? rowKey.equals(key.rowKey) : key.rowKey == null;
        }

        /** {@inheritDoc} */
        @Override public int hashCode() {
            int res = parentRowKey != null ? parentRowKey.hashCode() : 0;
            res = 31 * res + (rowKey != null ? rowKey.hashCode() : 0);
            return res;
        }
    }

    private static class RegionKey {
        /** Affinity key used to guarantee internal cache entry collocation with entries from trainer input. */
        @AffinityKeyMapped
        private Object parentRowKey;

        private int featureIdx;

        private int regIdx;

        private IgniteUuid trainingUUID;

        public RegionKey(int featureIdx, int regIdx, IgniteUuid trainingUUID, Object parentRowKey) {
            this.parentRowKey = parentRowKey;
            this.featureIdx = featureIdx;
            this.regIdx = regIdx;
            this.trainingUUID = trainingUUID;
        }

        public int featureIdx() {
            return featureIdx;
        }

        public int regionIdx() {
            return regIdx;
        }

        public IgniteUuid trainingUUID() {
            return trainingUUID;
        }

        public Object parentRowKey() {
            return parentRowKey;
        }

        @Override public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            RegionKey key = (RegionKey)o;

            if (featureIdx != key.featureIdx)
                return false;
            if (regIdx != key.regIdx)
                return false;
            if (parentRowKey != null ? !parentRowKey.equals(key.parentRowKey) : key.parentRowKey != null)
                return false;
            return trainingUUID != null ? trainingUUID.equals(key.trainingUUID) : key.trainingUUID == null;
        }

        @Override public int hashCode() {
            int result = parentRowKey != null ? parentRowKey.hashCode() : 0;
            result = 31 * result + featureIdx;
            result = 31 * result + regIdx;
            result = 31 * result + (trainingUUID != null ? trainingUUID.hashCode() : 0);
            return result;
        }
    }

    /**
     * Utility class used to build decision tree. Basically it is pointer to leaf node.
     */
    private static class TreeTip {
        /** */
        private Consumer<DecisionTreeNode> leafSetter;

        /** */
        private int depth;

        /** */
        public TreeTip(Consumer<DecisionTreeNode> leafSetter, int depth) {
            this.leafSetter = leafSetter;
            this.depth = depth;
        }
    }

    /**
     * Utility class used as decision tree root node.
     */
    private static class RootNode implements DecisionTreeNode {
        /** */
        private DecisionTreeNode s;

        /** {@inheritDoc} */
        @Override public double process(Vector v) {
            return s.process(v);
        }

        /** */
        public void setSplit(DecisionTreeNode s) {
            this.s = s;
        }
    }

    /** {@inheritDoc} */
    @Override public DecisionTreeModel train(ColumnDecisionTreeInput i) {
        double[] labels = i.labels();

        cache = newCache();
        IgniteUuid uuid = new IgniteUuid();

        CacheUtils.bcast(cache.getName(), () -> {
            Ignite ignite = Ignition.localIgnite();
            IgniteCache<RegionKey, Region> targetCache = ignite.getOrCreateCache(COLUMN_DECISION_TREE_TRAINER_CACHE_NAME);
            Affinity<RegionKey> targetAffinity = ignite.affinity(COLUMN_DECISION_TREE_TRAINER_CACHE_NAME);

            ClusterNode locNode = ignite.cluster().localNode();

            targetAffinity.
                mapKeysToNodes(IntStream.range(0, i.featuresCount()).
                    mapToObj(idx -> getCacheKey(idx, 0, uuid, i.affinityKey(idx))).
                    collect(Collectors.toSet())).getOrDefault(locNode, Collections.emptyList()).
                stream().
                forEach(k -> {
                    FeatureVector vec;
                    int featureIdx = k.featureIdx();
                    vec = vector(i, featureIdx);
                    targetCache.put(k, vec.createInitialRegion(getSamples(i.values(featureIdx), labels)));
                });

            return null;
        });

        return doTrain(i.featuresCount(), i, uuid);
    }

    private SampleInfo[] getSamples(Stream<IgniteBiTuple<Integer, Double>> values, double[] labels) {
        SampleInfo[] res = new SampleInfo[labels.length];

        values.forEach(v -> res[v.get1()] = new SampleInfo(labels[v.get1()], v.get2(), v.get1()));

        return res;
    }

    private RegionKey getCacheKey(int featureIdx, int regIdx, , IgniteUuid uuid, Object aff) {
        return new RegionKey(featureIdx, regIdx, uuid, aff);
    }

    /** */
    @NotNull private DecisionTreeModel doTrain(int size, ColumnDecisionTreeInput input, IgniteUuid uuid) {
        RootNode root = new RootNode();

        // List containing setters of leaves of the tree
        List<TreeTip> tips = new LinkedList<>();
        tips.add(new TreeTip(root::setSplit, 0));

        int curDepth = 0;
        int regsCnt = 1;

        // TODO: IGNITE-5893 Currently if the best split makes tree deeper than max depth process will be terminated, but actually we should
        // only stop when *any* improving split makes tree deeper than max depth. Can be fixed if we will store which
        // regions cannot be split more and split only those that can.
        while (curDepth < maxDepth) {
            // Keys of all regions.
            int rc = regsCnt;
            IgniteSupplier<Set<RegionKey>> keysGen = () -> IntStream.range(0, size).boxed().flatMap(fIdx ->
                IntStream.range(0, rc).boxed().map(regIdx -> getCacheKey(fIdx, regIdx, uuid, input.affinityKey(fIdx)))).collect(Collectors.toSet());

            // Get locally (for node) optimal (by information gain) splits.
            long before = System.currentTimeMillis();

            List<IndexAndSplitInfo> splits = CacheUtils.sparseFold(cache.getName(),
                (Cache.Entry<RegionKey, Region> e, List<IndexAndSplitInfo> lst) -> {
                    int featIdx = e.getKey().featureIdx();
                    int regIdx = e.getKey().regionIdx();

                    FeatureVector vector = vector(input, featIdx);

                    SplitInfo locallyBest = vector.findBestSplit(e.getValue(), regIdx);
                    if (locallyBest != null)
                        lst.add(new IndexAndSplitInfo(featIdx, locallyBest));
                    return lst;
                },
                keysGen,
                (infos, infos2) -> {
                    List<IndexAndSplitInfo> res = new LinkedList<>();
                    res.addAll(infos);
                    res.addAll(infos2);
                    return res;
                },
                LinkedList::new,
                null,
                null,
                0,
                true
            );
            long total = System.currentTimeMillis() - before;

            // Find globally optimal split.
            IndexAndSplitInfo best = splits.stream().max(Comparator.comparingDouble(o -> o.info.infoGain())).orElse(null);

            if (best != null && best.info.infoGain() > MIN_INFO_GAIN) {
                regsCnt++;
                System.out.println("Globally best: " + best.info + " time: " + total);
                // Request bitset for split region.
                SparseBitSet bs = cache.invoke(getCacheKey(best.featureIdx, input.affinityKey(best.featureIdx)), (entry, arguments) -> entry.getValue().calculateOwnershipBitSet(best.info));

                // Update decision tree.
                int ind = best.info.regionIndex();
                SplitNode sn = best.info.createSplitNode(best.featureIdx);

                TreeTip tipToSplit = tips.get(ind);
                tipToSplit.leafSetter.accept(sn);
                tipToSplit.leafSetter = sn::setLeft;
                int d = tipToSplit.depth++;
                tips.add(ind + 1, new TreeTip(sn::setRight, d));

                if (d > curDepth) {
                    curDepth = d;
                    System.out.println("Depth: " + curDepth);
                    System.out.println("Cache size: " + cache.size(CachePeekMode.PRIMARY));
                }

                Map<Integer, Integer> catFeaturesInfo = input.catFeaturesInfo();

                before = System.currentTimeMillis();
                // Perform split on all feature vectors.
                CacheUtils.update(cache.getName(),
                    (Cache.Entry<FeatureVectorKey, FeatureVector> e) -> {

                        FeatureVectorKey k = e.getKey();
                        FeatureVector v = e.getValue();

                        if ((!catFeaturesInfo.containsKey(k.rowKey.get1()) && !catFeaturesInfo.containsKey(best.featureIdx)))
                            v.performSplit(bs, ind, best.info.leftData(), best.info.rightData());
                        else
                            v.performSplitGeneric(bs, ind, best.info.leftData(), best.info.rightData());
                    },
                    keysGen);

                System.out.println("Update took " + (System.currentTimeMillis() - before));
            }
            else
                break;
        }

        // Ask to calculate values in regions.
        double[] vals = cache.invoke(getCacheKey(0, input.affinityKey(0)), (mutableEntry, objects) -> mutableEntry.getValue().calculateRegions(regCalc));

        int i = 0;
        for (TreeTip tip : tips) {
            tip.leafSetter.accept(new Leaf(vals[i]));
            i++;
        }

        cache.removeAll(
            IntStream.range(0, input.featuresCount()).mapToObj(j -> getCacheKey(j, input.affinityKey(j))).collect(Collectors.toSet()));

        return new DecisionTreeModel(root.s);
    }

    /**
     * Create new cache for ColumnDecisionTreeTrainer if needed.
     */
    private IgniteCache<RegionKey, Region> newCache() {
        CacheConfiguration<RegionKey, Region> cfg = new CacheConfiguration<>();

        // Write to primary.
        cfg.setWriteSynchronizationMode(CacheWriteSynchronizationMode.PRIMARY_SYNC);

        // Atomic transactions only.
        cfg.setAtomicityMode(CacheAtomicityMode.ATOMIC);

        // No eviction.
        cfg.setEvictionPolicy(null);

        // No copying of values.
        cfg.setCopyOnRead(false);

        // Cache is partitioned.
        cfg.setCacheMode(CacheMode.PARTITIONED);

        cfg.setBackups(0);

        cfg.setName(COLUMN_DECISION_TREE_TRAINER_CACHE_NAME);

        return Ignition.localIgnite().getOrCreateCache(cfg);
    }

    /** {@inheritDoc} */
    @Override public void destroy() {
        cache.destroy();
    }

//    /**
//     * Get internal cache key by feature index and affinity key of input entry.
//     *
//     * @param featureIdx Feature index.
//     * @param affinityKey Affinity key of input entry.
//     * @return Internal cache key by feature index and affinity key of input entry.
//     */
//    private FeatureVectorKey getCacheKey(int featureIdx, Object affinityKey) {
//        return new FeatureVectorKey(affinityKey, new IgniteBiTuple<>(featureIdx, uuid));
//    }

    private FeatureVector vector(ColumnDecisionTreeInput i, int featureIdx) {
        return i.catFeaturesInfo().containsKey(featureIdx) ?
            new CategoricalFeatureVector(catImpCalc, i.catFeaturesInfo().get(featureIdx)) :
            new ContinuousFeatureVector<>(calc);
    }
}
