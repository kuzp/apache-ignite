package org.apache.ignite.yardstick.cache;

import org.apache.ignite.IgniteCache;

/**
 *
 */
public class IgnitePutAllBenchmark2kTransactionalReplicated extends IgnitePutAllBenchmark2k {
    /** {@inheritDoc} */
    @Override protected IgniteCache<Integer, Object> cache() {
        return ignite().cache("tx-cache");
    }
}
