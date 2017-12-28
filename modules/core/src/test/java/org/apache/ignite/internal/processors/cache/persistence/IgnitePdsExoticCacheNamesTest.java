/* @java.file.header */

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.apache.ignite.internal.processors.cache.persistence;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.TcpDiscoveryIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;

import static org.apache.ignite.internal.processors.cache.persistence.file.FilePageStoreManager.DFLT_STORE_DIR;

/**
 * Tests for persistent caches with tricky names: with special characters, non-ASCII symbols, and with names that
 * are equal ignoring case.
 */
public class IgnitePdsExoticCacheNamesTest extends GridCommonAbstractTest {
    /** */
    private static final TcpDiscoveryIpFinder IP_FINDER = new TcpDiscoveryVmIpFinder(true);

    /** */
    private static final String CACHE_NAME_BASE = IgnitePdsExoticCacheNamesTest.class.getSimpleName() + "-";

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String igniteInstanceName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(igniteInstanceName);

        DataStorageConfiguration storageCfg = new DataStorageConfiguration();
        storageCfg.getDefaultDataRegionConfiguration().setPersistenceEnabled(true);
        cfg.setDataStorageConfiguration(storageCfg);

        ((TcpDiscoverySpi)cfg.getDiscoverySpi()).setIpFinder(IP_FINDER);

        return cfg;
    }

    /** {@inheritDoc} */
    @Override protected void beforeTestsStarted() throws Exception {
        // clean persistent store
        deleteRecursively(U.resolveWorkDirectory(U.defaultWorkDirectory(), DFLT_STORE_DIR, false));
    }

    /** {@inheritDoc} */
    @Override protected void afterTest() throws Exception {
        // clean persistent store
//        deleteRecursively(U.resolveWorkDirectory(U.defaultWorkDirectory(), DFLT_STORE_DIR, false));
    }

    /** Test a persistent cache with slashes in the name */
    public void testNameWithSlashes() throws Exception {
        checkPersistentCaches(CACHE_NAME_BASE + "my/cool/cache");
    }

    /** Test a persistent cache with special symbols in the name */
    public void testNameWithSpecialSymbols() throws Exception {
        checkPersistentCaches(CACHE_NAME_BASE + "my!@#$%^&()cache");
    }

    /** Test a persistent cache with control symbols in the name */
    public void testNameWithControlSymbols() throws Exception {
        checkPersistentCaches(CACHE_NAME_BASE + "my\0\1\2\3\4\5\6\7cache");
    }

    /** Test a persistent cache with control symbols in the name */
    public void testNameWithUnicodeSymbols() throws Exception {
        checkPersistentCaches(CACHE_NAME_BASE + "my\u0431\u0430\u0431\u0443\u0448\u043a\u0430cache");
    }

    /** Test a pair of persistent cache that have names that are equal ignoring case */
    public void testNamesThatAreEqualIgnoringCase() throws Exception {
        checkPersistentCaches(CACHE_NAME_BASE + "mycache", CACHE_NAME_BASE + "MYCACHE");
    }

    /**
     * Checks that persistent caches with the specified names can be created and work as expected.
     * The method takes variable arguments for the cases when multiple caches need to be checked together
     * (e.g. when {@code name1.equalsIgnoreCase(name2)})
     *
     * @param cacheNames cache names to test
     * @throws Exception If failed.
     */
    private void checkPersistentCaches(String... cacheNames) throws Exception {
        // start grid
        Ignite ignite = startGrid();
        ignite.active(true);

        // create caches with the specified names and put values into them
        for (String cacheName : cacheNames) {
            CacheConfiguration<String, String> cacheCfg = new CacheConfiguration<>(cacheName);
            cacheCfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
            cacheCfg.setBackups(1);
            cacheCfg.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC);
            IgniteCache<String, String> cache = ignite.getOrCreateCache(cacheName);
            cache.put(cacheName + "::foo", cacheName + "::bar");
        }

        // stop grid
        stopGrid();

        // restart grid
        ignite = startGrid();
        ignite.active(true);

        // read values from caches created before
        for (String cacheName : cacheNames) {
            IgniteCache<String, String> cache = ignite.cache(cacheName);
            String val = cache.get(cacheName + "::foo");
            // check that the value is as expected
            assertEquals(cacheName + "::bar", val);
        }

        // stop grid
        stopGrid();
    }
}
