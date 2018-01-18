package org.apache.ignite.yardstick.cache;

import org.apache.ignite.IgniteCache;

/**
 *
 */
public class IgnitePutAllBenchmark2kReplicated extends IgnitePutAllBenchmark2k {
    /** {@inheritDoc} */
    @Override protected IgniteCache<Integer, Object> cache() {
        return ignite().cache("atomic-replicated");
    }
}
