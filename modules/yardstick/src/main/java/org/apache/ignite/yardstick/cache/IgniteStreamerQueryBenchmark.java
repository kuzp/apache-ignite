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
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.cache.query.FieldsQueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.cluster.ClusterGroup;
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

        final SqlFieldsQuery qry = createQuery(qryType, args.bigEntry());

        BenchmarkUtils.println("IgniteStreamerQueryBenchmark start query. [query=" + qry.getSql() + ", bigEntry=" + args.bigEntry()
            + ", compute=" + args.compute() + ']');

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
                + ", bigEntry=" + args.bigEntry() + ", compute=" + args.compute() + ']');
        }

        IgniteCache<Object, Object> cache = ignite().cache(cacheName);

        for (int i = 0; i < 2; i++)
            System.out.println(cache.get(String.valueOf(i)));

        return false;
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

                if (items % 1000 == 0)
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
    private static SqlFieldsQuery createQuery(QueryType type, boolean bigEntry) {
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

            return sqlQry;
        }
        else if (type == QueryType.SELECT) {
            final String qry = "SELECT * FROM " + (bigEntry ? "ZIP_ENTITY " : "ZIP_QUERY_ENTITY ") +
                "WHERE businessdate=? AND booksourcesystemcode=?";

            SqlFieldsQuery sqlQry = new SqlFieldsQuery(qry);

            sqlQry.setArgs("2017-06-30", "93013109");

            return sqlQry;
        }

        throw new IllegalArgumentException("Unsupported query type: " + type);
    }

    /**
     *
     */
    private enum QueryType {
        /** Update. */UPDATE, /** Select. */SELECT
    }
}
