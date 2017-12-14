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

package org.apache.ignite.internal.processors.cache.persistence.checkpoint;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.ignite.IgniteInterruptedException;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.internal.pagemem.FullPageId;
import org.apache.ignite.internal.util.GridMultiCollectionWrapper;
import org.apache.ignite.internal.util.future.CountDownFuture;
import org.apache.ignite.lang.IgniteBiClosure;
import org.apache.ignite.lang.IgniteBiTuple;
import org.apache.ignite.thread.IgniteThreadPoolExecutor;
import org.jetbrains.annotations.Nullable;

import static org.apache.ignite.internal.processors.cache.persistence.GridCacheDatabaseSharedManager.SEQUENTIAL_CP_PAGE_COMPARATOR;

public class AsyncCheckpointer {

    /** Checkpoint runner thread name. */
    public static final String CHECKPOINT_RUNNER = "checkpoint-runner";
    public static final FullPageId[] POISON_PILL = {};
    private volatile ForkJoinPool pageQuickSortPool;

    /** Checkpoint runner thread pool. If null tasks are to be run in single thread */
    @Nullable private ExecutorService asyncRunner;
    private IgniteLogger log;

    public AsyncCheckpointer(int checkpointThreads, String igniteInstanceName, IgniteLogger log) {
        this.log = log;
        asyncRunner = new IgniteThreadPoolExecutor(
            CHECKPOINT_RUNNER,
            igniteInstanceName,
            checkpointThreads,
            checkpointThreads,
            30_000,
            new LinkedBlockingQueue<Runnable>()
        );
    }

    private static ForkJoinTask<Integer> splitAndSortCpPagesIfNeeded3(
        ForkJoinPool pool,
        IgniteBiTuple<Collection<GridMultiCollectionWrapper<FullPageId>>, Integer> cpPagesTuple,
        BlockingQueue<FullPageId[]> queue) {
        FullPageId[] pageIds = CheckpointScope.pagesToArray(cpPagesTuple);

        final QuickSortRecursiveTask task = new QuickSortRecursiveTask(pageIds, SEQUENTIAL_CP_PAGE_COMPARATOR, queue);
        final ForkJoinTask<Integer> submit = pool.submit(task);

        return submit;
    }

    public void shutdownCheckpointer() {
        final ForkJoinPool fjPool = pageQuickSortPool;
        if (fjPool != null)
            fjPool.shutdownNow();

        if (asyncRunner != null) {
            asyncRunner.shutdownNow();

            try {
                asyncRunner.awaitTermination(2, TimeUnit.MINUTES);
            }
            catch (InterruptedException ignore) {
                Thread.currentThread().interrupt();
            }
        }

        if (fjPool != null) {
            try {
                fjPool.awaitTermination(2, TimeUnit.SECONDS);
            }
            catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public ForkJoinTask<Integer> splitAndSortCpPagesIfNeeded3(
        IgniteBiTuple<Collection<GridMultiCollectionWrapper<FullPageId>>, Integer> tuple,
        BlockingQueue<FullPageId[]> queue) {
        if (pageQuickSortPool == null) {
            synchronized (this) {
                if (pageQuickSortPool == null)
                    pageQuickSortPool = new ForkJoinPool();
            }
        }
        return splitAndSortCpPagesIfNeeded3(pageQuickSortPool, tuple, queue);
    }

    public void execute(Runnable write) {
        try {
            asyncRunner.execute(write);
        }
        catch (RejectedExecutionException ignore) {
            // Run the task synchronously.
            write.run();
        }
    }

    public CountDownFuture lazySubmit(ForkJoinTask<Integer> cpPagesChunksCntFut,
        BlockingQueue<FullPageId[]> queue,
        IgniteBiClosure<FullPageId[], CountDownFuture, Runnable> factory) {

        final int submittingTask = 1;
        CountDownDynamicFuture cntDownDynamicFut = new CountDownDynamicFuture(submittingTask);

        while (true) {
            final FullPageId[] poll;
            try {
                poll = queue.take();
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();

                throw new IgniteInterruptedException(e);
            }
            if (poll == AsyncCheckpointer.POISON_PILL)
                break;

            final Runnable runnable = factory.apply(poll, cntDownDynamicFut);
            if (log.isInfoEnabled())
                log.info("Scheduling " + poll.length + " pages write");

            cntDownDynamicFut.incrementTasksCount();

            execute(runnable);
        }
        //submit complete
        cntDownDynamicFut.onDone((Void)null);
        return cntDownDynamicFut;
    }

}
