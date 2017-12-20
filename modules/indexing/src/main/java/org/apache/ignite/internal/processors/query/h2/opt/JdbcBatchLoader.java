package org.apache.ignite.internal.processors.query.h2.opt;

import java.sql.DriverManager;
import org.apache.ignite.internal.util.typedef.internal.U;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *  Loader via JDBC.
 */
public class JdbcBatchLoader {
    /** */
    private static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS Person(" +
        " id integer PRIMARY KEY," +
        " name varchar(50)," +
        " age integer," +
        " salary integer" +
        ")";

    /** */
    private static final String SQL_INSERT = "INSERT INTO Person(id, name, age, salary) VALUES (?, ?, ?, ?)";

    /**
     * @param msg Message to log.
     */
    private static void log(String msg) {
        U.debug(msg);
    }

    /**
     * Main entry point.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        try {
            JdbcBatchLoader ldr = new JdbcBatchLoader();

            ldr.load(10_000_000, 1000, 8, "xx.xx.xx.xx:yyyy");
        }
        catch (Exception e) {
            log("Failed to load data into cloud");

            e.printStackTrace();
        }
    }

    /**
     * Load data into cloud.
     *
     * @param total Total number of rows to lad.
     * @param batch Batch size.
     * @param threads How many threads to use.
     * @param addr JDBC endpoint address.
     * @throws Exception If failed to load data to cloud.
     */
    public void load(int total, int batch, int threads, String addr) throws Exception {
        ExecutorService exec = Executors.newFixedThreadPool(threads);

        log("Connecting to IGNITE...");

        try(Connection conn = DriverManager.getConnection("jdbc:ignite:thin://" + addr)) {
            Statement stmt = conn.createStatement();

            stmt.execute(SQL_CREATE);

            CountDownLatch latch = new CountDownLatch(threads);

            log("Start loading of " + total + " records...");

            long start = System.currentTimeMillis();

            int packetSz = total / threads;

            for (int i = 0; i < threads; i++)
                exec.execute(new Worker(addr, i, packetSz, batch, latch));

            latch.await();

            U.closeQuiet(stmt);

            log("Loading time: " + (System.currentTimeMillis() - start) / 1000 + "seconds");
            log("Loading finished!");
        }

        U.shutdownNow(JdbcBatchLoader.class, exec, null);
    }

    /**
     * Class that execute batch loading.
     */
    private static class Worker implements Runnable {
        /** */
        private final String addr;

        /** */
        private final int grp;

        /** */
        private final int batch;

        /** */
        private final CountDownLatch latch;

        /** */
        private final int start;

        /** */
        private final int finish;

        /**
         *
         * @param addr Connect to addr.
         * @param grp Group ID.
         * @param packetSz Packet ID.
         * @param batch Batch size.
         * @param latch Control latch to complete loading.
         */
        private Worker(String addr, int grp, int packetSz, int batch, CountDownLatch latch) {
            this.addr = addr;
            this.grp = grp;
            this.batch = batch;
            this.latch = latch;

            start = grp * packetSz;
            finish = start + packetSz;
        }

        /** {@inheritDoc} */
        @Override public void run() {
            log("Start group: " + grp);

            try(Connection conn = DriverManager.getConnection("jdbc:ignite:thin://" + addr)) {
                PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT);

                int j = 0;

                for (int i = start; i < finish; i++) {
                    pstmt.setInt(1, i);
                    pstmt.setString(2, "Some name" + i);
                    pstmt.setInt(3, 100);
                    pstmt.setInt(4, 200);

                    pstmt.addBatch();

                    j++;

                    if (j == batch) {
                        pstmt.executeBatch();

                        j = 0;
                    }
                }

                if (j > 0)
                    pstmt.executeBatch();

                Statement stmt = conn.createStatement();

                stmt.execute("FLUSH");

                U.closeQuiet(stmt);
                U.closeQuiet(pstmt);
            }
            catch (Throwable e) {
                log("Failed to load group: [grp=" + grp + ", err=" + e.getMessage() + "]");

                e.printStackTrace();
            }
            finally {
                latch.countDown();

                log("Finished group: " + grp);
            }
        }
    }
}
