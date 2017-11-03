package org.apache.ignite.cache.affinity.table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.ignite.cache.affinity.AffinityFunction;
import org.apache.ignite.cache.affinity.AffinityFunctionContext;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.events.DiscoveryEvent;
import org.apache.ignite.events.EventType;
import org.apache.ignite.internal.util.typedef.internal.U;

import static org.apache.ignite.events.EventType.EVT_NODE_FAILED;
import static org.apache.ignite.events.EventType.EVT_NODE_JOINED;
import static org.apache.ignite.events.EventType.EVT_NODE_LEFT;

/**
 * Created by A.Scherbakov on 11/2/2017.
 */
public class TableAffinityFunction implements AffinityFunction {
    private int state;

    private class Assign {

    }

    public TableAffinityFunction() {
    }

    @Override public void reset() {

    }

    @Override public int partitions() {
        return 3;
    }

    @Override public int partition(Object key) {
        return U.safeAbs(key.hashCode() % 3);
    }

    @Override public List<List<ClusterNode>> assignPartitions(AffinityFunctionContext affCtx) {
        final DiscoveryEvent event = affCtx.discoveryEvent();

        switch (event.type()) {
            case EVT_NODE_JOINED:
                break;
            case EVT_NODE_LEFT:
            case EVT_NODE_FAILED:
                break;
        }

        final ClusterNode node = event.eventNode();

        final List<ClusterNode> nodes = affCtx.currentTopologySnapshot();

        List<List<ClusterNode>> res = new ArrayList<>(partitions());

        return null;
    }

    @Override public void removeNode(UUID nodeId) {

    }
}
