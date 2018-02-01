package org.apache.ignite.internal.processors.cache.persistence.pagemem;

import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.ignite.DataRegionMetrics;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.cache.affinity.rendezvous.RendezvousAffinityFunction;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.IgniteInternalFuture;
import org.apache.ignite.internal.IgniteInterruptedCheckedException;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.testframework.GridTestUtils;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;

/**
 * Tests for fillFactor metrics.
 */
public class FillFactorMetricTest extends GridCommonAbstractTest {
    /** */
    private static final TcpDiscoveryVmIpFinder IP_FINDER = new TcpDiscoveryVmIpFinder(true);

    /** */
    private static final String MY_DATA_REGION = "MyPolicy";

    /** */
    private static final String MY_CACHE = "mycache";

    /** */
    public static final int NODES = 2;

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String igniteInstanceName) throws Exception {
        return super.getConfiguration(igniteInstanceName)
            .setDiscoverySpi(new TcpDiscoverySpi().setIpFinder(IP_FINDER))
            .setDataStorageConfiguration(
                new DataStorageConfiguration().setDataRegionConfigurations(
                    new DataRegionConfiguration()
                        .setName(MY_DATA_REGION)
                        .setInitialSize(100 * 1024L * 1024L)
                        .setMaxSize(200 * 1024L * 1024L)
                        .setMetricsEnabled(true)
                ));
    }

    /** */
    protected CacheConfiguration<Object, Object> cacheCfg() {
        return new CacheConfiguration<>()
            .setName(MY_CACHE)
            .setDataRegionName(MY_DATA_REGION)
            .setAffinity(new RendezvousAffinityFunction().setPartitions(16));
    }

    /** {@inheritDoc} */
    @Override protected long getTestTimeout() {
        return 300_000;
    }

    /**
     * Records counter.
     */
    private volatile int i = 0;

    /**
     * Last fill factor values.
     */
    private final float[] currentFillFactor = new float[NODES];

    /**
     * throws if failed.
     */
    public void testName() throws Exception {
        final AtomicBoolean stopLoadFlag = new AtomicBoolean();
        final AtomicBoolean doneFlag = new AtomicBoolean();

        startGrids(NODES);

        grid(0).getOrCreateCache(cacheCfg());

        final int pageSize = grid(0).configuration().getDataStorageConfiguration().getPageSize();

        IgniteInternalFuture printStatFut = GridTestUtils.runAsync(new Runnable() {
            @Override public void run() {
                while (!doneFlag.get()) {
                    printStat(0);
                    printStat(1);
                    System.out.println();

                    try {
                        U.sleep(1000);
                    }
                    catch (IgniteInterruptedCheckedException e) {
                        return;
                    }
                }
            }

            protected void printStat(int node) {
                DataRegionMetrics m = grid(node).dataRegionMetrics(MY_DATA_REGION);
                float fillFactor = m.getPagesFillFactor();
                long usedMem = (long)((m.getPhysicalMemoryPages() * pageSize)
                        * fillFactor);

                System.out.printf("Stat node-%d:\t\t%d\t\t%f\t\t%d\n",
                    node,
                    m.getPhysicalMemoryPages(),
                    fillFactor,
                    usedMem
                );

                currentFillFactor[node] = fillFactor;
            }
        });

        for (int iter = 0; iter < 5; iter++) {
            log.info("Going upward");

            stopLoadFlag.set(false);
            i = 0;

            IgniteInternalFuture loadFuture = GridTestUtils.runAsync(new Runnable() {
                @Override public void run() {
                    IgniteCache<Object, Object> cache = grid(0).cache(MY_CACHE);
                    long prime = 4294967291L;

                    while (!stopLoadFlag.get()) {
                        i++;
                        final long res = (i * i) % prime;
                        cache.put(res, new byte[1 << (res % 16)]);
                        try {
                            Thread.sleep(5);
                        }
                        catch (InterruptedException ie) {
                            return;
                        }
                    }
                }
            });

            U.sleep(30_000);

            stopLoadFlag.set(true);

            loadFuture.get();

            for (float fillFactor : currentFillFactor)
                assertTrue("FillFactor too low: " + fillFactor, fillFactor > 0.95);

            log.info("Going downward");

            IgniteInternalFuture clearFuture = GridTestUtils.runAsync(new Runnable() {
                @Override public void run() {
                    IgniteCache<Object, Object> cache = grid(0).cache(MY_CACHE);
                    long prime = 4294967291L;

                    while (i > 0) {
                        i--;
                        final long res = (i * i) % prime;
                        cache.remove(res);
                        try {
                            Thread.sleep(2);
                        }
                        catch (InterruptedException ie) {
                            return;
                        }
                    }
                }
            });

            clearFuture.get();

            for (float fillFactor : currentFillFactor)
                assertTrue("FillFactor too high: " + fillFactor, fillFactor < 0.85);
        }

        doneFlag.set(true);

        printStatFut.get();
    }
}
