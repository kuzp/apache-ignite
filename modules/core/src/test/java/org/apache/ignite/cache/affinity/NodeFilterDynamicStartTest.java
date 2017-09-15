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
package org.apache.ignite.cache.affinity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.MemoryConfiguration;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.internal.util.lang.gridfunc.AlwaysFalsePredicate;
import org.apache.ignite.lang.IgnitePredicate;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.apache.ignite.util.AttributeNodeFilter;
import org.apache.log4j.Logger;

/**
 * NodeFilterDynamicStartTest
 *
 * @author Alexandr Kuramshin <ein.nsk.ru@gmail.com>
 */
public class NodeFilterDynamicStartTest extends GridCommonAbstractTest {

    /**  */
    private static final Logger LOG = Logger.getLogger(NodeFilterDynamicStartTest.class);

    /**  */
    private static final int NODE_COUNT = 8;

    /**  */
    private static final String GRID_INDEX_ATTR = "gridIndex";

    /**  */
    @Override public String getTestIgniteInstanceName() {
        return "testGrid";
    }

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String igniteInstanceName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(igniteInstanceName);

        MemoryConfiguration mcfg = new MemoryConfiguration();
        mcfg.setDefaultMemoryPolicySize(100L << 20);
        cfg.setMemoryConfiguration(mcfg);

        int gridIdx = getTestIgniteInstanceIndex(igniteInstanceName);
        cfg.setUserAttributes(Collections.singletonMap(GRID_INDEX_ATTR, gridIdx));
        return cfg;
    }

    /**  */
    public void test() throws Exception {
        final CacheConfiguration testCacheCfg = new CacheConfiguration();
        testCacheCfg.setName("testCache");
        testCacheCfg.setNodeFilter(new CustomNodeFilter());

        final CountDownLatch startLatch1 = new CountDownLatch(NODE_COUNT);
        final CountDownLatch startLatch2 = new CountDownLatch(NODE_COUNT);
        final CountDownLatch stopLatch = new CountDownLatch(NODE_COUNT);
        final AtomicReference<Throwable> err = new AtomicReference<>();

        for (int i = 0; i < NODE_COUNT; ++i) {
            final int gridIdx = i;
            IgniteEx g;
            try {
                g = startGrid(gridIdx);
                g.log().error("$> start grid: name = " + g.name() + ", id = " + g.localNode().id());
                Thread.sleep(100L);
            }
            catch (Throwable e) {
                LOG.error("Failed to start grid: gridIdx = " + gridIdx, e);
                stopLatch.countDown();
                continue;
            }
            final IgniteEx grid = g;
            new Thread() {
                @Override public void run() {
                    try {
                        boolean affNode = testCacheCfg.getNodeFilter().apply(grid.localNode());
                        Collection<CacheConfiguration> ccfgs = new ArrayList<>();
                        for (int j = 0; j < 10; ++j) {
                            CacheConfiguration ccfg = new CacheConfiguration(testCacheCfg);
                            ccfg.setName(testCacheCfg.getName() + j);
                            ccfgs.add(ccfg);
                        }

                        startLatch1.countDown();
                        startLatch1.await();

                        if (gridIdx == 1) {
//                        grid.getOrCreateCaches(ccfgs);
                            for (int j = 10; j < 20; ++j) {
                                CacheConfiguration ccfg = new CacheConfiguration(testCacheCfg);
                                ccfg.setName(testCacheCfg.getName() + j);
                                ccfg.setNodeFilter(new AlwaysFalsePredicate<ClusterNode>());
                                grid.getOrCreateCache(ccfg);
                            }
                        }

                        Thread.sleep(1000L);

                        startLatch2.countDown();
                        startLatch2.await();
                    }
                    catch (Throwable e) {
                        err.compareAndSet(null, e);
                        grid.log().error("Dynamic start cache error", e);
                    }
                    finally {
                        stopLatch.countDown();
                    }
                }
            }.start();
        }
        stopLatch.await();
        Thread.sleep(1000L);
        stopAllGrids();

        if (err.get() != null)
            throw new AssertionError("Dynamic start cache error", err.get());
    }

    /**  */
    private static class CustomNodeFilter implements IgnitePredicate<ClusterNode> {

        /**  */
        @Override public boolean apply(ClusterNode node) {
            int gridIdx = node.attribute(GRID_INDEX_ATTR);
            return gridIdx % 4 == 0;
        }

    }
}
