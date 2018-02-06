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

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.apache.ignite.binary.BinaryRawWriter;
import org.apache.ignite.internal.binary.BinaryRawWriterEx;
import org.apache.ignite.internal.binary.BinaryUtils;
import org.apache.ignite.internal.binary.GridBinaryMarshaller;
import org.apache.ignite.internal.binary.streams.BinaryOutputStream;
import org.apache.ignite.internal.processors.platform.utils.PlatformUtils;

/**
 * Implementation of {@link IgniteClient} over TCP protocol.
 */
class TcpIgniteClient implements IgniteClient, AutoCloseable {
    /** Channel. */
    private final ClientChannel ch;

    /** Ignite Binary Object serializer/deserializer. */
    private final GridBinaryMarshaller marsh = PlatformUtils.marshaller();

    /**
     * Private constructor. Use {@link IgniteClient#start(IgniteClientConfiguration)} to create an instance of
     * {@link TcpClientChannel}.
     */
    private TcpIgniteClient(IgniteClientConfiguration cfg) throws IgniteClientException {
        this.ch = new TcpClientChannel(cfg);
    }

    /** {@inheritDoc} */
    @Override public void close() throws Exception {
        ch.close();
    }

    /** {@inheritDoc} */
    @Override public <K, V> CacheClient<K, V> getOrCreateCache(String name) throws IgniteClientException {
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("Cache name must be specified");

        createCacheIfNotExists(name);

        return new TcpCacheClient<>(name, ch);
    }

    /** {@inheritDoc} */
    @Override public <K, V> CacheClient<K, V> getOrCreateCache(
        CacheClientConfiguration cfg) throws IgniteClientException {
        if (cfg == null)
            throw new IllegalArgumentException("Cache configuration must be specified");

        if (cfg.getName() == null || cfg.getName().length() == 0)
            throw new IllegalArgumentException("Cache name must be specified");

        createCacheIfNotExists(cfg);

        return new TcpCacheClient<>(cfg.getName(), ch);
    }

    /** {@inheritDoc} */
    @Override public <K, V> CacheClient<K, V> cache(String name) {
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("Cache name must be specified");

        return new TcpCacheClient<>(name, ch);
    }

    /** {@inheritDoc} */
    @Override public Collection<String> cacheNames() throws IgniteClientException {
        final ClientOperation OP = ClientOperation.CACHE_GET_NAMES;

        long id = ch.send(OP, null);

        return Arrays.asList(BinaryUtils.doReadStringArray(ch.receive(OP, id)));
    }

    /**
     * Open thin client connection to the Ignite cluster.
     *
     * @param cfg Thin client configuration.
     * @return Successfully opened thin client connection.
     */
    static IgniteClient start(IgniteClientConfiguration cfg) throws IgniteClientException {
        return new TcpIgniteClient(cfg);
    }

    /** */
    private void createCacheIfNotExists(String name) throws IgniteClientException {
        final ClientOperation OP = ClientOperation.CACHE_GET_OR_CREATE_WITH_NAME;

        long id = ch.send(OP, req -> req.writeByteArray(marsh.marshal(name)));

        ch.receive(OP, id); // ignore empty response
    }

    /** */
    private void createCacheIfNotExists(CacheClientConfiguration cfg) throws IgniteClientException {
        final ClientOperation OP = ClientOperation.CACHE_CREATE_WITH_CONFIGURATION;

        long id = ch.send(OP, req -> writeClientConfiguration(req, cfg));

        ch.receive(OP, id); // ignore empty response
    }

    /** */
    private void writeClientConfiguration(BinaryOutputStream out, CacheClientConfiguration cfg) {
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

        writer.writeInt(origPos, out.position() - origPos - 4); // configuration length
        writer.writeInt(origPos + 4, propCnt.get()); // properties count
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
}
