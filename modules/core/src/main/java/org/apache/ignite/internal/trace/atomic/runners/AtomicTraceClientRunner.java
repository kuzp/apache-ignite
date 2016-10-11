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

package org.apache.ignite.internal.trace.atomic.runners;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.internal.trace.TraceCluster;
import org.apache.ignite.internal.trace.TraceData;
import org.apache.ignite.internal.trace.atomic.AtomicTrace;
import org.apache.ignite.internal.trace.atomic.AtomicTraceUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.ignite.internal.trace.atomic.AtomicTraceUtils.CACHE_NAME;

/**
 * Atomic trace client runner.
 */
public class AtomicTraceClientRunner {
    /** Overall test duration. */
    private static final long DUR = 120000L;

    /** Trace duration. */
    private static final long TRACE_DUR = 2000L;

    /** Sleep duration. */
    private static final long SLEEP_DUR = 5000L;

    /** Cache load threads count. */
    private static final int CACHE_LOAD_THREAD_CNT = 64;

    /** Cache size. */
    private static final int CACHE_SIZE = 1000;

    /**
     * Entry point.
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        AtomicTraceUtils.prepareTraceDir();

        try {
            // Start topology.
            Ignite node = Ignition.start(AtomicTraceUtils.config("cli", true));

            // Prepare cache.
            IgniteCache<Integer, Integer> cache = node.cache(CACHE_NAME);

            for (int i = 0; i < CACHE_SIZE; i++)
                cache.put(i, i);

            System.out.println(">>> Cache prepared.");

            // Prepare cache loaders.
            List<Thread> threads = new LinkedList<>();

            List<CacheLoader> ldrs = new LinkedList<>();

            for (int i = 0; i < CACHE_LOAD_THREAD_CNT; i++) {
                CacheLoader ldr = new CacheLoader(node);

                ldrs.add(ldr);

                threads.add(new Thread(ldr));
            }

            // Prepare tracer.
            TracePrinter printer = new TracePrinter(node);

            Thread printThread = new Thread(printer);

            threads.add(printThread);

            // Start threads.
            for (Thread thread : threads)
                thread.start();

            // Sleep.
            Thread.sleep(DUR);

            // Stop threads.
            for (CacheLoader ldr : ldrs)
                ldr.stop();

            printer.stop();

            for (Thread thread : threads)
                thread.join();
        }
        finally {
            Ignition.stopAll(true);
        }
    }

    /**
     * Cache load generator.
     */
    private static class CacheLoader implements Runnable {
        /** Index generator. */
        private static final AtomicInteger IDX_GEN = new AtomicInteger();

        /** Node. */
        private final Ignite node;

        /** Index. */
        private final int idx;

        /** Stop flag. */
        private volatile boolean stopped;

        /**
         * Constructor.
         *
         * @param node Node.
         */
        public CacheLoader(Ignite node) {
            this.node = node;

            idx = IDX_GEN.incrementAndGet();
        }

        /** {@inheritDoc} */
        @Override public void run() {
            System.out.println(">>> Cache loader " + idx + " started.");

            try {
                IgniteCache<Integer, Integer> cache = node.cache(CACHE_NAME);

                ThreadLocalRandom rand = ThreadLocalRandom.current();

                // Ensure threads are more or less distributed in time.
                try {
                    Thread.sleep(rand.nextInt(100, 2000));
                }
                catch (InterruptedException e) {
                    // No-op.
                }

                // Payload.
                while (!stopped) {
                    int key = rand.nextInt(CACHE_SIZE);

                    cache.put(key, key);
                }
            }
            finally {
                System.out.println(">>> Cache loader " + idx + " stopped.");
            }
        }

        /**
         * Stop thread.
         */
        public void stop() {
            stopped = true;
        }
    }

    /**
     * Trace printer.
     */
    private static class TracePrinter implements Runnable {
        /** Node. */
        private final Ignite node;

        /** Stop flag. */
        private volatile boolean stopped;

        /**
         * Constructor.
         *
         * @param node Node.
         */
        public TracePrinter(Ignite node) {
            this.node = node;
        }

        /** {@inheritDoc} */
        @Override public void run() {
            System.out.println(">>> Trace printer started.");

            int idx = 0;

            try {
                TraceCluster trace = new TraceCluster(node.cluster().forNodes(node.cluster().nodes()));

                while (!stopped) {
                    Thread.sleep(SLEEP_DUR);

                    trace.enable();

                    System.out.println(">>> Enabled trace");

                    Thread.sleep(TRACE_DUR);

                    trace.disable();

                    System.out.println(">>> Disabled trace");

                    TraceData data = trace.collectAndReset(
                        AtomicTrace.GRP_USR,
                        AtomicTrace.GRP_IO_SND,
                        AtomicTrace.GRP_IO_RCV,
                        AtomicTrace.GRP_SRV,
                        AtomicTrace.GRP_CLI
                    );

                    System.out.println(">>> Collected trace");

                    File traceFile = AtomicTraceUtils.traceFile(idx++);

                    data.save(traceFile);

                    System.out.println(">>> Saved trace");
                    System.out.println();
                }
            }
            catch (Exception e) {
                System.out.println(">>> Trace printer stopped due to exception: " + e);
            }
            finally {
                System.out.println(">>> Trace printer stopped.");
            }
        }

        /**
         * Stop thread.
         */
        public void stop() {
            stopped = true;
        }
    }
}
