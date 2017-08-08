package org.apache.ignite.internal.processors.cache.persistence.db.file;

import org.apache.ignite.IgniteCache;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.PersistentStoreConfiguration;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.internal.processors.cache.persistence.wal.FileWriteAheadLogManager;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.lang.IgniteCallable;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.TcpDiscoveryIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;

/**
 *
 */
public class IgniteWalCompressorTest extends GridCommonAbstractTest {

    private static final TcpDiscoveryIpFinder ipFinder = new TcpDiscoveryVmIpFinder(true);

    private static final String CACHE_NAME = "cache";

    @Override protected IgniteConfiguration getConfiguration(String name) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(name);

        cfg.setConsistentId(name);
        cfg.setDiscoverySpi(new TcpDiscoverySpi().setIpFinder(ipFinder));
        cfg.setPersistentStoreConfiguration(
            new PersistentStoreConfiguration()
                .setWalSegments(3)
                .setWalSegmentSize(1024 * 1024 * 20)
        );

        CacheConfiguration ccfg = new CacheConfiguration(CACHE_NAME);

        cfg.setCacheConfiguration(ccfg);

        return cfg;
    }

    @Override protected void beforeTest() throws Exception {
        deleteRecursively(U.resolveWorkDirectory(U.defaultWorkDirectory(), "db", false));
    }

    public void test() throws Exception {
        final IgniteEx ig = startGrid(0);

        ig.active(true);

        final FileWriteAheadLogManager walMgr = (FileWriteAheadLogManager)ig.context().cache().context().wal();

        loadWalRecords(ig, new IgniteCallable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return walMgr.walArchiveSegments() > 0;
            }
        });

        int segInArchive = walMgr.walArchiveSegments();

        Thread.sleep(60_000 * 5);
    }

    private void loadWalRecords(
        IgniteEx ig,
        IgniteCallable<Boolean> stopCondition
    ) throws Exception {
        IgniteCache<Long, byte[]> cache = ig.cache(CACHE_NAME);

        long cnt = 0;

        while (true) {
            if (stopCondition.call())
                break;
            else {
                cache.put(cnt, new byte[1024 * 1024]);

                cache.remove(cnt);

                System.out.println("Put/Remove entries:" + cnt);

                cnt++;
            }
        }
    }
}
