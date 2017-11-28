package org.apache.ignite.internal.processors.cache;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.util.future.GridCompoundFuture;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.TcpDiscoveryIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.testframework.GridTestUtils;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class IgniteCacheGroupDestroyTest extends GridCommonAbstractTest {
    /** */
    protected static TcpDiscoveryIpFinder ipFinder = new TcpDiscoveryVmIpFinder(true);

    /** */
    private static final Map<Integer, Integer> map = new HashMap<Integer, Integer>() {{
        put(1, 1);
        put(2, 2);
        put(3, 3);
    }};

    /** */
    private enum DSTR_NODES {
        /** */
        SERVER,

        /** */
        CLIENT,

        /** */
        ASYNC;
    }

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String igniteInstanceName) throws Exception {
        IgniteConfiguration icfg = super.getConfiguration(igniteInstanceName);

        ((TcpDiscoverySpi)icfg.getDiscoverySpi()).setIpFinder(ipFinder);

        if (igniteInstanceName.endsWith("client"))
            icfg.setClientMode(true);

        DataStorageConfiguration memCfg = new DataStorageConfiguration().setDefaultDataRegionConfiguration(
                new DataRegionConfiguration().setMaxSize(200 * 1024 * 1024).setPersistenceEnabled(true));

        icfg.setDataStorageConfiguration(memCfg);

        return icfg;
    }

    /**
     * @throws Exception If failed.
     */
    @Override protected void afterTest() throws Exception {
        super.afterTest();

        stopAllGrids();
    }

    @Override protected void afterTestsStopped() throws Exception {
        deleteRecursively(U.resolveWorkDirectory(U.defaultWorkDirectory(), "db", false));
    }

    /**
     * @return Default test case timeout.
     */
    @Override protected long getTestTimeout() {
        return 600000;
    }

    /**
     * @throws Exception If failed.
     */
    public void testStartMultipleClientCaches() throws Exception {
        startMultipleConfigCaches("testGrp");

        startMultipleConfigCaches(null);

        startMultipleConfigCaches("");
    }

    /**
     * @param grp Caches group name.
     * @throws Exception If failed.
     */
    private void startMultipleConfigCaches(@Nullable String grp) throws Exception {
        final int SRVS = 1;

        Ignite srv = startGrids(SRVS);

        Ignite client = startGrid("client");

        client.active(true);

        for (CacheAtomicityMode atomicityMode : CacheAtomicityMode.values())
            for (DSTR_NODES cntDestroy : DSTR_NODES.values())
                startCachesForGroup(srv, client, grp, atomicityMode, cntDestroy);

        stopAllGrids();
    }

    /**
     * @param srv Server node.
     * @param client Client node.
     * @param grp Cache group.
     * @param atomicityMode Cache atomicity mode.
     * @throws Exception If failed.
     */
    @SuppressWarnings("unchecked")
    private void startCachesForGroup(final Ignite srv,
                                     final Ignite client,
                                     @Nullable final String grp,
                                     CacheAtomicityMode atomicityMode,
                                     DSTR_NODES nodes) throws Exception {
        log.info("Start caches [grp=" + grp + ", atomicity=" + atomicityMode +
                ", clntDestroy=" + nodes + ']');

        Collection<IgniteCache> caches = srv.createCaches(cacheConfigurations(grp, atomicityMode));

        for (IgniteCache<Integer, Integer> cache : caches)
            cache.putAll(map);

        switch (nodes) {
            case ASYNC:
                GridCompoundFuture<Object, Object> comp = new GridCompoundFuture<>();

                comp.add(GridTestUtils.runAsync(new Runnable() {
                    @Override public void run() {
                        client.destroyCacheGroup(grp);
                    }
                }));

                comp.add(GridTestUtils.runAsync(new Runnable() {
                    @Override public void run() {
                        srv.destroyCacheGroup(grp);
                    }
                }));

                comp.markInitialized();

                comp.get();

                break;

            case CLIENT:
                client.destroyCacheGroup(grp);

                break;

            case SERVER:
                srv.destroyCacheGroup(grp);

                break;
        }

        for (IgniteCache<Integer, Integer> cache : caches)
            assertNull(nodes.equals(DSTR_NODES.SERVER) ?
                client.cache(cache.getName()) : srv.cache(cache.getName()));
    }

    /**
     * @param grp Group name.
     * @param atomicityMode Atomicity mode.
     * @return Cache configurations.
     */
    private List<CacheConfiguration> cacheConfigurations(@Nullable String grp, CacheAtomicityMode atomicityMode) {
        List<CacheConfiguration> ccfgs = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            CacheConfiguration ccfg = new CacheConfiguration();

            ccfg.setGroupName(grp);

            ccfg.setName("cache-" + atomicityMode + "-" + i);

            ccfgs.add(ccfg);
        }

        return ccfgs;
    }
}
