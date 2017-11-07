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

package org.apache.ignite.internal.managers.communication;

import java.io.Externalizable;
import java.nio.ByteBuffer;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.internal.ExecutorAwareMessage;
import org.apache.ignite.internal.GridDirectTransient;
import org.apache.ignite.internal.GridTopic;
import org.apache.ignite.internal.direct.DataObject;
import org.apache.ignite.internal.direct.MessageHandler;
import org.apache.ignite.internal.direct.MessageHandlerProvider;
import org.apache.ignite.internal.processors.cache.GridCacheMessage;
import org.apache.ignite.internal.util.GridUnsafe;
import org.apache.ignite.internal.util.tostring.GridToStringInclude;
import org.apache.ignite.internal.util.typedef.internal.S;
import org.apache.ignite.plugin.extensions.communication.Message;
import org.apache.ignite.plugin.extensions.communication.MessageReader;
import org.apache.ignite.plugin.extensions.communication.MessageWriter;
import org.jetbrains.annotations.Nullable;
import sun.nio.ch.DirectBuffer;

import static org.apache.ignite.internal.util.GridUnsafe.BYTE_ARR_OFF;

/**
 * Wrapper for all grid messages.
 */
@SuppressWarnings("WeakerAccess")
public class GridIoMessageEx implements Message, DataObject {
    /** */
    public static final short DIRECT_TYPE = 566;
    /** */
    private static final long serialVersionUID = 0L;
    /** */
    private static final byte FLAG_ORDERED = 0b0001;
    /** */
    private static final byte FLAG_SKIP_ON_TIMEOUT = 0b0010;
    /** */
    private static final byte FLAG_TOPIC_ORDINAL = 0b0100;
    /** */
    private static final int HEADER_SIZE = 6;

    /** Message topic. */
    @GridToStringInclude
    @GridDirectTransient
    private Object topic;

    /** Topic ordinal. */
    @GridDirectTransient
    private int topicOrd = -1;

    /** Message. */
    @GridDirectTransient
    private Message msg;

    /** Flags. */
    @GridDirectTransient
    private byte flags = Byte.MIN_VALUE;

    /** Policy. */
    @GridDirectTransient
    private byte plc;

    /** Partition. */
    @GridDirectTransient
    private int partition;

    /** Message timeout. */
    @GridDirectTransient
    private long timeout = Long.MIN_VALUE;

    private int size = -1;
    private int position;
    private int limit = -1;
    private byte[] data;

    /**
     * No-op constructor to support {@link Externalizable} interface. This constructor is not meant to be used for other
     * purposes.
     */
    public GridIoMessageEx() {
        // No-op.
    }

    /**
     * @param plc Policy.
     * @param topic Communication topic.
     * @param msg Message.
     * @param ordered Message ordered flag.
     * @param timeout Timeout.
     * @param skipOnTimeout Whether message can be skipped on timeout.
     */
    public GridIoMessageEx(
        byte plc,
        Object topic,
        int topicOrd,
        Message msg,
        boolean ordered,
        long timeout,
        boolean skipOnTimeout) {
        assert topic != null;
        assert topicOrd <= Byte.MAX_VALUE;
        assert msg != null;

        flags = 0;

        if (ordered)
            flags |= FLAG_ORDERED;

        if (skipOnTimeout)
            flags |= FLAG_SKIP_ON_TIMEOUT;

        if (topicOrd != -1)
            flags |= FLAG_TOPIC_ORDINAL;

        this.plc = plc;
        this.timeout = timeout;
        this.topic = topic;
        this.topicOrd = topicOrd;
        this.msg = msg;

        partition = partition(msg);
    }

    /**
     * @return Policy.
     */
    byte policy() {
        return plc;
    }

    /**
     * @return Topic.
     */
    Object topic(MessageHandlerProvider provider) throws IgniteCheckedException {
        if (topic == null) {
            assert position != 0;
            assert (flags & FLAG_TOPIC_ORDINAL) == 0;

            try (MessageHandler handler = provider.forRead(this)) {
                topic = handler.readObject();
            }
        }

        return topic;
    }

    /**
     * @return Topic.
     */
    Object topic() {
        assert topic != null;

        return topic;
    }

    /**
     * @return Message.
     */
    public Message message() {
        return msg;
    }

    /**
     * @return Message timeout.
     */
    public long timeout() {
        return timeout;
    }

    /**
     * @return Whether message can be skipped on timeout.
     */
    public boolean skipOnTimeout() {
        assert flags != Byte.MIN_VALUE;

        return (flags & FLAG_SKIP_ON_TIMEOUT) != 0;
    }

    /**
     * @return {@code True} if message is ordered, {@code false} otherwise.
     */
    boolean isOrdered() {
        assert flags != Byte.MIN_VALUE;

        return (flags & FLAG_ORDERED) != 0;
    }

    public int partition() {
        return partition;
    }

    @Override
    public byte[] data() {
        return data;
    }

