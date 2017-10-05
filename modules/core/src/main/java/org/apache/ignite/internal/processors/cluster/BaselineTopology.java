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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.internal.util.typedef.F;
import org.apache.ignite.lang.IgnitePredicate;

public class BaselineTopology implements Serializable {

    private final Set<Object> consistentIds;

    private final boolean ready;

    public BaselineTopology(Set<Object> consistentIds, boolean ready) {
        this.consistentIds = consistentIds;
        this.ready = ready;
    }

    public Set<Object> consistentIds() {
        return consistentIds;
    }

    public boolean ready() {
        return ready;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        BaselineTopology topology = (BaselineTopology)o;

        return consistentIds != null ? consistentIds.equals(topology.consistentIds) : topology.consistentIds == null;
    }

    @Override public int hashCode() {
        return consistentIds != null ? consistentIds.hashCode() : 0;
    }

    public static boolean equals(BaselineTopology blt1, BaselineTopology blt2) {
        if (blt1 == null && blt2 == null)
            return true;

        if (blt1 == null ^ blt2 == null)
            return false;

        return blt1.equals(blt2);
    }
}
