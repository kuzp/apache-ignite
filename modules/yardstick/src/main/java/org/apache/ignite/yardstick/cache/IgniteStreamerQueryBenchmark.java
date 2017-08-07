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
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.cache.query.FieldsQueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.yardstick.IgniteAbstractBenchmark;
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

    /** {@inheritDoc} */
    @Override public void setUp(BenchmarkConfiguration cfg) throws Exception {
        super.setUp(cfg);
    }

    /** {@inheritDoc} */
    @Override public boolean test(Map<Object, Object> map) throws Exception {
        BenchmarkUtils.println("IgniteStreamerQueryBenchmark start test.");

        final int threads = args.compressThreads();

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

        try (final IgniteDataStreamer<Object, Object> streamer = ignite().dataStreamer(CACHE_NAME)) {
            final long startLoad = System.currentTimeMillis();

            for (int i = 0; i < threads; i++) {
                final int part = args.range() / threads;
                final int start = i * part;

                Future<Object> fut = exec.submit(new Callable<Object>() {
                    @Override public Object call() throws Exception {
                        for (int j = start; j < start + part; j++) {
                            streamer.addData(String.valueOf(j), ZipQueryEntity.generateHard());

                            if (j % 100_000 == 0) {
                                BenchmarkUtils.println("IgniteStreamerQueryBenchmark loaded entries: [entries=" + j + ", threadName="
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

        final String qry = "UPDATE  ZIP_QUERY_ENTITY\n" +
            "SET totalvalue= '0.0',\n" +
            "   sys_audit_trace = concat(\n" +
            "    casewhen(sys_audit_trace is not null, concat(sys_audit_trace,','),''),\n" +
            "      '{\"aid\":\"2017-06-30_20170806230013895\",\"changes\":{\"totalvalue\":\"',\n" +
            "      nvl(totalvalue,''),\n" +
            "      '\"}}')\n" +
            "WHERE businessdate='2017-06-30' AND booksourcesystemcode='93013109'";

        BenchmarkUtils.println("IgniteStreamerQueryBenchmark start query. [query=" + qry + ']');

        final long startQry = System.currentTimeMillis();

        try {
            SqlFieldsQuery sqlQry = new SqlFieldsQuery(qry);

            IgniteCache<Object, Object> cache = ignite().cache(CACHE_NAME);

            FieldsQueryCursor<List<?>> cur = cache.query(sqlQry);

            cur.close();
        }
        finally {
            final long endQry = System.currentTimeMillis();

            BenchmarkUtils.println("IgniteStreamerQueryBenchmark query finished. [time=" + (endQry - startQry) + ']');
        }

        return false;
    }
}
