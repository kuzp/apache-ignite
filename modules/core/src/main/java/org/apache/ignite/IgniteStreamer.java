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

package org.apache.ignite;

import org.apache.ignite.streamer.*;

import java.util.*;

/**
 * Streamer interface. Streamer provides an easy way to process large (possibly infinite) stream of
 * events. Event can be of any object type, different types of events can be submitted to streamer. Each event
 * is processed by one or more {@link org.apache.ignite.streamer.StreamerStage}, a set of stages event passed through is called pipeline.
 * <p>
 * For each submitted group of events streamer determines one or more execution nodes that will process this
 * group of events. Execution nodes are determined by {@link org.apache.ignite.streamer.StreamerEventRouter}. Execution nodes run stages
 * with received events. After stage execution streamer gets an optional set of events that should be processed
 * further. The process is repeated until stage returns empty map. After stage returned empty map pipeline execution
 * for given group of events is finished.
 * <p>
 * It is guaranteed that group of events returned by router will be neither split nor concatenated with
 * any other group of events and will be passed to stage as is. Event processing order is not guaranteed, group that
 * was submitted second can be processed earlier then first submitted group.
 * <p>
 * If {@link StreamerConfiguration#isAtLeastOnce()} is set to {@code false}, then event execution is not tracked
 * by streamer and any occurred failure will be reported to failure listener on node on which failure happened. If
 * this configuration property is set to {@code true}, then streamer will cancel current pipeline execution in case
 * of failure and will try to execute pipeline from the beginning. If failover cannot be succeeded or maximum number
 * of failover attempts is exceeded, then listener will be notified on node which originated pipeline execution.
 *
 * @see org.apache.ignite.streamer.StreamerStage
 * @see org.apache.ignite.streamer.StreamerEventRouter
 */
public interface IgniteStreamer {
    /**
     * Gets streamer configuration.
     *
     * @return Streamer configuration.
     */
    public StreamerConfiguration configuration();

    /**
     * Gets streamer name.
     *
     * @return Streamer name, or {@code null} for default no-name streamer.
     */
    public String name();

    /**
     * Submits group of events for processing. This group of events will be processed on default stage,
     * i.e. stage that is the first in the streamer stages list.
     *
     * @param evt Event to add.
     * @param evts Optional events to add.
     * @throws IgniteException If event submission failed.
     */
    public void addEvent(Object evt, Object... evts) throws IgniteException;

    /**
     * Submits group of events to streamer. Events will be processed from a stage with specified name.
     *
     * @param stageName Stage name to start with.
     * @param evt Event tp process.
     * @param evts Optional events.
     * @throws IgniteException If event submission failed.
     */
    public void addEventToStage(String stageName, Object evt, Object... evts) throws IgniteException;

    /**
     * Submits group of events for processing. This group of events will be processed on default stage,
     * i.e. stage that is the first in the streamer stages list.
     *
     * @param evts Events to add.
     * @throws IgniteException If event submission failed.
     */
    public void addEvents(Collection<?> evts) throws IgniteException;

    /**
     * Submits events to streamer. Events will be processed from a stage with specified name.
     *
     * @param stageName Stage name to start with.
     * @param evts Events to process.
     * @throws IgniteException If event submission failed.
     */
    public void addEventsToStage(String stageName, Collection<?> evts) throws IgniteException;

    /**
     * Gets streamer context. Streamer context provides access to streamer local space on this node, configured
     * streamer windows and provides various methods to run streamer queries.
     *
     * @return Streamer context.
     */
    public StreamerContext context();

    /**
     * Adds streamer failure listener. Listener will be notified on node on which failure occurred in case if
     * {@link StreamerConfiguration#isAtLeastOnce()} is set to {@code false} and on node which originated
     * pipeline execution otherwise.
     *
     * @param lsnr Listener to add.
     */
    public void addStreamerFailureListener(StreamerFailureListener lsnr);

    /**
     * Removes streamer failure listener.
     *
     * @param lsnr Listener to remove.
     */
    public void removeStreamerFailureListener(StreamerFailureListener lsnr);

    /**
     * Gets current streamer metrics.
     *
     * @return Streamer metrics.
     */
    public StreamerMetrics metrics();

    /**
     * Resets all configured streamer windows by calling {@link org.apache.ignite.streamer.StreamerWindow#reset()} on each and
     * clears local space.
     * <p>
     * This is local method, it will clear only local windows and local space. Note that windows and
     * space will not be cleaned while stages are executing, i.e. all currently running stages will
     * have to complete before streamer can be reset.
     */
    public void reset();

    /**
     * Resets all streamer metrics.
     */
    public void resetMetrics();

    /**
     * Explicitly sets deployment class. Will be used only if peer-to-peer class loading
     * is enabled.
     *
     * @param depCls Deployment class.
     */
    public void deployClass(Class<?> depCls);
}