    @Override
    public void data(byte[] data) {
        this.data = data;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void size(int size) {
        this.size = size;
    }

    @Override
    public int limit() {
        return limit;
    }

    @Override
    public void limit(int limit) {
        this.limit = limit;
    }

    @Override
    public int position() {
        return position;
    }

    @Override
    public void position(int position) {
        this.position = position;
    }

    /** {@inheritDoc} */
    @Override public void onAckReceived() {
        msg.onAckReceived();
    }

    public GridIoMessageEx finishMarshalling(MessageHandlerProvider provider) throws IgniteCheckedException {
        if (data == null) {
            try (MessageHandler handler = provider.forWrite(this)) {
                position = 4; // skip size

                assert flags != Byte.MIN_VALUE;

                handler.writeByte(flags);
                handler.writeByte(plc);
                handler.writeInt(partition);
                handler.writeLong(timeout);

                if (topicOrd != -1)
                    handler.writeByte((byte)topicOrd);
                else
                    handler.writeObject(topic);

                handler.writeMessage(msg, handler);
            }

            assert data != null;

            GridUnsafe.putInt(data, GridUnsafe.BYTE_ARR_OFF, position);

            limit = size = position;

            position = 0;
        }

        return this;
    }

    public GridIoMessageEx finishUnmarshalling(MessageHandlerProvider provider) throws IgniteCheckedException {
        if (position != 0) {
            try (MessageHandler handler = provider.forRead(this)) {
                if (topic == null) {
                    assert (flags & FLAG_TOPIC_ORDINAL) != FLAG_TOPIC_ORDINAL;

                    topic = handler.readObject();
                }

                msg = handler.readMessage(handler);
            }

            position = 0;

            data = null;
        }

        return this;
    }

    /** {@inheritDoc} */
    @Override public boolean equals(Object obj) {
        throw new AssertionError();
    }

    /** {@inheritDoc} */
    @Override public int hashCode() {
        throw new AssertionError();
    }

    /** {@inheritDoc} */
    @Override public boolean writeTo(ByteBuffer buf, MessageWriter writer) {
        if (position == 0 && buf.remaining() < HEADER_SIZE)
            return false;

        assert size != -1;      // check if message is fully prepared

        byte[] dst = buf.isDirect() ? null : buf.array();
        long dstOff = buf.isDirect() ? ((DirectBuffer)buf).address() : BYTE_ARR_OFF;

        if (position == 0) {
            GridUnsafe.putShort(dst, dstOff + buf.position(), directType());

            buf.position(buf.position() + 2);
        }

        int len = Math.min(size - position, buf.remaining());

        GridUnsafe.copyMemory(data, BYTE_ARR_OFF + position, dst, dstOff + buf.position(), len);

        buf.position(buf.position() + len);
        position += len;

        if (position == size) {
            position = 0;

            return true;
        }

        return false;
    }

    /** {@inheritDoc} */
    @Override public boolean readFrom(ByteBuffer buf, MessageReader reader) {
        byte[] dst = buf.isDirect() ? null : buf.array();
        long dstOff = buf.isDirect() ? ((DirectBuffer)buf).address() : BYTE_ARR_OFF;

        int size;

        if (position == 0) {
            if (buf.remaining() < 4)
                return false;
            else {
                size = GridUnsafe.getInt(dst, dstOff + buf.position());

                data = new byte[size];

                limit = size;

                GridUnsafe.putInt(data, BYTE_ARR_OFF, size);

                position += 4;

                buf.position(buf.position() + 4);
            }
        }
        else
            size = data.length;

        int len = Math.min(size - position, buf.remaining());

        GridUnsafe.copyMemory(dst, dstOff + buf.position(), data, BYTE_ARR_OFF + position, len);

        buf.position(buf.position() + len);
        position += len;

        if (position != size)
            return false;

        assert position == size;

        this.size = limit = size;

        position = 4;

        flags = MessageHandler.readByte(this);
        plc = MessageHandler.readByte(this);
        partition = MessageHandler.readInt(this);
        timeout = MessageHandler.readLong(this);

        if ((flags & FLAG_TOPIC_ORDINAL) == FLAG_TOPIC_ORDINAL)
            topic = GridTopic.fromOrdinal(topicOrd = MessageHandler.readByte(this));

        return true;
    }

    /** {@inheritDoc} */
    @Override public short directType() {
        return DIRECT_TYPE;
    }

    /** {@inheritDoc} */
    @Override public byte fieldsCount() {
        return 1;
    }

    /**
     * Get single partition for this message (if applicable).
     *
     * @return Partition ID.
     */
    private int partition(Message msg) {
        if (msg instanceof GridCacheMessage)
            return ((GridCacheMessage)msg).partition();

        return Integer.MIN_VALUE;
    }

    /**
     * @return Executor name (if available).
     */
    @Nullable public String executorName() {
        if (msg instanceof ExecutorAwareMessage)
            return ((ExecutorAwareMessage)msg).executorName();

        return null;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(GridIoMessageEx.class, this);
    }
}
