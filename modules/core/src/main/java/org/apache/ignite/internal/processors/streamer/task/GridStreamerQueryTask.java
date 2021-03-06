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

package org.apache.ignite.internal.processors.streamer.task;

import org.apache.ignite.*;
import org.apache.ignite.cluster.*;
import org.apache.ignite.compute.*;
import org.apache.ignite.internal.processors.closure.*;
import org.apache.ignite.internal.util.typedef.internal.*;
import org.apache.ignite.lang.*;
import org.apache.ignite.resources.*;
import org.apache.ignite.streamer.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.util.*;

/**
 * Streamer query task.
 */
public class GridStreamerQueryTask<R> extends GridPeerDeployAwareTaskAdapter<Void, Collection<R>> {
    /** */
    private static final long serialVersionUID = 0L;

    /** Query closure. */
    private IgniteClosure<StreamerContext, R> qryClos;

    /** Streamer. */
    private String streamer;

    /**
     * @param qryClos Query closure.
     * @param streamer Streamer.
     */
    public GridStreamerQueryTask(IgniteClosure<StreamerContext, R> qryClos, @Nullable String streamer) {
        super(U.peerDeployAware(qryClos));

        this.qryClos = qryClos;
        this.streamer = streamer;
    }

    /** {@inheritDoc} */
    @Override public Map<? extends ComputeJob, ClusterNode> map(List<ClusterNode> subgrid, @Nullable Void arg) {
        Map<ComputeJob, ClusterNode> res = U.newHashMap(subgrid.size());

        for (ClusterNode node : subgrid)
            res.put(new QueryJob<>(qryClos, streamer), node);

        return res;
    }

    /** {@inheritDoc} */
    @Override public Collection<R> reduce(List<ComputeJobResult> results) {
        Collection<R> res = new ArrayList<>(results.size());

        for (ComputeJobResult jobRes : results)
            res.add(jobRes.<R>getData());

        return res;
    }

    /** {@inheritDoc} */
    @Override public ComputeJobResultPolicy result(ComputeJobResult res, List<ComputeJobResult> rcvd) {
        // No failover for this task.
        if (res.getException() != null)
            throw res.getException();

        return ComputeJobResultPolicy.WAIT;
    }

    /**
     * Query job.
     */
    private static class QueryJob<R> extends ComputeJobAdapter implements Externalizable {
        /** */
        private static final long serialVersionUID = 0L;

        /** Injected grid. */
        @IgniteInstanceResource
        private Ignite g;

        /** Query closure. */
        private IgniteClosure<StreamerContext, R> qryClos;

        /** Streamer. */
        private String streamer;

        /**
         * Empty constructor required by {@link Externalizable}.
         */
        public QueryJob() {
            // No-op.
        }

        /**
         * @param qryClos Query closure.
         * @param streamer Streamer.
         */
        private QueryJob(IgniteClosure<StreamerContext, R> qryClos, String streamer) {
            this.qryClos = qryClos;
            this.streamer = streamer;
        }

        /** {@inheritDoc} */
        @Override public Object execute() {
            IgniteStreamer s = g.streamer(streamer);

            assert s != null;

            return qryClos.apply(s.context());
        }

        /** {@inheritDoc} */
        @Override public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(qryClos);
            U.writeString(out, streamer);
        }

        /** {@inheritDoc} */
        @Override public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            qryClos = (IgniteClosure<StreamerContext, R>)in.readObject();
            streamer = U.readString(in);
        }
    }
}
