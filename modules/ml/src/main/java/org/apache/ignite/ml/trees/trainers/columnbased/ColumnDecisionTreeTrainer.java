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

import java.util.*;
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
import org.apache.ignite.internal.processors.cache.CacheEntryImpl;
import org.apache.ignite.lang.IgniteBiTuple;
import org.apache.ignite.lang.IgniteUuid;
import org.apache.ignite.ml.Trainer;
import org.apache.ignite.ml.math.Destroyable;
import org.apache.ignite.ml.math.Vector;
import org.apache.ignite.ml.math.functions.Functions;
import org.apache.ignite.ml.math.functions.IgniteFunction;
import org.apache.ignite.ml.math.functions.IgniteSupplier;
import org.apache.ignite.ml.math.impls.CacheUtils;
import org.apache.ignite.ml.trees.ContinuousRegionInfo;
import org.apache.ignite.ml.trees.ContinuousSplitCalculator;
import org.apache.ignite.ml.trees.models.DecisionTreeModel;
import org.apache.ignite.ml.trees.nodes.DecisionTreeNode;
import org.apache.ignite.ml.trees.nodes.Leaf;
import org.apache.ignite.ml.trees.nodes.SplitNode;
import org.apache.ignite.ml.trees.trainers.columnbased.vectors.CategoricalFeatureProcessor;
import org.apache.ignite.ml.trees.trainers.columnbased.vectors.ContinuousFeatureProcessor;
import org.apache.ignite.ml.trees.trainers.columnbased.vectors.FeatureProcessor;
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
    private IgniteCache<RegionKey, RegionProjection> cache;

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

        public IndexAndSplitInfo() {
            this.featureIdx = -1;
        }

        @Override public String toString() {
            return "IndexAndSplitInfo [featureIdx=" + featureIdx + ", info=" + info + ']';
        }
    }

    private static class RegionKey {
        /** Affinity key used to guarantee internal cache entry collocation with entries from trainer input. */
        @AffinityKeyMapped
        private Object parentRowKey;

        /** Feature index. */
        private int featureIdx;

        /** Region index. */
        private int regIdx;

        /** Training UUID. */
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

        /** {@inheritDoc} */
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

        /** {@inheritDoc} */
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
        IgniteUuid uuid = IgniteUuid.randomUuid();

        CacheUtils.bcast(cache.getName(), () -> {
            Ignite ignite = Ignition.localIgnite();
            IgniteCache<RegionKey, RegionProjection> targetCache = ignite.getOrCreateCache(COLUMN_DECISION_TREE_TRAINER_CACHE_NAME);
            Affinity<RegionKey> targetAffinity = ignite.affinity(COLUMN_DECISION_TREE_TRAINER_CACHE_NAME);

            ClusterNode locNode = ignite.cluster().localNode();

            targetAffinity.
                mapKeysToNodes(IntStream.range(0, i.featuresCount()).
                    mapToObj(idx -> getCacheKey(idx, 0, uuid, i.affinityKey(idx))).
                    collect(Collectors.toSet())).getOrDefault(locNode, Collections.emptyList()).
                stream().
                forEach(k -> {
                    FeatureProcessor vec;
                    int featureIdx = k.featureIdx();
                    vec = vectorProcessor(i, featureIdx);
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

    private RegionKey getCacheKey(int featureIdx, int regIdx, IgniteUuid uuid, Object aff) {
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

        Map<Integer, SplitInfo> optimalForFeature = getOptimals(input, allKeys(input, regsCnt, uuid));

        // TODO: IGNITE-5893 Currently if the best split makes tree deeper than max depth process will be terminated, but actually we should
        // only stop when *any* improving split makes tree deeper than max depth. Can be fixed if we will store which
        // regions cannot be split more and split only those that can.
        while (true) {
            // Keys of all regions.
            int rc = regsCnt;
            System.out.println("Regs cnt: " + rc);

            // Get locally (for node) optimal (by information gain) splits.
            long before = System.currentTimeMillis();

//            IndexAndSplitInfo best = CacheUtils.sparseFold(cache.getName(),
//                    (Cache.Entry<RegionKey, RegionProjection> e, IndexAndSplitInfo si) -> {
//                        int featIdx = e.getKey().featureIdx();
//                        int regIdx = e.getKey().regionIdx();
//
//                        FeatureProcessor vector = vectorProcessor(input, featIdx);
//                        RegionProjection reg = e.getValue();
//                        IndexAndSplitInfo bestForReg = reg.depth < maxDepth ? new IndexAndSplitInfo(featIdx, vector.findBestSplit(reg, regIdx)) : new IndexAndSplitInfo();
//                        return Functions.MAX_GENERIC(si, bestForReg, Comparator.comparingDouble(x -> x.info != null ? x.info.infoGain() : Double.NEGATIVE_INFINITY));
//                    },
//                    allKeys(input, regsCnt, uuid),
//                    (i1, i2) -> Functions.MAX_GENERIC(i1, i2, Comparator.comparingDouble(x -> x.info != null ? x.info.infoGain() : Double.NEGATIVE_INFINITY)),
//                    IndexAndSplitInfo::new,
//                    null,
//                    null,
//                    0,
//                    true
//            );
            Map.Entry<Integer, SplitInfo> bestEntry = optimalForFeature.entrySet().stream().
                max(Comparator.comparingDouble(en -> en.getValue().infoGain())).orElse(null);

            long total = System.currentTimeMillis() - before;

            if (bestEntry != null && bestEntry.getValue().infoGain() > MIN_INFO_GAIN) {
                regsCnt++;

                SplitInfo bestInfo = bestEntry.getValue();
                int bestRegIdx = bestInfo.regionIndex();
                int bestFeatIdx = bestEntry.getKey();
                System.out.println("Globally best: " + bestInfo + " time: " + total);

                RegionKey bestRegKey = getCacheKey(bestFeatIdx, bestRegIdx, uuid, input.affinityKey(bestFeatIdx));

                // Request bitset for split region.
                SparseBitSet bs = cache.invoke(bestRegKey,
                    (e, args) -> vectorProcessor(input, bestFeatIdx).calculateOwnershipBitSet(e.getValue(), bestInfo));

                // Update decision tree.
                SplitNode sn = bestInfo.createSplitNode(bestFeatIdx);

                TreeTip tipToSplit = tips.get(bestRegIdx);
                tipToSplit.leafSetter.accept(sn);
                tipToSplit.leafSetter = sn::setLeft;
                int d = tipToSplit.depth++;
                tips.add(new TreeTip(sn::setRight, d));

                if (d > curDepth) {
                    curDepth = d;
                    System.out.println("Depth: " + curDepth);
                    System.out.println("Cache size: " + cache.size(CachePeekMode.PRIMARY));
                }

                Map<Integer, Integer> catFeaturesInfo = input.catFeaturesInfo();

                before = System.currentTimeMillis();
                // Perform split on all feature vectors.
                IgniteSupplier<Set<RegionKey>> bestRegsKeys = () -> IntStream.range(0, input.featuresCount()).
                        mapToObj(fIdx -> getCacheKey(fIdx, bestRegIdx, uuid, input.affinityKey(fIdx))).collect(Collectors.toSet());

                CacheUtils.update(cache.getName(),
                    (Cache.Entry<RegionKey, RegionProjection> e) -> {
                        RegionKey k = e.getKey();
                        RegionProjection v = e.getValue();
                        IgniteBiTuple<RegionProjection, RegionProjection> regs;
                        int fIdx = k.featureIdx();

                        if (!catFeaturesInfo.containsKey(fIdx) && !catFeaturesInfo.containsKey(bestFeatIdx))
                            regs = new ContinuousFeatureProcessor<>(calc).performSplit(bs, v, (D)bestInfo.leftData(), (D)bestInfo.rightData());
                        else
                            regs = vectorProcessor(input, k.featureIdx).performSplitGeneric(bs, v, bestInfo.leftData(), bestInfo.rightData());

                        return Stream.of(new CacheEntryImpl<>(k, regs.get1()), new CacheEntryImpl<>(getCacheKey(fIdx, rc, uuid, input.affinityKey(fIdx)), regs.get2()));
                    },
                    bestRegsKeys);

                // Recalculate optimals for each feature. There are 2 cases.

                // (1): For all features except one be which the split has been done,
                // new optimal split can be calculated as max(previouslyOptimal, newRegion1, newRegion2) (max taken by information gain).
                IgniteSupplier<Set<RegionKey>> nonOpts = () -> IntStream.range(0, input.featuresCount()).
                    filter(i -> i != bestFeatIdx && optimalForFeature.containsKey(i)).boxed().
                    flatMap(fIdx -> Stream.of(optimalForFeature.get(fIdx).regionIndex(), bestRegIdx, rc).map(rIdx -> getCacheKey(fIdx, rIdx, uuid, input.affinityKey(fIdx)))).
                    collect(Collectors.toSet());

                Map<Integer, SplitInfo> m = getOptimals(input, nonOpts);
                updateOptimals(optimalForFeature, m, bestFeatIdx);

                // (2): For feature by which split has been done we calculate all regions splits and chose optimal one.
                IgniteSupplier<Set<RegionKey>> opts = () -> IntStream.range(0, input.featuresCount()).
                    filter(i -> !m.containsKey(i)).boxed().
                    flatMap(fIdx -> IntStream.range(0, rc + 1).boxed().map(rIdx -> getCacheKey(fIdx, rIdx, uuid, input.affinityKey(fIdx)))).
                    collect(Collectors.toSet());
                //IgniteSupplier<Set<RegionKey>> opts = () -> IntStream.range(0, rc + 1).mapToObj(rIdx -> getCacheKey(bestFeatIdx, rIdx, uuid, input.affinityKey(bestFeatIdx))).collect(Collectors.toSet());
                m.clear();
                m.putAll(getOptimals(input, opts));
                updateOptimals(optimalForFeature, m, null);

                System.out.println("Update took " + (System.currentTimeMillis() - before));
            }
            else
                break;
        }

        int rc = regsCnt;

        IgniteSupplier<Set<RegionKey>> featZeroRegs = () -> IntStream.range(0, rc).
                mapToObj(rIdx -> getCacheKey(0, rIdx, uuid, input.affinityKey(0))).collect(Collectors.toSet());

        Map<Integer, Double> vals = CacheUtils.sparseFold(cache.getName(),
                (Cache.Entry<RegionKey, RegionProjection> e, Map<Integer, Double> m) -> {
                    int regIdx = e.getKey().regionIdx();

                    Double apply = regCalc.apply(Arrays.stream(e.getValue().samples()).mapToDouble(SampleInfo::label));
                    m.put(regIdx, apply);

                    return m;
                },
                featZeroRegs,
                (infos, infos2) -> {
                    Map<Integer, Double> res = new HashMap<>();
                    res.putAll(infos);
                    res.putAll(infos2);
                    return res;
                },
                HashMap::new,
                null,
                null,
                0,
                true
        );

        int i = 0;
        for (TreeTip tip : tips) {
            tip.leafSetter.accept(new Leaf(vals.get(i)));
            i++;
        }

        cache.removeAll(allKeys(input, regsCnt, uuid).get());

        return new DecisionTreeModel(root.s);
    }

    private Map<Integer, SplitInfo> getOptimals(ColumnDecisionTreeInput input, IgniteSupplier<Set<RegionKey>> keys) {
        return CacheUtils.sparseFold(cache.getName(),
                (Cache.Entry<RegionKey, RegionProjection> e, Map<Integer, SplitInfo> si) -> {
                    int featIdx = e.getKey().featureIdx();
                    int regIdx = e.getKey().regionIdx();

                    FeatureProcessor vector = vectorProcessor(input, featIdx);
                    RegionProjection reg = e.getValue();
                    SplitInfo bestForReg = (reg.depth < maxDepth) ? vector.findBestSplit(reg, regIdx) : null;
                    si.compute(featIdx, (idx, info) -> Functions.MAX_GENERIC(info, bestForReg, Comparator.comparingDouble(x -> x != null ? x.infoGain() : Double.NEGATIVE_INFINITY)));
                    return si;
                },
                keys,
                (opts1, opts2) -> {
                    opts2.putAll(opts1);
                    return opts2;
                },
                HashMap::new,
                null,
                null,
                0,
                true
            );
    }
    private void updateOptimals(Map<Integer, SplitInfo> optimals, Map<Integer, SplitInfo> updater, Integer key) {
        Set<Integer> ks = new HashSet<>(optimals.keySet());
        ks.forEach(k -> {
            if (!updater.containsKey(k) && !k.equals(key))
                optimals.remove(k);
            else
                optimals.put(k, updater.get(k));
        });
    }

    /**
     * Create new cache for ColumnDecisionTreeTrainer if needed.
     */
    private IgniteCache<RegionKey, RegionProjection> newCache() {
        CacheConfiguration<RegionKey, RegionProjection> cfg = new CacheConfiguration<>();

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

    /**
     * All keys supplier.
     * @param i Input.
     * @param regCnt Count of regions.
     * @param uuid
     * @return
     */
    private IgniteSupplier<Set<RegionKey>> allKeys(ColumnDecisionTreeInput i, int regCnt, IgniteUuid uuid) {
        return () -> IntStream.range(0, i.featuresCount()).boxed().flatMap(fIdx ->
                IntStream.range(0, regCnt).boxed().map(regIdx -> getCacheKey(fIdx, regIdx, uuid, i.affinityKey(fIdx)))).collect(Collectors.toSet());

    }

    /** Get vector processor. */
    private FeatureProcessor vectorProcessor(ColumnDecisionTreeInput i, int featureIdx) {
        return i.catFeaturesInfo().containsKey(featureIdx) ?
            new CategoricalFeatureProcessor(catImpCalc, i.catFeaturesInfo().get(featureIdx)) :
            new ContinuousFeatureProcessor<>(calc);
    }
}
