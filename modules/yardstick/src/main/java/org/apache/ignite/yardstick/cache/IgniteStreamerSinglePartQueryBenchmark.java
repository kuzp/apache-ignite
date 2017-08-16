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

package org.apache.ignite.yardstick.cache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import javax.cache.Cache;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.MutableEntry;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.cache.affinity.AffinityKey;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.cluster.ClusterGroup;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.lang.IgniteCallable;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.transactions.Transaction;
import org.apache.ignite.yardstick.IgniteAbstractBenchmark;
import org.apache.ignite.yardstick.cache.model.ZipEntity;
import org.apache.ignite.yardstick.cache.model.ZipQueryEntity;
import org.jetbrains.annotations.NotNull;
import org.yardstickframework.BenchmarkUtils;

import static org.apache.ignite.transactions.TransactionConcurrency.PESSIMISTIC;
import static org.apache.ignite.transactions.TransactionIsolation.REPEATABLE_READ;

/**
 *
 */
public class IgniteStreamerSinglePartQueryBenchmark extends IgniteAbstractBenchmark {
    /** Cache name. */
    public static final String CACHE_NAME = "streamer-cache";

    /** Big cache name. */
    public static final String BIG_CACHE_NAME = "streamer-cache-2";

    /** {@inheritDoc} */
    @Override public boolean test(Map<Object, Object> map) throws Exception {
        BenchmarkUtils.println("IgniteStreamerSinglePartQueryBenchmark start test.");

        final int threads = args.compressThreads();

        final String cacheName = args.bigEntry() ? BIG_CACHE_NAME : CACHE_NAME;

        TestConfig[] configs = {
            new TestConfig(10_000),
            new TestConfig(50_000),
            new TestConfig(100_000),
            new TestConfig(500_000),
            new TestConfig(1_000_000),
            new TestConfig(10_000_000),
            new TestConfig(20_000_000),
            new TestConfig(50_000_000),
        };

        for (TestConfig config : configs) {
            load(threads, cacheName, config.range);

            update(cacheName, config.range);

            ignite().cache(cacheName).clear();
        }


        return false;
    }

    private void update(String cacheName, int range) {
        ClusterGroup grp = ignite().cluster().forServers().forCacheNodes(cacheName);

        BenchmarkUtils.println("IgniteStreamerSinglePartQueryBenchmark start single partition scan query update. " +
            ", bigEntry=" + args.bigEntry() + ", entries=" + range + ']');

        final long updateStart = System.currentTimeMillis();

        ignite().compute(grp).broadcast(new UpdateCall(cacheName));

        final long updateEnd = System.currentTimeMillis();

        BenchmarkUtils.println("IgniteStreamerSinglePartQueryBenchmark scan query finished. [time="
            + (updateEnd - updateStart) + ", bigEntry=" + args.bigEntry() + ", compute=" + args.compute()
            + ", entries=" + range + ']');

        for (int i = 0; i < 2; i++)
            System.out.println(ignite().cache(cacheName).get(new AffinityKey<>(String.valueOf(i), 0)));
    }

