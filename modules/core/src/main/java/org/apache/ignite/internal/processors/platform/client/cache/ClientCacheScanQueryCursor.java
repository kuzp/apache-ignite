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

package org.apache.ignite.internal.processors.platform.client.cache;

import org.apache.ignite.internal.binary.BinaryRawWriterEx;
import org.apache.ignite.internal.processors.cache.query.QueryCursorEx;
import org.apache.ignite.internal.processors.platform.client.ClientCloseableResource;
import org.apache.ignite.internal.processors.platform.client.ClientResourceRegistry;

import javax.cache.Cache;
import java.util.Iterator;

/**
 * Query cursor holder.
  */
class ClientCacheScanQueryCursor implements ClientCloseableResource {
    /** Cursor. */
    private final QueryCursorEx<Cache.Entry> cursor;

    /** Page size. */
    private final int pageSize;

    /** Resources. */
    private final ClientResourceRegistry resources;

    /** Id. */
    private long id;

    /** Iterator. */
    private Iterator<Cache.Entry> iterator;

    /**
     * Ctor.
     *
     * @param cursor Cursor.
     * @param pageSize Page size.
     */
    ClientCacheScanQueryCursor(QueryCursorEx<Cache.Entry> cursor, int pageSize, ClientResourceRegistry resources) {
        assert cursor != null;
        assert pageSize > 0;
        assert resources != null;

        this.cursor = cursor;
        this.pageSize = pageSize;
        this.resources = resources;
    }

    /**
     * Writes next page to the writer.
     *
     * @param writer Writer.
     */
    void writePage(BinaryRawWriterEx writer) {
        Iterator<Cache.Entry> iter = iterator();

        int cntPos = writer.reserveInt();
        int cnt = 0;

        while (cnt < pageSize && iter.hasNext()) {
            Cache.Entry e = iter.next();

            writer.writeObjectDetached(e.getKey());
            writer.writeObjectDetached(e.getValue());

            cnt++;
        }

        writer.writeInt(cntPos, cnt);

        writer.writeBoolean(iter.hasNext());

        if (!iter.hasNext())
            resources.release(id);
    }

    /**
     * Closes the cursor.
     */
    @Override public void close() {
        cursor.close();
    }

    /**
     * Sets the cursor id.
     *
     * @param id Id.
     */
    public void id(long id) {
        this.id = id;
    }

    /**
     * Gets the cursor id.
     *
     * @return Id.
     */
    public long id() {
        return id;
    }

    /**
     * Gets the iterator.
     *
     * @return Iterator.
     */
    private Iterator<Cache.Entry> iterator() {
        if (iterator == null)
            iterator = cursor.iterator();

        return iterator;
    }
}
