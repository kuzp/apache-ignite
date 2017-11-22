package org.apache.ignite.bench;

import java.io.File;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.ignite.DataStorageMetrics;
import org.apache.ignite.IgniteException;
import org.apache.ignite.IgniteSystemProperties;
import org.apache.ignite.internal.IgnitionEx;

public class Benchmarks {
    private static final int MARKS = Integer.getInteger(IgniteSystemProperties.IGNITE_BENCH_MARKS, 10000);
    private static final int PERIOD = Integer.getInteger(IgniteSystemProperties.IGNITE_BENCH_PERIOD, 300);
    public static final int COLS = Integer.getInteger(IgniteSystemProperties.IGNITE_BENCH_COLS, 20);

    private static final List<Benchmark> BENCHMARKS = Collections.synchronizedList(new ArrayList());

    private static final ScheduledExecutorService SCHEDULER;

    static {
        SCHEDULER = Executors.newScheduledThreadPool(1);

        try {
            String pid = ManagementFactory.getRuntimeMXBean().getName().replaceAll("@.*", "");
            final PrintWriter aggregateLog = new PrintWriter(new File("benchmarks-" + pid + ".txt"));
            final PrintWriter threadsLog = new PrintWriter(new File("benchmarks-threads-" + pid + ".txt"));
            final PrintWriter metricsLog = new PrintWriter(new File("benchmarks-metrics-" + pid + ".txt"));

            DateFormat date = new SimpleDateFormat("yyyy-MM-dd");

            aggregateLog.print(date.format(new Date()) + "\tinvoc's\t");
            threadsLog.print(date.format(new Date()) + "\tinvoc's\t");

            for (int col = COLS; col > 0; col--) {
                aggregateLog.print(100 * col / COLS);
                aggregateLog.print("%\t");
                threadsLog.print(100 * col / COLS);
                threadsLog.print("%\t");
            }

            aggregateLog.println("Class#method");
            threadsLog.println("Class#method\tThread-name");
            metricsLog.println("lastCheckpointDataPagesNumber\tlastCheckpointTotalPagesNumber" +
                "\tlastCheckpointCopiedOnWritePagesNumber\tlastCheckpointDuration\tlastCheckpointFsyncDuration" +
                "\tlastCheckpointLockWaitDuration\tlastCheckpointMarkDuration" +
                "\twalArchiveSegments\twalFsyncTimeAverage\twalLoggingRate\twalWritingRate");

            Runnable outputStats = new Runnable() {
                public void run() {
                    DateFormat time = new SimpleDateFormat("HH:mm:ss");

                    String now = time.format(new Date());

                    for (Benchmark benchmark : BENCHMARKS)
                        benchmark.report(now, aggregateLog, threadsLog);

                    for (Benchmark benchmark : BENCHMARKS)
                        benchmark.reset();

                    aggregateLog.flush();
                    threadsLog.flush();

                    // XXX
                    DataStorageMetrics metrics = IgnitionEx.grid().dataStorageMetrics();

                    metricsLog.println(String.format("%d %d %d %d %d %d %d %d %f %f %f",
                        metrics.getLastCheckpointDataPagesNumber(),
                        metrics.getLastCheckpointTotalPagesNumber(),
                        metrics.getLastCheckpointCopiedOnWritePagesNumber(),
                        metrics.getLastCheckpointDuration(),
                        metrics.getLastCheckpointFsyncDuration(),
                        metrics.getLastCheckpointLockWaitDuration(),
                        metrics.getLastCheckpointMarkDuration(),
                        metrics.getWalArchiveSegments(),
                        metrics.getWalFsyncTimeAverage(),
                        metrics.getWalLoggingRate(),
                        metrics.getWalWritingRate()));
                    metricsLog.flush();
                }
            };

            SCHEDULER.scheduleAtFixedRate(outputStats, PERIOD, PERIOD, TimeUnit.SECONDS);


        } catch (Exception e) {
            throw new IgniteException(e);
        }
    }

    public static int marks() {
        return MARKS;
    }

    public static void register(Benchmark benchmark) {
        BENCHMARKS.add(benchmark);
    }
}