    private void load(int threads, String cacheName, int range) throws InterruptedException, java.util.concurrent.ExecutionException {
        ExecutorService exec =  Executors.newFixedThreadPool(threads, new ThreadFactory() {
            @Override public Thread newThread(@NotNull Runnable r) {
                Thread t = new Thread(r);

                t.setName("streamer-loader-" + t.getId());

                return t;
            }
        });

        BenchmarkUtils.println("IgniteStreamerSinglePartQueryBenchmark start loading [entries=" + range +
            ", threads=" + threads + ']');

        List<Future<?>> futs = new ArrayList<>(threads);

        try (final IgniteDataStreamer<Object, Object> streamer = ignite().dataStreamer(cacheName)) {
            final long startLoad = System.currentTimeMillis();

            for (int i = 0; i < threads; i++) {
                final int part = range / threads;
                final int start = i * part;

                Future<Object> fut = exec.submit(new Callable<Object>() {
                    @Override public Object call() throws Exception {
                        for (int j = start; j < start + part; j++) {
                            // load to single partition
                            streamer.addData(new AffinityKey<>(String.valueOf(j), 0), createEntry(args.bigEntry()));

                            if (j % 100_000 == 0) {
                                BenchmarkUtils.println("IgniteStreamerSinglePartQueryBenchmark loaded entries: [entries="
                                    + (j - start) + ", threadName=" + Thread.currentThread().getName() + ']');
                            }
                        }

                        return null;
                    }
                });

                futs.add(fut);
            }

            for (Future<?> fut : futs)
                fut.get();

            streamer.flush();

            final long endLoad = System.currentTimeMillis();

            BenchmarkUtils.println("IgniteStreamerSinglePartQueryBenchmark finished loading entries. [entries=" + args.range()
                + ", threads=" + threads + ", time=" + (endLoad - startLoad) + ']');
        }
        finally {
            exec.shutdown();
        }
    }

    /**
     * @param bigEntry Big entry.
     */
    private Object createEntry(boolean bigEntry) {
        if (bigEntry) {
            ZipEntity entity = ZipEntity.generateHard(1.0, ZipEntity.RND_STRING_LEN);

            entity.BUSINESSDATE = "2017-06-30";
            entity.BOOKSOURCESYSTEMCODE = "93013109";

            return entity;
        }
        else
            return ZipQueryEntity.generateHard();
    }

    /**
     *
     */
    static class UpdateCall implements IgniteCallable<Object> {
        /** Serial version uid. */
        private static final long serialVersionUID = 0L;

        /** Ignite. */
        @IgniteInstanceResource
        private Ignite ignite;

        /** Cache name. */
        private final String cacheName;

        /**
         * @param cacheName Cache name.
         */
        UpdateCall(String cacheName) {
            this.cacheName = cacheName;
        }

        /** {@inheritDoc} */
        @Override public Object call() throws Exception {
            final IgniteCache<BinaryObject, BinaryObject> cache = ignite.cache(cacheName).withKeepBinary();

            final int part = ignite.affinity(cacheName).partition(new AffinityKey<>("", 0));

            QueryCursor<Cache.Entry<BinaryObject, BinaryObject>> cur =
                cache.query(createScanQuery().setLocal(true).setPartition(part));

            Set<BinaryObject> keys = new HashSet<>();

            for (Cache.Entry<BinaryObject, BinaryObject> entry : cur)
                keys.add(entry.getKey());

            try (Transaction tx = ignite.transactions().txStart(PESSIMISTIC, REPEATABLE_READ)) {
                cache.invokeAll(keys, new Updater());

                tx.commit();
            }
            catch (Exception e) {
                Transaction tx = ignite.transactions().tx();

                if (tx != null)
                    tx.rollback();

                throw e;
            }

            return null;
        }
    }

    /**
     *
     */
    private static class Updater implements EntryProcessor<BinaryObject, BinaryObject, Object> {
        /** {@inheritDoc} */
        @Override public Object process(MutableEntry<BinaryObject, BinaryObject> entry,
            Object... arguments) throws EntryProcessorException {
            final BinaryObject val = entry.getValue();

            final double total = val.field("TOTALVALUE");

            BinaryObject newVal = val.toBuilder().setField("TOTALVALUE", total + 10).build();

            entry.setValue(newVal);

            return null;
        }
    }

    /**
     *
     */
    private static ScanQuery<BinaryObject, BinaryObject> createScanQuery() {
        return new ScanQuery<>(new IgniteBiPredicate<BinaryObject, BinaryObject>() {
            @Override public boolean apply(BinaryObject s, BinaryObject b) {
                return b.field("businessdate").equals("2017-06-30")
                    && b.field("booksourcesystemcode").equals("93013109");
            }
        });
    }

    private static class TestConfig {
        private final int range;

        public TestConfig(int range) {
            this.range = range;
        }
    }
}
