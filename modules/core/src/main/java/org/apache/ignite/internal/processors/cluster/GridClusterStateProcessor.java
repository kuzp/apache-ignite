/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.ignite.internal.processors.cluster;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.internal.IgniteInternalFuture;
import org.apache.ignite.internal.managers.discovery.DiscoCache;
import org.apache.ignite.internal.processors.GridProcessor;
import org.apache.ignite.internal.processors.affinity.AffinityTopologyVersion;
import org.apache.ignite.internal.processors.cache.StateChangeRequest;
import org.jetbrains.annotations.Nullable;

public interface GridClusterStateProcessor extends GridProcessor {
    boolean publicApiActiveState();

    @Nullable IgniteInternalFuture<Boolean> onLocalJoin(DiscoCache discoCache);

    @Nullable ChangeGlobalStateFinishMessage onNodeLeft(ClusterNode node);

    void onStateFinishMessage(ChangeGlobalStateFinishMessage msg);

    boolean onStateChangeMessage(AffinityTopologyVersion topVer,
        ChangeGlobalStateMessage msg,
        DiscoCache discoCache);

    DiscoveryDataClusterState clusterState();

    void cacheProcessorStarted();

    IgniteInternalFuture<?> changeGlobalState(boolean activate);

    IgniteInternalFuture<?> changeGlobalState(boolean activate,
        Collection<ClusterNode> baselineNodes);

    ChangeGlobalStateFinishMessage createChangeGlobalStateFinishMessage(ChangeGlobalStateMessage req,
        boolean clusterActive, @Nullable BaselineTopology baselineTopology);

    void onStateChangeError(Map<UUID, Exception> errs, StateChangeRequest req);

    void onStateChangeExchangeDone(StateChangeRequest req);

    void onBaselineTopologyReady();
}
