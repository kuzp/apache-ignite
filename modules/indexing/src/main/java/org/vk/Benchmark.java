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

package org.vk;

import java.util.concurrent.atomic.AtomicLong;

public abstract  class Benchmark {

    private static final AtomicLong cnt = new AtomicLong();

    public void run() {
        System.out.println("Starting benchmark: " + getClass().getSimpleName());

        try {
            setup();

            System.out.println("Set up completed.");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        long startTime = System.currentTimeMillis();
                        long startCnt = cnt.get();

                        try {
                            Thread.sleep(3000);
                        }
                        catch (InterruptedException e) {
                            // No-op.
                        }

                        long endTime = System.currentTimeMillis();
                        long endCnt = cnt.get();

                        long deltaTime = endTime - startTime;
                        long deltaCnt = endCnt - startCnt;

                        float t = (float)deltaCnt / (float)deltaTime * 1000;

                        System.out.println("Throughput: " + t);
                    }
                }
            }).start();

            while (true) {
                test();

                cnt.incrementAndGet();
            }
        }
        finally {
            tearDown();
        }
    }

    protected abstract void setup();

    protected abstract void test();

    protected abstract void tearDown();
}
