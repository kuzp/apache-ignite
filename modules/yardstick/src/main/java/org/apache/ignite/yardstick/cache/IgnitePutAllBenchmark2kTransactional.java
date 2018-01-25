package org.apache.ignite.yardstick.cache;

import org.apache.ignite.IgniteCache;

/**
 *
 */
public class IgnitePutAllBenchmark2kTransactional extends IgnitePutAllBenchmark2kReplicated {
    /** {@inheritDoc} */
    @Override protected IgniteCache<Integer, Object> cache() {
        return ignite().cache("tx-cache-replicated");
    }
}
