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
import org.apache.ignite.transactions.Transaction;
import org.apache.ignite.transactions.TransactionConcurrency;
import org.apache.ignite.transactions.TransactionIsolation;

public class TxRunner {
    @SuppressWarnings({"unchecked", "TryFinallyCanBeTryWithResources"})
    public static void main(String[] args) throws Exception {
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

            final IgniteCache cache = client.getOrCreateCache(ccfg);

            Thread.sleep(500L);

//            new Thread(new Runnable() {
//                @Override public void run() {
//                    Transaction tx = client.transactions().txStart();
//
//                    cache.put(1, 1);
//
//                    try {
//                        Thread.sleep(Integer.MAX_VALUE);
//                    }
//                    catch (InterruptedException e) {
//                        // No-op.
//                    }
//                }
//            }).start();
//
//            Thread.sleep(500L);

            GridIoManager.print = true;

            Transaction tx = client.transactions().txStart(TransactionConcurrency.PESSIMISTIC, TransactionIsolation.READ_COMMITTED);

            try {
                cache.get(1);
                cache.get(1);

                cache.put(1, 2);

                tx.commit();
            }
            finally {
                tx.close();
            }

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
