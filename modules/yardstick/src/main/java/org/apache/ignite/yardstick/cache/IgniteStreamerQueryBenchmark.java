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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.cache.Cache;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.binary.BinaryObjectBuilder;
import org.apache.ignite.cache.query.FieldsQueryCursor;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.cluster.ClusterGroup;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.lang.IgniteCallable;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.yardstick.IgniteAbstractBenchmark;
import org.apache.ignite.yardstick.cache.model.ZipEntity;
import org.apache.ignite.yardstick.cache.model.ZipQueryEntity;
import org.jetbrains.annotations.NotNull;
import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkUtils;

/**
 *
 */
public class IgniteStreamerQueryBenchmark extends IgniteAbstractBenchmark {
    /** Cache name. */
    public static final String CACHE_NAME = "streamer-cache";

    /** Big cache name. */
    public static final String BIG_CACHE_NAME = "streamer-cache-2";

    /** {@inheritDoc} */
    @Override public void setUp(BenchmarkConfiguration cfg) throws Exception {
        super.setUp(cfg);
    }

    /** {@inheritDoc} */
    @Override public boolean test(Map<Object, Object> map) throws Exception {
        BenchmarkUtils.println("IgniteStreamerQueryBenchmark start test.");

        final int threads = args.compressThreads();

        final String cacheName = args.bigEntry() ? BIG_CACHE_NAME : CACHE_NAME;

        ExecutorService exec =  Executors.newFixedThreadPool(threads, new ThreadFactory() {
            @Override public Thread newThread(@NotNull Runnable r) {
                Thread t = new Thread(r);

                t.setName("streamer-loader-" + t.getId());

                return t;
            }
        });

        BenchmarkUtils.println("IgniteStreamerQueryBenchmark start loading [entries=" + args.range() +
            ", threads=" + threads + ']');

        List<Future<?>> futs = new ArrayList<>(threads);

        try (final IgniteDataStreamer<Object, Object> streamer = ignite().dataStreamer(cacheName)) {
            final long startLoad = System.currentTimeMillis();

            for (int i = 0; i < threads; i++) {
                final int part = args.range() / threads;
                final int start = i * part;

                Future<Object> fut = exec.submit(new Callable<Object>() {
                    @Override public Object call() throws Exception {
                        for (int j = start; j < start + part; j++) {
                            streamer.addData(String.valueOf(j), createEntry(args.bigEntry()));

                            if (j % 100_000 == 0) {
                                BenchmarkUtils.println("IgniteStreamerQueryBenchmark loaded entries: [entries=" + (j - start) + ", threadName="
                                    + Thread.currentThread().getName() + ']');
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

            BenchmarkUtils.println("IgniteStreamerQueryBenchmark finished loading entries. [entries=" + args.range()
                + ", threads=" + threads + ", time=" + (endLoad - startLoad) + ']');
        }
        finally {
            exec.shutdown();
        }

        final QueryType qryType = QueryType.valueOf(args.queryType());

        IgniteCache<Object, Object> cache = ignite().cache(cacheName);

        if (qryType == QueryType.SCAN_UPDATE) {
            final long startQry = System.currentTimeMillis();

            if (args.compute()) {
                ClusterGroup grp = ignite().cluster().forServers();

                ignite().compute(grp).broadcast(new UpdateCall(cacheName));
            }
            else {
                ScanQuery<String, BinaryObject> qry = createScanQuery();

                IgniteCache<Object, Object> binCache = cache.withKeepBinary();

                QueryCursor<Cache.Entry<String, BinaryObject>> cur = binCache.query(qry);

                update(cache, cur);
            }

            final long endQry = System.currentTimeMillis();

            BenchmarkUtils.println("IgniteStreamerQueryBenchmark scan query finished. [time=" + (endQry - startQry)
                + ", bigEntry=" + args.bigEntry() + ", compute=" + args.compute() + ']');
        }
        else {
            final SqlFieldsQuery qry = createQuery(qryType, args.bigEntry(), args.lazySql(), args.queryPageSize());

            BenchmarkUtils.println("IgniteStreamerQueryBenchmark start query. [query=" + qry.getSql() + ", bigEntry=" + args.bigEntry()
                + ", compute=" + args.compute() + ", lazyQuery=" + args.lazySql() + ']');

            final long startQry = System.currentTimeMillis();

            try {
                if (!args.compute())
                    executeQuery(ignite(), cacheName, qry, startQry, qryType);
                else {
                    ClusterGroup grp = ignite().cluster().forServers();

                    ignite().compute(grp).broadcast(new IgniteCallable<Object>() {
                        @IgniteInstanceResource
                        private Ignite ignite;

                        @Override public Object call() throws Exception {
                            executeQuery(ignite, cacheName, qry.setLocal(true), startQry, qryType);

                            return null;
                        }
                    });
                }
            }
            finally {
                final long endQry = System.currentTimeMillis();

                BenchmarkUtils.println("IgniteStreamerQueryBenchmark query finished. [time=" + (endQry - startQry)
                    + ", bigEntry=" + args.bigEntry() + ", compute=" + args.compute()
                    + ", lazyQuery=" + args.lazySql() + ']');
            }
        }

        for (int ii = 0; ii < 2; ii++)
            System.out.println(cache.get(String.valueOf(ii)));

        return false;
    }

    /**
     * @param map Map.
     * @param cache Cache.
     * @param i I.
     */
    private static int save(Map<Object, Object> map, IgniteCache<Object, Object> cache, int i, boolean last) {
        if (map.size() > 100_000 || (last && !map.isEmpty())) {
            cache.putAll(map);

            map.clear();

            i += 100_000;

            System.out.println("== " + i + " records was updated");
        }

        return i;
    }

    /**
     * @param ignite Ignite.
     * @param cacheName Cache name.
     * @param qry Query.
     * @param startQry Start query.
     */
    private static void executeQuery(Ignite ignite, String cacheName, SqlFieldsQuery qry, long startQry, QueryType qryType) {
        FieldsQueryCursor<List<?>> cur = ignite.cache(cacheName).query(qry);

        if (qryType == QueryType.SELECT) {
            int items = 0;

            for (List<?> item : cur) {
                items++;

                if (items % 100_000 == 0)
                    System.out.println("== Loaded " + items + " in " + (System.currentTimeMillis() - startQry));
            }

            System.out.println("IgniteStreamerQueryBenchmark got: [items=" + items + ", time="
                + (System.currentTimeMillis() - startQry) + ']');
        }

        cur.close();
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
     * @param type Type.
     * @param bigEntry Big entry.
     */
    private static SqlFieldsQuery createQuery(QueryType type, boolean bigEntry, boolean lazy, int pageSize) {
        if (type == QueryType.UPDATE) {
            final String qry = "UPDATE " + (bigEntry ? "ZIP_ENTITY " : "ZIP_QUERY_ENTITY ") +
                "SET totalvalue=?, " +
                "sys_audit_trace = concat(" +
                " casewhen(sys_audit_trace is not null, concat(sys_audit_trace,','),'')," +
                " concat('{\\\"aid\\\":\\\"', ?, '\\\",\\\"changes\\\":{\\\"totalvalue\\\":\\\"')," +
                "nvl(totalvalue,'')," +
                "'\\\"}}')" +
                "WHERE businessdate=? AND booksourcesystemcode=?";

            SqlFieldsQuery sqlQry = new SqlFieldsQuery(qry);

            sqlQry.setArgs(0.0, "2017-06-30_20170806230013895", "2017-06-30", "93013109");
            sqlQry.setLazy(lazy);
            sqlQry.setPageSize(pageSize);

            return sqlQry;
        }
        else if (type == QueryType.SELECT) {
            final String qry = "SELECT * FROM " + (bigEntry ? "ZIP_ENTITY " : "ZIP_QUERY_ENTITY ") +
                "WHERE businessdate=? AND booksourcesystemcode=?";

            SqlFieldsQuery sqlQry = new SqlFieldsQuery(qry);

            sqlQry.setArgs("2017-06-30", "93013109");
            sqlQry.setLazy(lazy);
            sqlQry.setPageSize(pageSize);

            return sqlQry;
        }

        throw new IllegalArgumentException("Unsupported query type: " + type);
    }

    /**
     *
     */
    private static ScanQuery<String, BinaryObject> createScanQuery() {
        return new ScanQuery<>(new IgniteBiPredicate<String, BinaryObject>() {
            @Override public boolean apply(String s, BinaryObject b) {
                return b.field("businessdate").equals("2017-06-30")
                    && b.field("booksourcesystemcode").equals("93013109");
            }
        });
    }

    /**
     *
     */
    private enum QueryType {
        /** Update. */UPDATE, /** Select. */SELECT, /** Scan update. */SCAN_UPDATE
    }

    /**
     *
     */
    static class UpdateCall implements IgniteCallable<Object> {
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
            final IgniteCache<Object, Object> cache = ignite.cache(cacheName).withKeepBinary();

            int[] parts = ignite.affinity(cacheName).primaryPartitions(ignite.cluster().localNode());

            ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            for (int i = 0; i < parts.length; i++) {
                final int part = parts[i];

                pool.submit(new Callable<Object>() {
                    @Override public Object call() throws Exception {
                        QueryCursor<Cache.Entry<String, BinaryObject>> cur =
                            cache.query(createScanQuery().setLocal(true).setPartition(part));

                        update(cache, cur);

                        return null;
                    }
                });
            }

            pool.shutdown();
            pool.awaitTermination(1, TimeUnit.HOURS);

            return null;
        }
    }

    /**
     * @param cache Cache.
     * @param cur Query.
     */
    private static void update(IgniteCache<Object, Object> cache, QueryCursor<Cache.Entry<String, BinaryObject>> cur) {
        int i = 0;

        Map<Object, Object> map = new HashMap<>();

        for (Cache.Entry<String, BinaryObject> entry : cur) {
            BinaryObject val = entry.getValue();

            BinaryObjectBuilder builder = val.toBuilder();

            double newTotalVal = 0.0;

            builder.setField("totalvalue", 0.0);

            String sysAuditTrace = val.field("sys_audit_trace");

            sysAuditTrace = sysAuditTrace.isEmpty() ? "" : sysAuditTrace + ",";
            sysAuditTrace += "{\"aid\":\""+ "2017-06-30_20170806230013895" +"\"," +
                "\"changes\":{\"totalvalue\":\"" + newTotalVal + "\"}}";

            builder.setField("sys_audit_trace", sysAuditTrace);

            map.put(entry.getKey(), builder.build());

            i = save(map, cache, i, false);
        }

        save(map, cache, i, true);
    }
}
