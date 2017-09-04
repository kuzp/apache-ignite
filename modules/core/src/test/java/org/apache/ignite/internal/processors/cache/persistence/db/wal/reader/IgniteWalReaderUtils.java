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

package org.apache.ignite.internal.processors.cache.persistence.db.wal.reader;

import java.io.File;
import org.apache.ignite.internal.pagemem.wal.WALIterator;
import org.apache.ignite.internal.pagemem.wal.WALPointer;
import org.apache.ignite.internal.pagemem.wal.record.WALRecord;
import org.apache.ignite.internal.processors.cache.persistence.file.RandomAccessFileIOFactory;
import org.apache.ignite.internal.processors.cache.persistence.wal.FileWriteAheadLogManager;
import org.apache.ignite.internal.processors.cache.persistence.wal.reader.IgniteWalIteratorFactory;
import org.apache.ignite.lang.IgniteBiTuple;
import org.apache.ignite.logger.NullLogger;

public class IgniteWalReaderUtils {
    public static void main(String[] args) throws Exception {
        final File walArchiveDirWithConsistentId = new File("/tmp/database_IgniteDbSnapshotSelfMultiNodeTest1/");
//        final File walArchiveDirWithConsistentId = new File("/ssd/dev/incubator-ignite/work/db/wal/archive/database_IgniteDbSnapshotSelfMultiNodeTest1");
        final File walWorkDirWithConsistentId = new File("/ssd/dev/incubator-ignite/work/db/wal/archive/database_IgniteDbSnapshotSelfMultiNodeTest1");
//        final File walWorkDirWithConsistentId = new File("/ssd/dev/incubator-ignite/work/db/wal/database_IgniteDbSnapshotSelfMultiNodeTest1");

        final IgniteWalIteratorFactory factory = new IgniteWalIteratorFactory(new NullLogger(),
            new RandomAccessFileIOFactory(), 1024);

        final File[] workFiles = walWorkDirWithConsistentId.listFiles(FileWriteAheadLogManager.WAL_SEGMENT_FILE_FILTER);

        try (WALIterator stIt = factory.iteratorWorkFiles(workFiles)) {
            while (stIt.hasNextX()) {
                IgniteBiTuple<WALPointer, WALRecord> next = stIt.nextX();

                System.out.println("[W] " + next.get2());
            }
        }

        try (WALIterator stIt = factory.iteratorArchiveDirectory(walArchiveDirWithConsistentId)) {
            while (stIt.hasNextX()) {
                IgniteBiTuple<WALPointer, WALRecord> next = stIt.nextX();

                System.out.println("[A] " + next.get2());

            }
        }
    }
}
