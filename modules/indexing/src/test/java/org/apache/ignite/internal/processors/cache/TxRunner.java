package org.apache.ignite.internal.processors.cache;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.managers.communication.GridIoManager;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

public class TxRunner {
    @SuppressWarnings({"unchecked", "TryFinallyCanBeTryWithResources"})
    public static void main(String[] args) {
        TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder(true);

        Ignite node1 = Ignition.start(config("node1", ipFinder));
        Ignite node2 = Ignition.start(config("node2", ipFinder));

        Ignite client = Ignition.start(config("cli", ipFinder).setClientMode(true));

        try {
            CacheConfiguration ccfg = new CacheConfiguration()
                .setName("cache")
                .setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL)
                .setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC)
                .setBackups(1);

            IgniteCache cache = client.getOrCreateCache(ccfg);

            GridIoManager.print = true;

            cache.put(1, 2);

            GridIoManager.print = false;
        }
        finally {
            node1.close();
            node2.close();

            client.close();
        }
    }

    private static IgniteConfiguration config(String name, TcpDiscoveryVmIpFinder ipFinder) {
        IgniteConfiguration cfg = new IgniteConfiguration().setLocalHost("127.0.0.1").setIgniteInstanceName(name);

        TcpDiscoverySpi discoSpi = new TcpDiscoverySpi();

        discoSpi.setIpFinder(ipFinder);

        cfg.setDiscoverySpi(discoSpi);

        return cfg;
    }
}
