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

package org.apache.ignite.internal.processors.query.h2.database;

import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.IgniteException;
import org.apache.ignite.internal.pagemem.PageIdUtils;
import org.apache.ignite.internal.processors.cache.GridCacheContext;
import org.apache.ignite.internal.processors.cache.GridCacheEntryEx;
import org.apache.ignite.internal.processors.cache.GridCacheEntryRemovedException;
import org.apache.ignite.internal.processors.cache.KeyCacheObject;
import org.apache.ignite.internal.processors.cache.persistence.CacheDataRowAdapter;
import org.apache.ignite.internal.processors.query.h2.IgniteH2Indexing;
import org.apache.ignite.internal.processors.query.h2.opt.GridH2QueryContext;
import org.apache.ignite.internal.processors.query.h2.opt.GridH2Row;
import org.apache.ignite.internal.processors.query.h2.opt.GridH2RowDescriptor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Data store for H2 rows.
 */
public class H2RowFactory {
    /** Indexing. */
    private final IgniteH2Indexing idx;

    /** */
    private final GridCacheContext<?,?> cctx;

    /** */
    private final GridH2RowDescriptor rowDesc;

    /**
     * Constructor.
     *
     * @param idx Indexing.
     * @param rowDesc Row descriptor.
     * @param cctx Cache context.
     */
    public H2RowFactory(IgniteH2Indexing idx, GridH2RowDescriptor rowDesc, GridCacheContext<?,?> cctx) {
        this.idx = idx;
        this.rowDesc = rowDesc;
        this.cctx = cctx;
    }

    private static final ConcurrentHashMap<Long, GridH2Row> ROW_CACHE = new ConcurrentHashMap<>();

    /**
     * !!! This method must be invoked in read or write lock of referring index page. It is needed to
     * !!! make sure that row at this link will be invisible, when the link will be removed from
     * !!! from all the index pages, so that row can be safely erased from the data page.
     *
     * @param link Link.
     * @return Row.
     * @throws IgniteCheckedException If failed.
     */
    public GridH2Row getRow(long link) throws IgniteCheckedException {
        GridH2Row row = ROW_CACHE.get(link);

        if (row == null) {
            // TODO Avoid extra garbage generation. In upcoming H2 1.4.193 Row will become an interface,
            // TODO we need to refactor all this to return CacheDataRowAdapter implementing Row here.

            final CacheDataRowAdapter rowBuilder = new CacheDataRowAdapter(link);

            rowBuilder.initFromLink(cctx.group(), CacheDataRowAdapter.RowData.FULL);

            try {
                row = rowDesc.createRow(rowBuilder.key(), PageIdUtils.partId(link), rowBuilder.value(),
                    rowBuilder.version(), rowBuilder.expireTime());

                row.link = link;
            }
            catch (IgniteCheckedException e) {
                throw new IgniteException(e);
            }

            assert row.ver != null;

            ROW_CACHE.put(link, row);
        }
        else {
            CacheDataRowAdapter rowBuilder = new CacheDataRowAdapter(link);

            rowBuilder.initFromLink(cctx.group(), CacheDataRowAdapter.RowData.KEY_ONLY);

            KeyCacheObject key = rowBuilder.key();

            key.partition(PageIdUtils.partId(link));

            GridCacheEntryEx entry = cctx.cache().peekEx(key);

            if (entry != null) {
                try {
                    row = rowDesc.createRow(rowBuilder.key(), PageIdUtils.partId(link), entry.valueBytes(),
                        entry.version(), entry.expireTime());
                }
                catch (GridCacheEntryRemovedException e) {
                    // No-op.
                }
            }
        }

        return row;
    }
}
