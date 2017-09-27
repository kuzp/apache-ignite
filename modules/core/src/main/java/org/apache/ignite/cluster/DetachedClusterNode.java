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

package org.apache.ignite.cluster;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import org.apache.ignite.lang.IgniteProductVersion;
import org.jetbrains.annotations.Nullable;

public class DetachedClusterNode implements ClusterNode {

    private final Object consistentId;

    public DetachedClusterNode(Object consistentId) {
        this.consistentId = consistentId;
    }

    @Override public UUID id() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override public Object consistentId() {
        return consistentId;
    }

    @Nullable @Override public <T> T attribute(String name) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override public ClusterMetrics metrics() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override public Map<String, Object> attributes() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override public Collection<String> addresses() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override public Collection<String> hostNames() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override public long order() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override public IgniteProductVersion version() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override public boolean isLocal() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override public boolean isDaemon() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override public boolean isClient() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
