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

package org.apache.ignite.internal.processors.query.h2;

import org.apache.ignite.internal.processors.cache.GridCacheContext;
import org.apache.ignite.internal.processors.query.h2.opt.GridH2KeyValueRowOnheap;
import org.apache.ignite.internal.util.typedef.F;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * H2 row cache.
 */
public class H2RowCache {
    /** Cached rows. */
    private ConcurrentHashMap<Long, GridH2KeyValueRowOnheap> rows = new ConcurrentHashMap<>();

    /** Usage count. */
    private int usageCnt = 1;

    /**
     * Get row by link.
     *
     * @param link Link.
     * @return Rows.
     */
    public GridH2KeyValueRowOnheap get(long link) {
        return rows.get(link);
    }

    /**
     * Put row by link.
     *
     * @param row Row.
     */
    public void put(GridH2KeyValueRowOnheap row) {
        rows.put(row.link(), row);
    }

    /**
     * Remove row by link.
     *
     * @param link Link.
     */
    public void remove(long link) {
        rows.remove(link);
    }

    /**
     * Cache registration callback.
     */
    public void onCacheRegistered() {
        usageCnt++;
    }

    /**
     * Cache un-registration callback.
     *
     * @param cctx Cache context.
     * @return {@code True} if there are no more usages for the given cache group.
     */
    public boolean onCacheUnregistered(GridCacheContext cctx) {
        boolean res = --usageCnt == 0;

        clearForCache(cctx);

        return res;
    }

    /**
     * Clear entries belonging to the given cache.
     *
     * @param cctx Cache context.
     */
    private void clearForCache(GridCacheContext cctx) {
        int cacheId = cctx.cacheId();

        Iterator<Map.Entry<Long, GridH2KeyValueRowOnheap>> iter = rows.entrySet().iterator();

        while (iter.hasNext()) {
            GridH2KeyValueRowOnheap row = iter.next().getValue();

            if (F.eq(cacheId, row.cacheId()))
                iter.remove();
        }
    }
}
