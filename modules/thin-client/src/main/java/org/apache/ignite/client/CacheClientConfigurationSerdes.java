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

package org.apache.ignite.client;

import org.apache.ignite.binary.*;
import org.apache.ignite.cache.*;
import org.apache.ignite.internal.binary.*;
import org.apache.ignite.internal.binary.streams.*;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

/** Serialize/deserialize {@link CacheClientConfiguration}. */
class CacheClientConfigurationSerdes {
    /** Marshaller. */
    private final GridBinaryMarshaller marsh;

    /** Collection serializer/deserializer. */
    private final CollectionSerdes colSerdes = new CollectionSerdes();

    /** Constructor. */
    CacheClientConfigurationSerdes(GridBinaryMarshaller marsh) {
        this.marsh = marsh;
    }

    /** Serialize configuration to stream. */
    void write(CacheClientConfiguration cfg, BinaryOutputStream out) {
        BinaryRawWriterEx writer = marsh.writer(out);

        int origPos = out.position();

        writer.writeInt(0); // configuration length is to be assigned in the end
        writer.writeShort((short)0); // properties count is to be assigned in the end

        AtomicInteger propCnt = new AtomicInteger(0);

        BiConsumer<ClientConfigurationItem, Consumer<BinaryRawWriter>> itemWriter = (cfgItem, cfgWriter) -> {
            writer.writeShort(cfgItem.code());

            cfgWriter.accept(writer);

            propCnt.incrementAndGet();
        };

        itemWriter.accept(ClientConfigurationItem.NAME, w -> w.writeString(cfg.getName()));
        itemWriter.accept(ClientConfigurationItem.CACHE_MODE, w -> w.writeInt(cfg.getCacheMode().ordinal()));
        itemWriter.accept(ClientConfigurationItem.ATOMICITY_MODE, w -> w.writeInt(cfg.getAtomicityMode().ordinal()));
        itemWriter.accept(ClientConfigurationItem.BACKUPS, w -> w.writeInt(cfg.getBackups()));
        itemWriter.accept(ClientConfigurationItem.WRITE_SYNCHRONIZATION_MODE, w -> w.writeInt(cfg.getWriteSynchronizationMode().ordinal()));
        itemWriter.accept(ClientConfigurationItem.READ_FROM_BACKUP, w -> w.writeBoolean(cfg.isReadFromBackup()));
        itemWriter.accept(ClientConfigurationItem.EAGER_TTL, w -> w.writeBoolean(cfg.isEagerTtl()));
        itemWriter.accept(ClientConfigurationItem.GROUP_NAME, w -> w.writeString(cfg.getGroupName()));
        itemWriter.accept(ClientConfigurationItem.DEFAULT_LOCK_TIMEOUT, w -> w.writeLong(cfg.getDefaultLockTimeout()));
        itemWriter.accept(ClientConfigurationItem.PARTITION_LOSS_POLICY, w -> w.writeInt(cfg.getPartitionLossPolicy().ordinal()));
        itemWriter.accept(ClientConfigurationItem.REBALANCE_BATCH_SIZE, w -> w.writeInt(cfg.getRebalanceBatchSize()));
        itemWriter.accept(ClientConfigurationItem.REBALANCE_BATCHES_PREFETCH_COUNT, w -> w.writeLong(cfg.getRebalanceBatchesPrefetchCount()));
        itemWriter.accept(ClientConfigurationItem.REBALANCE_DELAY, w -> w.writeLong(cfg.getRebalanceDelay()));
        itemWriter.accept(ClientConfigurationItem.REBALANCE_MODE, w -> w.writeInt(cfg.getRebalanceMode().ordinal()));
        itemWriter.accept(ClientConfigurationItem.REBALANCE_ORDER, w -> w.writeInt(cfg.getRebalanceOrder()));
        itemWriter.accept(ClientConfigurationItem.REBALANCE_THROTTLE, w -> w.writeLong(cfg.getRebalanceThrottle()));
        itemWriter.accept(ClientConfigurationItem.REBALANCE_TIMEOUT, w -> w.writeLong(cfg.getRebalanceTimeout()));

        // TODO: copyOnRead, dataRegionName, enableStatistics, lockTimeout, maxConcurrentAsyncOperations
        // TODO: maxQueryIteratorsCount, onheapCacheEnabled, queryDetailMetricsSize, queryParallelism, sqlEscapeAll
        // TODO: sqlIndexMaxInlineSize, sqlSchema, keyConfiguration, queryEntities

        writer.writeInt(origPos, out.position() - origPos - 4); // configuration length
        writer.writeInt(origPos + 4, propCnt.get()); // properties count
    }

