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

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.affinity.rendezvous.RendezvousAffinityFunction;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.cache.query.SqlQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.WALMode;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.lang.IgniteBiPredicate;

import java.io.File;
import java.util.Iterator;
import java.util.Random;

public class SqlBenchmark extends Benchmark {
    private final boolean noCopy;

    public SqlBenchmark(boolean noCopy) {
        this.noCopy = noCopy;
    }

    private IgniteCache<Integer, CMAccums> cache;

    @Override protected void setup() {
        U.delete(new File("C:\\Personal\\code\\incubator-ignite\\work"));

        IgniteConfiguration nodeCfg = new IgniteConfiguration().setLocalHost("127.0.0.1");

        nodeCfg.setDataStorageConfiguration(new DataStorageConfiguration());
        nodeCfg.getDataStorageConfiguration().getDefaultDataRegionConfiguration().setPersistenceEnabled(false);
        nodeCfg.getDataStorageConfiguration().setWalMode(WALMode.BACKGROUND);
        nodeCfg.getDataStorageConfiguration().setPageSize(4 * 1024);

        Ignite ignite = Ignition.start(nodeCfg);

        ignite.active(true);

        RendezvousAffinityFunction aff = new RendezvousAffinityFunction().setPartitions(1024);

        CacheConfiguration<Integer, CMAccums> cfg = new CacheConfiguration<Integer, CMAccums>("sql").setAffinity(aff);

        //cfg.setQueryParallelism(8);

        cfg.setIndexedTypes(Integer.class, CMAccums.class);

//        cfg.setOnheapCacheEnabled(true);
//        cfg.setCopyOnRead(false);

        cache = ignite.createCache(cfg);

        Random rand = new Random();

        for (int i = 0; i < 300_000; i++) {
            CMAccums acc = new CMAccums(rand.nextInt(10), rand);

            cache.put(i, acc);
        }

//        for (int i = 0; i < 30_000; i++) {
//            CMAccums acc = new CMAccums(5, rand);
//
//            cache.put(i, acc);
//        }
    }

    private static final boolean USE_SCAN = false;

    private static boolean EXPLAIN = true;
    private static boolean PRINT_CNT = true;

    @Override protected void test() {
        int arg = 5;

        Iterator iter;

        if (USE_SCAN) {
            IgniteBiPredicate<Integer, CMAccums> pred = new IgniteBiPredicate<Integer, CMAccums>() {
                @Override public boolean apply(Integer key, CMAccums val) {
                    return val.CustomerId == arg;
                }
            };

            ScanQuery<Integer, CMAccums> qry = new ScanQuery<Integer, CMAccums>(pred).setLocal(true);

            iter = cache.query(qry).iterator();
        }
        else {
            if (EXPLAIN) {
                String plan = (String)cache.query(new SqlFieldsQuery("EXPLAIN SELECT * FROM CMAccums WHERE CustomerId = ?").setArgs(arg)).getAll().get(0).get(0);

                System.out.println(plan);
                System.out.println();

                EXPLAIN = false;
            }

            SqlQuery<Integer, CMAccums> qry = new SqlQuery<Integer, CMAccums>(CMAccums.class, "CustomerId = ?")
                .setArgs(arg).setLocal(true);

            iter = cache.query(qry).iterator();
        }

        int cnt = 0;

        while (iter.hasNext()) {
            iter.next();

            cnt++;
        }

        if (PRINT_CNT) {
            System.out.println(cnt);
            System.out.println();

            PRINT_CNT = false;
        }
    }

    @Override protected void tearDown() {
        Ignition.stop(true);
    }
}
