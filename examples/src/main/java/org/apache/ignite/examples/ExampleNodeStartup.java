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

package org.apache.ignite.examples;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.configuration.CacheConfiguration;
import static java.lang.System.out;

/**
 * Starts up an empty node with example compute configuration.
 */
public class ExampleNodeStartup {
    /**
     * Start up an empty node with example compute configuration.
     *
     * @param args Command line arguments, none required.
     * @throws IgniteException If failed.
     */
    public static void main(String[] args) throws Exception {
        Ignite ignite = Ignition.start("examples/config/example-ignite.xml");

        try {
            IgniteCache cache = ignite.getOrCreateCache("test-fd-atomic");
            TestValue t = new TestValue();
            t.int_val = 12d;
            t.name = "teststring";

            cache.put("IDD", t);

            t.name = "new string";
            cache.put("IDD", t);

            t = (TestValue)cache.get("IDD2");
            System.out.println("value: " + t.int_val + ", " + t.name);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

/*
        CacheConfiguration cfg = new CacheConfiguration<Object, Object>();
        cfg.setReadThrough(true);
        cfg.setWriteThrough(true);
        cfg.setBackups(3);
        cfg.setWriteBehindEnabled(true);
        cfg.setWriteSynchronizationMode(CacheWriteSynchronizationMode.PRIMARY_SYNC);

        IgniteCache cache = ignite.getOrCreateCache(cfg);
        out.print("Starting..");
        cache.put(12, 42);
        out.print("Put");
        out.print("Get " + cache.get(12));
        out.print("Remove " + cache.remove(12));
*/

        Thread.sleep(15000);
    }
}
