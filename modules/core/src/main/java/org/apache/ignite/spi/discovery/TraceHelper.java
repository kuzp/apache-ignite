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

package org.apache.ignite.spi.discovery;

import java.util.ArrayList;
import java.util.List;
import org.apache.ignite.internal.managers.discovery.DiscoveryCustomMessage;

/**
 * Helper for tracing discovery messages.
 */
public class TraceHelper {
    /** Trace class name. */
    public static final String TRACE_CLASS_NAME = System.getProperty("IGNITE_DISCOVERY_CUSTOM_TRACE_CLS", "StartSnapshotOperationAckDiscoveryMessage");

    private static final ThreadLocal<TraceHelper> helperThreadLoc = new ThreadLocal<TraceHelper>() {
        @Override protected TraceHelper initialValue() {
            return new TraceHelper();
        }
    };

    /** */
    List<TraceStep> traces = new ArrayList<>();

    private long t0;

    public void start(DiscoveryCustomMessage msg) {
        if (msg == null || !TRACE_CLASS_NAME.equals(msg.getClass().getSimpleName()))
            return;

        if (t0 != 0)
            throw new IllegalStateException("Unbalanced call to start");

        t0 = System.nanoTime();
    }

    public void finish(DiscoveryCustomMessage msg) {
        if (msg == null || !TRACE_CLASS_NAME.equals(msg.getClass().getSimpleName()))
            return;

        if (t0 == 0)
            throw new IllegalStateException("Unbalanced call to finish");

        try {
            long delta = System.nanoTime() - t0;

            traces.add(new TraceStep(delta));
        }
        finally {
            t0 = 0;
        }
    }

    /** */
    public static class TraceStep {
        /** Delta nanos. */
        final long delta;

        /** File name. */
        final String fileName;

        /** Line. */
        final int line;

        /**
         * @param delta Delta.
         */
        public TraceStep(long delta) {
            this.delta = delta;

            StackTraceElement element = Thread.currentThread().getStackTrace()[3];
            fileName = element.getFileName();
            line = element.getLineNumber();
        }

        public long getDelta() {
            return delta;
        }

        public String getFileName() {
            return fileName;
        }

        public int getLine() {
            return line;
        }
    }

    public List<TraceStep> getAndClear() {
        List<TraceStep> cpy = new ArrayList<>(traces);

        traces.clear();

        return cpy;
    }

    public static TraceHelper get() {
        return helperThreadLoc.get();
    }
}