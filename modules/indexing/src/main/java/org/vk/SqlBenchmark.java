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

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.affinity.rendezvous.RendezvousAffinityFunction;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.cache.query.SqlQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.lang.IgniteBiPredicate;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

//import org.apache.ignite.configuration.DataStorageConfiguration;

public class SqlBenchmark extends Benchmark {
    private final boolean noCopy;

    public SqlBenchmark(boolean noCopy) {
        this.noCopy = noCopy;
    }

    private IgniteCache<Integer, CMAccums> cache;

    private PreparedStatement dbStmt;

    @Override protected void setup() throws Exception {
        U.delete(new File("C:\\Personal\\code\\incubator-ignite\\work"));

        IgniteConfiguration nodeCfg = new IgniteConfiguration().setLocalHost("127.0.0.1");

//        nodeCfg.setDataStorageConfiguration(new DataStorageConfiguration());
//        nodeCfg.getDataStorageConfiguration().getDefaultDataRegionConfiguration().setPersistenceEnabled(false);
//        nodeCfg.getDataStorageConfiguration().setWalMode(WALMode.BACKGROUND);
//        nodeCfg.getDataStorageConfiguration().setPageSize(4 * 1024);

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

          // MYSQL
//        BasicDataSource ds = new BasicDataSource();
//
//        ds.setUrl("jdbc:mysql://localhost:3306/mydb?serverTimezone=Europe/Moscow&max_allowed_packet=1M&verifyServerCertificate=false&useSSL=true");
//        ds.setUsername("root");
//
//        Connection conn = ds.getConnection();

        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/mydb", "root", "root");

//        // PG
//        Connection conn = DriverManager.getConnection("jdbc:postgresql:postgres", "ppoze", "Test");

        if (LOAD_DB) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM CMAccums");
            }
        }

        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
            for (int i = 0; i < 300_000; i++) {
                CMAccums acc = new CMAccums(rand.nextInt(10), rand);

                cache.put(i, acc);

                if (LOAD_DB) {
                    ps.setLong(1, acc.CustomerId);
                    ps.setString(2, acc.d);
                    ps.setLong(3, acc.id);
                    ps.setString(4, acc.merchantNumber);
                    ps.setDouble(5, acc.amount);

                    ps.setString(6, acc.t);
                    ps.setString(7, acc.cmhistory_String_Variable1);
                    ps.setString(8, acc.profile_String_Variable1);
                    ps.setLong(9, acc.profile_long_Variable1);
                    ps.setLong(10, acc.seprof_long_Variable1);

                    ps.setString(11, acc.cmhistory_String_Variable2);
                    ps.setString(12, acc.trs_String_Variable1);
                    ps.setString(13, acc.profile_String_Variable2);
                    ps.setString(14, acc.seprof_String_Variable7);
                    ps.setString(15, acc.profile_String_Variable3);

                    ps.setString(16, acc.profile_String_Variable4);
                    ps.setString(17, acc.cmhistory_String_Variable3);
                    ps.setString(18, acc.trs_String_Variable2);
                    ps.setString(19, acc.seprof_String_Variable2);
                    ps.setString(20, acc.seprof_String_Variable3);

                    ps.setLong(21, acc.seprof_long_Variable2);
                    ps.setLong(22, acc.trs_long_Variable1);
                    ps.setLong(23, acc.trs_long_Variable2);
                    ps.setString(24, acc.seprof_String_Variable4);
                    ps.setString(25, acc.seprof_String_Variable4);

                    ps.setLong(26, acc.profile_long_Variable2);
                    ps.setString(27, acc.trs_String_Variable3);
                    ps.setString(28, acc.seprof_String_Variable6);
                    ps.setDouble(29, acc.trs_double_Variable1);
                    ps.setDouble(30, acc.trs_double_Variable2);

                    ps.setDouble(31, acc.trs_double_Variable3);
                    ps.setDouble(32, acc.trs_double_Variable4);
                    ps.setDouble(33, acc.trs_double_Variable5);
                    ps.setInt(34, acc.trs_int_Variable1);
                    ps.setDouble(35, acc.seprof_double_Variable);

                    ps.setDouble(36, acc.seprof_double_Variable1);
                    ps.setString(37, acc.profile_String_Variable5);
                    ps.setString(38, acc.profile_String_Variable6);

                    ps.addBatch();
                }

                if (i % 10_000 == 0) {
                    if (LOAD_DB) {
                        ps.executeBatch();

                        System.out.println("LOADING: " + i);
                    }
                }
            }

            if (LOAD_DB)
                ps.executeBatch();
        }

        dbStmt = conn.prepareStatement("SELECT * FROM CMAccums WHERE CustomerId=?");
    }

    private static final String SQL_INSERT = "INSERT INTO CMAccums VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final boolean USE_JDBC = false;

    private static final boolean LOAD_DB = false;
    private static final boolean USE_DB = true;

    private static boolean EXPLAIN = true;
    private static boolean PRINT_CNT = true;

    private static volatile PreparedStatement IGNITE_STMT;

    @Override protected void test() throws Exception {
        int arg = ThreadLocalRandom.current().nextInt(10);

        if (USE_DB) {
            dbStmt.setInt(1, arg);

            try (ResultSet rs = dbStmt.executeQuery()) {
                int cnt = 0;

                while (rs.next())
                    cnt++;

                if (PRINT_CNT) {
                    System.out.println(cnt);
                    System.out.println();

                    PRINT_CNT = false;
                }
            }
        }
        else {
            if (EXPLAIN) {
                String plan = (String) cache.query(new SqlFieldsQuery("EXPLAIN SELECT * FROM CMAccums WHERE CustomerId=?").setArgs(arg)).getAll().get(0).get(0);

                System.out.println(plan);
                System.out.println();

                EXPLAIN = false;
            }

            if (USE_JDBC) {
                if (IGNITE_STMT == null)
                    IGNITE_STMT = DriverManager.getConnection("jdbc:ignite:thin://127.0.0.1/\"sql\"").prepareStatement("SELECT * FROM CMAccums WHERE CustomerId=?");

                IGNITE_STMT.setInt(1, arg);

                try (ResultSet rs = IGNITE_STMT.executeQuery()) {
                    int cnt = 0;

                    while (rs.next())
                        cnt++;

                    if (PRINT_CNT) {
                        System.out.println(cnt);
                        System.out.println();

                        PRINT_CNT = false;
                    }
                }
            }
            else {
                SqlQuery<Integer, CMAccums> qry = new SqlQuery<Integer, CMAccums>(CMAccums.class, "SELECT * FROM CMAccums WHERE CustomerId=?")
                    .setArgs(arg).setLocal(true);

//                SqlFieldsQuery qry = new SqlFieldsQuery("SELECT * FROM CMAccums WHERE CustomerId=?")
//                    .setArgs(arg).setLocal(true);

                Iterator iter = cache.query(qry).iterator();

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
        }
    }

    @Override protected void tearDown() {
        Ignition.stop(true);
    }
}
