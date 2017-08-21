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

import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.yardstick.IgniteAbstractBenchmark;
import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkUtils;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class IgniteDataStreamerBenchmarkV2 extends IgniteAbstractBenchmark {

    /** */
    private int entries;

    /** */
    private long curKey;

    /** */
    private int threads;

    /** */
    private String cacheName;

    /** */
    private IgniteDataStreamer[] streamers;

    /** */
    private byte[] data;

    private ExecutorService executor;

    /** {@inheritDoc} */
    @Override public void setUp(BenchmarkConfiguration cfg) throws Exception {
        super.setUp(cfg);

        ignite().active(true);

        threads = cfg.threads();

        entries = args.batch();

        int entrySize = args.streamerEntrySize();

        data = new byte[entrySize];

        ThreadLocalRandom.current().nextBytes(data);

        if (threads < 1)
            throw new IllegalArgumentException("Invalid threads count: " + threads);

        String cacheNamePrefix = args.streamerCachesPrefix();

        if (cacheNamePrefix == null || cacheNamePrefix.isEmpty())
            throw new IllegalArgumentException("Streamer caches prefix not set.");

        for (String cacheName : ignite().cacheNames()) {
            if (cacheName.startsWith(cacheNamePrefix))
                this.cacheName = cacheName;
        }

        if (cacheName == null)
            throw new IllegalArgumentException("Failed to find for IgniteStreamerBenchmark cache " +
                "starting with '" + cacheNamePrefix + "'");

        BenchmarkUtils.println("Found cache for IgniteStreamerBenchmark: " + cacheName);

        int bufSize = args.streamerBufferSize();

        int parOps = args.streamerParOps();

        streamers = new IgniteDataStreamer[threads];

        for (int i = 0; i < threads; i++) {
            IgniteDataStreamer streamer = ignite().dataStreamer(cacheName);

            streamer.perNodeBufferSize(bufSize);
            streamer.perNodeParallelOperations(parOps);

            streamers[i] = streamer;
        }

        if(threads > 1)
            executor = Executors.newFixedThreadPool(threads - 1);
    }

    /** {@inheritDoc} */
    @Override public void tearDown() throws Exception {
        if (streamers != null) {
            for (IgniteDataStreamer streamer : streamers)
                U.closeQuiet(streamer);
        }

        if (executor != null) {
            executor.shutdown();

            if (!executor.awaitTermination(20, TimeUnit.SECONDS))
                executor.shutdownNow();
        }

        super.tearDown();
    }

    /** {@inheritDoc} */
    @Override public boolean test(Map<Object, Object> ctx) throws Exception {
        Future[] futs = threads > 1 ? new Future[threads - 1] : null;

        if (threads > 1) {
            for (int i = 1; i < threads; i++) {
                final long start = curKey + i;
                final long end = curKey + (entries * threads);
                final IgniteDataStreamer streamer = streamers[i];

                futs[i - 1] = executor.submit(new Runnable() {
                    @Override public void run() {
                        for (long i = start; i < end; i += threads)
                            streamer.addData(i, data);
                    }
                });
            }
        }

        final long start = curKey;
        final long end = curKey + entries;
        final IgniteDataStreamer streamer = streamers[0];

        for (long i = start; i < end; i += threads)
            streamer.addData(i, data);

        if (futs != null) {
            for (int i = 0; i < threads - 1; i++)
                futs[i].get(20, TimeUnit.SECONDS);
        }

        curKey += entries * threads;

        return true;
    }
}
