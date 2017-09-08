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
 */
package org.apache.ignite.internal.processors.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.apache.ignite.cluster.ClusterNode;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class BaselineTopologyImpl {
    private Collection<Object> nodeIds;

    public BaselineTopologyImpl(Collection<ClusterNode> nodes) {
        nodeIds = new ArrayList(nodes.size());

        Iterator<ClusterNode> iter = nodes.iterator();

        while (iter.hasNext())
            nodeIds.add(iter.next().consistentId());
    }

    public boolean isSatisfied(@NotNull Collection<ClusterNode> presentedNodes) {
        if (presentedNodes.size() < nodeIds.size())
            return false;

        if (presentedNodes.size() == nodeIds.size()) {
            Iterator<ClusterNode> iter = presentedNodes.iterator();

            int cntr = 0;

            while (iter.hasNext()) {
                ClusterNode nextNode = iter.next();

                if (nodeIds.contains(nextNode.consistentId()))
                    cntr++;
            }

            if (cntr == nodeIds.size())
                return true;
        }

        return false;
    }
}
