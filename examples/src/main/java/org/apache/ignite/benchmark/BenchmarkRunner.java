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

package org.apache.ignite.benchmark;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;

/**
 * Benchmark runner.
 */
public class BenchmarkRunner {
    /** Configuration path. */
    private static final String CFG_PATH =
        "C:\\Personal\\code\\incubator-ignite\\examples\\src\\main\\java\\org\\apache\\ignite\\benchmark\\benchmark-config.xml";

    /**
     * Entry point.
     */
    public static void main(String[] args) {
        Ignition.setClientMode(true);

        try (Ignite client = Ignition.start(CFG_PATH)) {
            System.out.println(client);
        }
    }
}