    /** Deserialize configuration from stream. */
    CacheClientConfiguration read(BinaryInputStream in) {
        CacheClientConfiguration cfg = new CacheClientConfiguration("TBD"); // cache name is to be assigned later

        BinaryRawReader reader = new BinaryReaderExImpl(null, in, null, true);

        reader.readInt(); // Do not need length to read data. The protocol defines fixed configuration layout.

        cfg.setAtomicityMode(CacheAtomicityMode.fromOrdinal(reader.readInt()));
        cfg.setBackups(reader.readInt());
        cfg.setCacheMode(CacheMode.fromOrdinal(reader.readInt()));
        reader.readBoolean(); // TODO: copyOnRead
        reader.readString(); // TODO: dataRegionName
        cfg.setEagerTtl(reader.readBoolean());
        reader.readBoolean(); // TODO: enableStatistics
        cfg.setGroupName(reader.readString());
        cfg.setDefaultLockTimeout(reader.readLong());
        reader.readInt(); // TODO: maxConcurrentAsyncOperations
        reader.readInt(); // TODO: maxQueryIteratorsCount
        cfg.setName(reader.readString());
        reader.readBoolean(); // TODO: onheapCacheEnabled
        cfg.setPartitionLossPolicy(PartitionLossPolicy.fromOrdinal((byte)reader.readInt()));
        reader.readInt(); // TODO: queryDetailMetricsSize
        reader.readInt(); // TODO: queryParallelism
        cfg.setReadFromBackup(reader.readBoolean());
        cfg.setRebalanceBatchSize(reader.readInt());
        cfg.setRebalanceBatchesPrefetchCount(reader.readLong());
        cfg.setRebalanceDelay(reader.readLong());
        cfg.setRebalanceMode(CacheRebalanceMode.fromOrdinal(reader.readInt()));
        cfg.setRebalanceOrder(reader.readInt());
        cfg.setRebalanceThrottle(reader.readLong());
        cfg.setRebalanceTimeout(reader.readLong());
        reader.readBoolean(); // TODO: sqlEscapeAll
        reader.readInt(); // TODO: sqlIndexMaxInlineSize
        reader.readString(); // TODO: sqlSchema
        cfg.setWriteSynchronizationMode(CacheWriteSynchronizationMode.fromOrdinal(reader.readInt()));
        colSerdes.read(in, i -> {
            i
        }); // TODO: keyConfiguration
        reader.readCollection(); // TODO: queryEntities

        return cfg;
    }

    /** Thin client protocol cache configuration item codes. */
    private enum ClientConfigurationItem {
        /** Name. */NAME(0),
        /** Cache mode. */CACHE_MODE(1),
        /** Atomicity mode. */ATOMICITY_MODE(2),
        /** Backups. */BACKUPS(3),
        /** Write synchronization mode. */WRITE_SYNCHRONIZATION_MODE(4),
        /** Read from backup. */READ_FROM_BACKUP(6),
        /** Eager ttl. */EAGER_TTL(405),
        /** Group name. */GROUP_NAME(400),
        /** Default lock timeout. */DEFAULT_LOCK_TIMEOUT(402),
        /** Partition loss policy. */PARTITION_LOSS_POLICY(404),
        /** Rebalance batch size. */REBALANCE_BATCH_SIZE(303),
        /** Rebalance batches prefetch count. */REBALANCE_BATCHES_PREFETCH_COUNT(304),
        /** Rebalance delay. */REBALANCE_DELAY(301),
        /** Rebalance mode. */REBALANCE_MODE(300),
        /** Rebalance order. */REBALANCE_ORDER(305),
        /** Rebalance throttle. */REBALANCE_THROTTLE(306),
        /** Rebalance timeout. */REBALANCE_TIMEOUT(302);

        /** Code. */
        private final short code;

        /** */
        ClientConfigurationItem(int code) {
            this.code = (short)code;
        }

        /** @return Code. */
        short code() {
            return code;
        }
    }

    /** Serialize/deserialize {@link Collection}. */
    private static class CollectionSerdes {
        /**
         * @param col Collection to serialize.
         * @param writer Stream writer.
         * @param elemWriter Collection element serializer
         */
        <E> void write(Collection<E> col, BinaryRawWriter writer, BiConsumer<BinaryRawWriter, E> elemWriter) {
            if (col == null || col.size() == 0)
                writer.writeInt(0);
            else {
                writer.writeInt(col.size());

                for (E e : col)
                    elemWriter.accept(writer, e);
            }
        }

        /**
         * @param reader Stream reader.
         * @param elemReader Collection element deserializer.
         * @return Deserialized collection.
         */
        <E> Collection<E> read(BinaryRawReader reader, Function<BinaryRawReader, E> elemReader) {
            Collection<E> col = new ArrayList<>();

            int cnt = reader.readInt();

            for (int i = 0; i < cnt; i++)
                col.add(elemReader.apply(reader));

            return col;
        }
    }
}
