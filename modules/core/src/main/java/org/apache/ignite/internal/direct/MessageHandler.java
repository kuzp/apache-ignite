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

package org.apache.ignite.internal.direct;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.UUID;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.IgniteException;
import org.apache.ignite.internal.util.GridUnsafe;
import org.apache.ignite.internal.util.tostring.GridToStringExclude;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.lang.IgniteUuid;
import org.apache.ignite.marshaller.Marshaller;
import org.apache.ignite.plugin.extensions.communication.Message;
import org.apache.ignite.plugin.extensions.communication.MessageCollectionItemType;
import org.apache.ignite.plugin.extensions.communication.MessageFactory;
import org.apache.ignite.plugin.extensions.communication.MessageReader;
import org.apache.ignite.plugin.extensions.communication.MessageWriter;
import org.jetbrains.annotations.Nullable;

import static org.apache.ignite.internal.util.GridUnsafe.BIG_ENDIAN;
import static org.apache.ignite.internal.util.GridUnsafe.BYTE_ARR_OFF;
import static org.apache.ignite.internal.util.GridUnsafe.CHAR_ARR_OFF;
import static org.apache.ignite.internal.util.GridUnsafe.DOUBLE_ARR_OFF;
import static org.apache.ignite.internal.util.GridUnsafe.FLOAT_ARR_OFF;
import static org.apache.ignite.internal.util.GridUnsafe.INT_ARR_OFF;
import static org.apache.ignite.internal.util.GridUnsafe.LONG_ARR_OFF;
import static org.apache.ignite.internal.util.GridUnsafe.SHORT_ARR_OFF;

@SuppressWarnings({"WeakerAccess", "ForLoopReplaceableByForEach"})
public class MessageHandler implements MessageReader, MessageWriter, AutoCloseable {
    /** */
    private static final int HEADER_WRITTEN_FLAG = Integer.MIN_VALUE;

    /** */
    private static final byte[] BYTE_ARR_EMPTY = new byte[0];

    /** */
    private static final short[] SHORT_ARR_EMPTY = new short[0];

    /** */
    private static final int[] INT_ARR_EMPTY = U.EMPTY_INTS;

    /** */
    private static final long[] LONG_ARR_EMPTY = U.EMPTY_LONGS;

    /** */
    private static final float[] FLOAT_ARR_EMPTY = new float[0];

    /** */
    private static final double[] DOUBLE_ARR_EMPTY = new double[0];

    /** */
    private static final char[] CHAR_ARR_EMPTY = new char[0];

    /** */
    private static final boolean[] BOOLEAN_ARR_EMPTY = new boolean[0];

    /** */
    private static final ArrayCreator<byte[]> BYTE_ARR_CREATOR = new ArrayCreator<byte[]>() {
        @Override public byte[] create(int len) {
            if (len < 0)
                throw new IgniteException("Read invalid byte array length: " + len);

            switch (len) {
                case 0:
                    return BYTE_ARR_EMPTY;

                default:
                    return new byte[len];
            }
        }
    };

    /** */
    private static final ArrayCreator<short[]> SHORT_ARR_CREATOR = new ArrayCreator<short[]>() {
        @Override public short[] create(int len) {
            if (len < 0)
                throw new IgniteException("Read invalid short array length: " + len);

            switch (len) {
                case 0:
                    return SHORT_ARR_EMPTY;

                default:
                    return new short[len];
            }
        }
    };

    /** */
    private static final ArrayCreator<int[]> INT_ARR_CREATOR = new ArrayCreator<int[]>() {
        @Override public int[] create(int len) {
            if (len < 0)
                throw new IgniteException("Read invalid int array length: " + len);

            switch (len) {
                case 0:
                    return INT_ARR_EMPTY;

                default:
                    return new int[len];
            }
        }
    };

    /** */
    private static final ArrayCreator<long[]> LONG_ARR_CREATOR = new ArrayCreator<long[]>() {
        @Override public long[] create(int len) {
            if (len < 0)
                throw new IgniteException("Read invalid long array length: " + len);

            switch (len) {
                case 0:
                    return LONG_ARR_EMPTY;

                default:
                    return new long[len];
            }
        }
    };

    /** */
    private static final ArrayCreator<float[]> FLOAT_ARR_CREATOR = new ArrayCreator<float[]>() {
        @Override public float[] create(int len) {
            if (len < 0)
                throw new IgniteException("Read invalid float array length: " + len);

            switch (len) {
                case 0:
                    return FLOAT_ARR_EMPTY;

                default:
                    return new float[len];
            }
        }
    };

    /** */
    private static final ArrayCreator<double[]> DOUBLE_ARR_CREATOR = new ArrayCreator<double[]>() {
        @Override public double[] create(int len) {
            if (len < 0)
                throw new IgniteException("Read invalid double array length: " + len);

            switch (len) {
                case 0:
                    return DOUBLE_ARR_EMPTY;

                default:
                    return new double[len];
            }
        }
    };

    /** */
    private static final ArrayCreator<char[]> CHAR_ARR_CREATOR = new ArrayCreator<char[]>() {
        @Override public char[] create(int len) {
            if (len < 0)
                throw new IgniteException("Read invalid char array length: " + len);

            switch (len) {
                case 0:
                    return CHAR_ARR_EMPTY;

                default:
                    return new char[len];
            }
        }
    };

    /** */
    private static final ArrayCreator<boolean[]> BOOLEAN_ARR_CREATOR = new ArrayCreator<boolean[]>() {
        @Override public boolean[] create(int len) {
            if (len < 0)
                throw new IgniteException("Read invalid boolean array length: " + len);

            switch (len) {
                case 0:
                    return BOOLEAN_ARR_EMPTY;

                default:
                    return new boolean[len];
            }
        }
    };

    /** */
    @GridToStringExclude
    private final MessageFactory msgFactory;

    /** */
    @GridToStringExclude
    private final Marshaller marsh;

    /** */
    @GridToStringExclude
    private final ClassLoader clsLdr;

    /** */
    @GridToStringExclude
    private final InputStream input = new InputStreamEx();

    /** */
    @GridToStringExclude
    private final OutputStream output = new OutputStreamEx();

    /** */
    @GridToStringExclude
    protected byte[] heapArr;

    /** */
    @GridToStringExclude
    protected SoftReference<byte[]> cache;

    /** */
    private int[] state = new int[16];

    /** */
    private int stIdx;

    /** */
    private DataObject dataObject;

    /** */
    private boolean isWrite;

    public MessageHandler(Marshaller marsh, ClassLoader clsLdr, MessageFactory msgFactory) {
        this.marsh = marsh;
        this.clsLdr = clsLdr;
        this.msgFactory = msgFactory;
    }

    public static byte readByte(DataObject dataObject) {
        int pos = dataObject.position();
        byte[] heapArr = dataObject.data();

        assert pos + 1 >= 0 && pos + 1 <= heapArr.length;

        dataObject.position(pos + 1);

        return GridUnsafe.getByte(heapArr, BYTE_ARR_OFF + pos);
    }

    public static long readLong(DataObject dataObject) {
        long val;

        long prim = 0;

        int primShift = 0;

        byte[] heapArr = dataObject.data();

        while (true) {
            int pos = dataObject.position();

            assert pos + 1 >= 0 && pos + 1 <= heapArr.length;

            byte b = GridUnsafe.getByte(heapArr, BYTE_ARR_OFF + pos);

            dataObject.position(pos + 1);

            prim |= ((long)b & 0x7F) << (7 * primShift);

            if ((b & 0x80) == 0) {
                val = prim;

                if (val == Long.MIN_VALUE)
                    val = Long.MAX_VALUE;
                else
                    val--;

                break;
            }
            else
                primShift++;
        }

        return val;
    }

    public static int readInt(DataObject dataObject) {
        int val;

        long prim = 0;

        int primShift = 0;

        byte[] heapArr = dataObject.data();

        while (true) {
            int pos = dataObject.position();

            assert pos + 1 >= 0 && pos + 1 <= heapArr.length;

            byte b = GridUnsafe.getByte(heapArr, BYTE_ARR_OFF + pos);

            dataObject.position(pos + 1);

            prim |= ((long)b & 0x7F) << (7 * primShift);

            if ((b & 0x80) == 0) {

                val = (int)prim;

                if (val == Integer.MIN_VALUE)
                    val = Integer.MAX_VALUE;
                else
                    val--;

                break;
            }
            else
                primShift++;
        }

        return val;
    }

    public MessageHandler forRead(DataObject dataObject) {
        assert stIdx == 0 && state[stIdx] == 0;
        assert heapArr == null && this.dataObject == null;
        assert dataObject != null;

        assert dataObject.data() != null;
        assert dataObject.size() != -1;

        this.dataObject = dataObject;
        heapArr = dataObject.data();
        isWrite = false;

        return this;
    }

    public MessageHandler forWrite(DataObject dataObject) {
        assert stIdx == 0 && state[stIdx] == 0;
        assert heapArr == null && this.dataObject == null;
        assert dataObject != null;

        this.dataObject = dataObject;

        byte[] referent;

        if (cache == null || (referent = cache.get()) == null)
            referent = new byte[4096];

        heapArr = referent;
        isWrite = true;

        return this;
    }

    public <T> T readObject() throws IgniteCheckedException {
        int size = GridUnsafe.getInt(heapArr, BYTE_ARR_OFF + dataObject.position());

        move(4);

        dataObject.limit(dataObject.position() + size);

        T res = U.unmarshal(marsh, input, clsLdr);

        dataObject.limit(dataObject.size());

        return res;
    }

    public void writeObject(Object obj) throws IgniteCheckedException {
        int start = dataObject.position();

        move(4);

        U.marshal(marsh, obj, output);

        int end = dataObject.position();

        int size = end - (start + 4);

        GridUnsafe.putInt(heapArr, BYTE_ARR_OFF + start, size);
    }

    private void move(int size) {
        checkCapacity(size);

        dataObject.position(dataObject.position() + size);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte readByte(String name) {
        return readByte();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short readShort(String name) {
        return readShort();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readInt(String name) {
        return readInt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readInt(String name, int dflt) {
        return readInt(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long readLong(String name) {
        return readLong();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float readFloat(String name) {
        return readFloat();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double readDouble(String name) {
        return readDouble();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public char readChar(String name) {
        return readChar();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean readBoolean(String name) {
        return readBoolean();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public byte[] readByteArray(String name) {
        return readByteArray();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public short[] readShortArray(String name) {
        return readShortArray();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public int[] readIntArray(String name) {
        return readIntArray();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public long[] readLongArray(String name) {
        return readLongArray();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public float[] readFloatArray(String name) {
        return readFloatArray();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public double[] readDoubleArray(String name) {
        return readDoubleArray();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public char[] readCharArray(String name) {
        return readCharArray();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public boolean[] readBooleanArray(String name) {
        return readBooleanArray();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public String readString(String name) {
        return readString();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public BitSet readBitSet(String name) {
        return readBitSet();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public UUID readUuid(String name) {
        return readUuid();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public IgniteUuid readIgniteUuid(String name) {
        return readIgniteUuid();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public <T extends Message> T readMessage(String name) {
        return readMessage(this);
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public <T> T[] readObjectArray(String name, MessageCollectionItemType itemType, Class<T> itemCls) {
        return readObjectArray(itemType, itemCls, this);
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public <C extends Collection<?>> C readCollection(String name, MessageCollectionItemType itemType) {
        return readCollection(itemType, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public <M extends Map<?, ?>> M readMap(String name, MessageCollectionItemType keyType,
        MessageCollectionItemType valType, boolean linked) {
        return readMap(keyType, valType, linked, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCurrentReadClass(Class<? extends Message> msgCls) {
    }

    @Override
    public boolean beforeMessageRead() {
        return true;
    }

    @Override
    public boolean afterMessageRead(Class<? extends Message> msgCls) {
        return true;
    }

    @Override
    public boolean isLastRead() {
        return true;
    }

    /** {@inheritDoc} */
    @Override public void beforeInnerMessageRead() {
        forward();
    }

    protected void forward() {
        if (++stIdx == state.length) {
            int[] state0 = state;

            state = new int[state.length << 1];

            System.arraycopy(state0, 0, state, 0, state.length);
        }
    }

    protected void backward() {
        state[stIdx--] = 0;
    }

    /** {@inheritDoc} */
    @Override public void afterInnerMessageRead(boolean finished) {
        backward();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean writeHeader(short type, byte fieldCnt) {
        writeShort(type);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean writeByte(String name, byte val) {
        writeByte(val);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean writeShort(String name, short val) {
        writeShort(val);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean writeInt(String name, int val) {
        writeInt(val);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean writeLong(String name, long val) {
        writeLong(val);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean writeFloat(String name, float val) {
        writeFloat(val);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean writeDouble(String name, double val) {
        writeDouble(val);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean writeChar(String name, char val) {
        writeChar(val);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean writeBoolean(String name, boolean val) {
        writeBoolean(val);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean writeByteArray(String name, @Nullable byte[] val) {
        writeByteArray(val);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean writeByteArray(String name, byte[] val, long off, int len) {
        writeByteArray(val, off, len);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean writeShortArray(String name, @Nullable short[] val) {
        writeShortArray(val);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean writeIntArray(String name, @Nullable int[] val) {
        writeIntArray(val);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean writeLongArray(String name, @Nullable long[] val) {
        writeLongArray(val);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean writeFloatArray(String name, @Nullable float[] val) {
        writeFloatArray(val);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean writeDoubleArray(String name, @Nullable double[] val) {
        writeDoubleArray(val);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean writeCharArray(String name, @Nullable char[] val) {
        writeCharArray(val);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean writeBooleanArray(String name, @Nullable boolean[] val) {
        writeBooleanArray(val);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean writeString(String name, String val) {
        writeString(val);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean writeBitSet(String name, BitSet val) {
        writeBitSet(val);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean writeUuid(String name, UUID val) {
        writeUuid(val);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean writeIgniteUuid(String name, IgniteUuid val) {
        writeIgniteUuid(val);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean writeMessage(String name, @Nullable Message msg) {
        writeMessage(msg, this);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> boolean writeObjectArray(String name, T[] arr, MessageCollectionItemType itemType) {
        writeObjectArray(arr, itemType, this);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> boolean writeCollection(String name, Collection<T> col, MessageCollectionItemType itemType) {
        writeCollection(col, itemType, this);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> boolean writeMap(String name, Map<K, V> map, MessageCollectionItemType keyType,
        MessageCollectionItemType valType) {
        writeMap(map, keyType, valType, this);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCurrentWriteClass(Class<? extends Message> msgCls) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHeaderWritten() {
        return (state[stIdx] & HEADER_WRITTEN_FLAG) == HEADER_WRITTEN_FLAG;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onHeaderWritten() {
        state[stIdx] |= HEADER_WRITTEN_FLAG;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeInnerMessageWrite() {
        forward();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterInnerMessageWrite(boolean finished) {
        backward();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBuffer(ByteBuffer buf) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int state() {
        return state[stIdx] & (~HEADER_WRITTEN_FLAG);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void incrementState() {
        state[stIdx] = (state[stIdx] & HEADER_WRITTEN_FLAG) | (state() + 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        assert stIdx == 0;

        state[stIdx] = 0;
    }

    /** {@inheritDoc} */
    @Override public void close() {
        if (isWrite) {
            assert dataObject != null && heapArr != null && heapArr.length >= dataObject.position();

            dataObject.data(Arrays.copyOf(heapArr, dataObject.position()));

            if ((cache == null || cache.get() != heapArr))
                cache = new SoftReference<>(heapArr);
        }

        heapArr = null;
        dataObject = null;

        reset();
    }

    private void checkCapacity(int size) {
        int required = dataObject.position() + size;

        assert required >= 0;

        if (required > heapArr.length) {
            assert isWrite;

            cache = null;

            heapArr = Arrays.copyOf(heapArr, U.ceilPow2(required));
        }
    }

    public void writeByte(byte val) {
        checkCapacity(1);

        int pos = dataObject.position();

        GridUnsafe.putByte(heapArr, BYTE_ARR_OFF + pos, val);

        dataObject.position(pos + 1);
    }

    public void writeShort(short val) {
        checkCapacity(2);

        int pos = dataObject.position();

        long off = BYTE_ARR_OFF + pos;

        if (BIG_ENDIAN)
            GridUnsafe.putShortLE(heapArr, off, val);
        else
            GridUnsafe.putShort(heapArr, off, val);

        dataObject.position(pos + 2);

    }

    public void writeInt(int val) {
        checkCapacity(5);

        if (val == Integer.MAX_VALUE)
            val = Integer.MIN_VALUE;
        else
            val++;

        int pos = dataObject.position();

        while ((val & 0xFFFF_FF80) != 0) {
            byte b = (byte)(val | 0x80);

            GridUnsafe.putByte(heapArr, BYTE_ARR_OFF + pos++, b);

            val >>>= 7;
        }

        GridUnsafe.putByte(heapArr, BYTE_ARR_OFF + pos++, (byte)val);

        dataObject.position(pos);

    }

    public void writeLong(long val) {
        checkCapacity(10);

        if (val == Long.MAX_VALUE)
            val = Long.MIN_VALUE;
        else
            val++;

        int pos = dataObject.position();

        while ((val & 0xFFFF_FFFF_FFFF_FF80L) != 0) {
            byte b = (byte)(val | 0x80);

            GridUnsafe.putByte(heapArr, BYTE_ARR_OFF + pos++, b);

            val >>>= 7;
        }

        GridUnsafe.putByte(heapArr, BYTE_ARR_OFF + pos++, (byte)val);

        dataObject.position(pos);

    }

    /** {@inheritDoc} */
    public void writeFloat(float val) {
        checkCapacity(4);

        int pos = dataObject.position();

        long off = BYTE_ARR_OFF + pos;

        if (BIG_ENDIAN)
            GridUnsafe.putFloatLE(heapArr, off, val);
        else
            GridUnsafe.putFloat(heapArr, off, val);

        dataObject.position(pos + 4);

    }

    /** {@inheritDoc} */
    public void writeDouble(double val) {
        checkCapacity(8);

        int pos = dataObject.position();

        long off = BYTE_ARR_OFF + pos;

        if (BIG_ENDIAN)
            GridUnsafe.putDoubleLE(heapArr, off, val);
        else
            GridUnsafe.putDouble(heapArr, off, val);

        dataObject.position(pos + 8);

    }

    /** {@inheritDoc} */
    public void writeChar(char val) {
        checkCapacity(2);

        int pos = dataObject.position();

        long off = BYTE_ARR_OFF + pos;

        if (BIG_ENDIAN)
            GridUnsafe.putCharLE(heapArr, off, val);
        else
            GridUnsafe.putChar(heapArr, off, val);

        dataObject.position(pos + 2);

    }

    /** {@inheritDoc} */
    public void writeBoolean(boolean val) {
        checkCapacity(1);

        int pos = dataObject.position();

        GridUnsafe.putBoolean(heapArr, BYTE_ARR_OFF + pos, val);

        dataObject.position(pos + 1);

    }

    /** {@inheritDoc} */
    public void writeByteArray(byte[] val) {
        if (val != null)
            writeArray(val, BYTE_ARR_OFF, val.length, val.length);
        else
            writeInt(-1);
    }

    /** {@inheritDoc} */
    public void writeByteArray(byte[] val, long off, int len) {
        if (val != null)
            writeArray(val, BYTE_ARR_OFF + off, len, len);
        else
            writeInt(-1);
    }

    /** {@inheritDoc} */
    public void writeShortArray(short[] val) {
        if (val != null)
            if (BIG_ENDIAN)
                writeArrayLE(val, SHORT_ARR_OFF, val.length, 2, 1);
            else
                writeArray(val, SHORT_ARR_OFF, val.length, val.length << 1);
        else
            writeInt(-1);
    }

    /** {@inheritDoc} */
    public void writeIntArray(int[] val) {
        if (val != null)
            if (BIG_ENDIAN)
                writeArrayLE(val, INT_ARR_OFF, val.length, 4, 2);
            else
                writeArray(val, INT_ARR_OFF, val.length, val.length << 2);
        else
            writeInt(-1);
    }

    /** {@inheritDoc} */
    public void writeLongArray(long[] val) {
        if (val != null)
            if (BIG_ENDIAN)
                writeArrayLE(val, LONG_ARR_OFF, val.length, 8, 3);
            else
                writeArray(val, LONG_ARR_OFF, val.length, val.length << 3);
        else
            writeInt(-1);
    }

    /** {@inheritDoc} */
    public void writeFloatArray(float[] val) {
        if (val != null)
            if (BIG_ENDIAN)
                writeArrayLE(val, FLOAT_ARR_OFF, val.length, 4, 2);
            else
                writeArray(val, FLOAT_ARR_OFF, val.length, val.length << 2);
        else
            writeInt(-1);
    }

    /** {@inheritDoc} */
    public void writeDoubleArray(double[] val) {
        if (val != null)
            if (BIG_ENDIAN)
                writeArrayLE(val, DOUBLE_ARR_OFF, val.length, 8, 3);
            else
                writeArray(val, DOUBLE_ARR_OFF, val.length, val.length << 3);
        else
            writeInt(-1);
    }

    /** {@inheritDoc} */
    public void writeCharArray(char[] val) {
        if (val != null) {
            if (BIG_ENDIAN)
                writeArrayLE(val, CHAR_ARR_OFF, val.length, 2, 1);
            else
                writeArray(val, CHAR_ARR_OFF, val.length, val.length << 1);
        }
        else
            writeInt(-1);
    }

    /** {@inheritDoc} */
    public void writeBooleanArray(boolean[] val) {
        if (val != null)
            writeArray(val, GridUnsafe.BOOLEAN_ARR_OFF, val.length, val.length);
        else
            writeInt(-1);
    }

    /** {@inheritDoc} */
    public void writeString(String val) {
        writeByteArray(val != null ? val.getBytes() : null);
    }

    /** {@inheritDoc} */
    public void writeBitSet(BitSet val) {
        writeLongArray(val != null ? val.toLongArray() : null);
    }

    /** {@inheritDoc} */
    public void writeUuid(UUID val) {

        writeBoolean(val == null);

        if (val != null) {
            writeLong(val.getMostSignificantBits());

            writeLong(val.getLeastSignificantBits());
        }

    }

    /** {@inheritDoc} */
    public void writeIgniteUuid(IgniteUuid val) {

        writeBoolean(val == null);

        if (val != null) {
            writeLong(val.globalId().getMostSignificantBits());

            writeLong(val.globalId().getLeastSignificantBits());

            writeLong(val.localId());
        }

    }

    /** {@inheritDoc} */
    public void writeMessage(Message msg, MessageWriter writer) {
        if (msg != null) {

            try {
                writer.beforeInnerMessageWrite();

                writer.setCurrentWriteClass(msg.getClass());

                msg.writeTo(null, writer);
            }
            finally {
                writer.afterInnerMessageWrite(true);
            }

        }
        else
            writeShort(Short.MIN_VALUE);
    }

    /** {@inheritDoc} */
    public <T> void writeObjectArray(T[] arr, MessageCollectionItemType itemType,
        MessageWriter writer) {
        if (arr != null) {

            writeInt(arr.length);

            for (int i = 0; i < arr.length; i++)
                write(itemType, arr[i], writer);
        }
        else
            writeInt(-1);
    }

    /** {@inheritDoc} */
    public <T> void writeCollection(Collection<T> col, MessageCollectionItemType itemType,
        MessageWriter writer) {
        if (col != null) {
            if (col instanceof List && col instanceof RandomAccess)
                writeRandomAccessList((List<T>)col, itemType, writer);
            else {
                writeInt(col.size());

                for (Object o : col)
                    write(itemType, o, writer);
            }
        }
        else
            writeInt(-1);
    }

    /**
     * @param list List.
     * @param itemType Component type.
     * @param writer Writer.
     */
    private <T> void writeRandomAccessList(List<T> list, MessageCollectionItemType itemType, MessageWriter writer) {
        assert list instanceof RandomAccess;

        int size = list.size();

        writeInt(size);

        for (int i = 0; i < size; i++)
            write(itemType, list.get(i), writer);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public <K, V> void writeMap(Map<K, V> map, MessageCollectionItemType keyType,
        MessageCollectionItemType valType, MessageWriter writer) {
        if (map != null) {
            writeInt(map.size());

            for (Map.Entry<K, V> e : map.entrySet()) {
                write(keyType, e.getKey(), writer);
                write(valType, e.getValue(), writer);
            }
        }
        else
            writeInt(-1);
    }

    /** {@inheritDoc} */
    public byte readByte() {
        return readByte(dataObject);
    }

    /** {@inheritDoc} */
    public short readShort() {
        checkCapacity(2);

        int pos = dataObject.position();

        dataObject.position(pos + 2);

        long off = BYTE_ARR_OFF + pos;

        return BIG_ENDIAN ? GridUnsafe.getShortLE(heapArr, off) : GridUnsafe.getShort(heapArr, off);
    }

    /** {@inheritDoc} */
    public int readInt() {
        return readInt(dataObject);
    }

    /** {@inheritDoc} */
    public long readLong() {
        return readLong(dataObject);
    }

    /** {@inheritDoc} */
    public float readFloat() {
        checkCapacity(4);

        int pos = dataObject.position();

        dataObject.position(pos + 4);

        long off = BYTE_ARR_OFF + pos;

        return BIG_ENDIAN ? GridUnsafe.getFloatLE(heapArr, off) : GridUnsafe.getFloat(heapArr, off);
    }

    /** {@inheritDoc} */
    public double readDouble() {
        checkCapacity(8);

        int pos = dataObject.position();

        dataObject.position(pos + 8);

        long off = BYTE_ARR_OFF + pos;

        return BIG_ENDIAN ? GridUnsafe.getDoubleLE(heapArr, off) : GridUnsafe.getDouble(heapArr, off);
    }

    /** {@inheritDoc} */
    public char readChar() {
        checkCapacity(2);

        int pos = dataObject.position();

        dataObject.position(pos + 2);

        long off = BYTE_ARR_OFF + pos;

        return BIG_ENDIAN ? GridUnsafe.getCharLE(heapArr, off) : GridUnsafe.getChar(heapArr, off);
    }

    /** {@inheritDoc} */
    public boolean readBoolean() {

        checkCapacity(1);

        int pos = dataObject.position();

        dataObject.position(pos + 1);

        return GridUnsafe.getBoolean(heapArr, BYTE_ARR_OFF + pos);
    }

    /** {@inheritDoc} */
    public byte[] readByteArray() {
        return readArray(BYTE_ARR_CREATOR, 0, BYTE_ARR_OFF);
    }

    /** {@inheritDoc} */
    public short[] readShortArray() {
        return BIG_ENDIAN ? readArrayLE(SHORT_ARR_CREATOR, 2, 1, SHORT_ARR_OFF) :
            readArray(SHORT_ARR_CREATOR, 1, SHORT_ARR_OFF);
    }

    /** {@inheritDoc} */
    public int[] readIntArray() {
        return BIG_ENDIAN ? readArrayLE(INT_ARR_CREATOR, 4, 2, INT_ARR_OFF) :
            readArray(INT_ARR_CREATOR, 2, INT_ARR_OFF);
    }

    /** {@inheritDoc} */
    public long[] readLongArray() {
        return BIG_ENDIAN ? readArrayLE(LONG_ARR_CREATOR, 8, 3, LONG_ARR_OFF) :
            readArray(LONG_ARR_CREATOR, 3, LONG_ARR_OFF);
    }

    /** {@inheritDoc} */
    public float[] readFloatArray() {
        return BIG_ENDIAN ? readArrayLE(FLOAT_ARR_CREATOR, 4, 2, FLOAT_ARR_OFF) :
            readArray(FLOAT_ARR_CREATOR, 2, FLOAT_ARR_OFF);
    }

    /** {@inheritDoc} */
    public double[] readDoubleArray() {
        return BIG_ENDIAN ? readArrayLE(DOUBLE_ARR_CREATOR, 8, 3, DOUBLE_ARR_OFF) :
            readArray(DOUBLE_ARR_CREATOR, 3, DOUBLE_ARR_OFF);
    }

    /** {@inheritDoc} */
    public char[] readCharArray() {
        return BIG_ENDIAN ? readArrayLE(CHAR_ARR_CREATOR, 2, 1, CHAR_ARR_OFF) :
            readArray(CHAR_ARR_CREATOR, 1, CHAR_ARR_OFF);
    }

    /** {@inheritDoc} */
    public boolean[] readBooleanArray() {
        return readArray(BOOLEAN_ARR_CREATOR, 0, GridUnsafe.BOOLEAN_ARR_OFF);
    }

    /** {@inheritDoc} */
    public String readString() {
        byte[] arr = readByteArray();

        return arr != null ? new String(arr) : null;
    }

    /** {@inheritDoc} */
    public BitSet readBitSet() {
        long[] arr = readLongArray();

        return arr != null ? BitSet.valueOf(arr) : null;
    }

    /** {@inheritDoc} */
    public UUID readUuid() {
        if (readBoolean())
            return null;

        long uuidMost = readLong();

        long uuidLeast = readLong();

        return new UUID(uuidMost, uuidLeast);
    }

    /** {@inheritDoc} */
    public IgniteUuid readIgniteUuid() {
        if (readBoolean())
            return null;

        long uuidMost = readLong();
        long uuidLeast = readLong();
        long uuidLocId = readLong();

        return new IgniteUuid(new UUID(uuidMost, uuidLeast), uuidLocId);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public <T extends Message> T readMessage(MessageReader reader) {
        checkCapacity(Message.DIRECT_TYPE_SIZE);

        short type = readShort();

        Message msg = type == Short.MIN_VALUE ? null : msgFactory.create(type);

        if (msg != null) {
            try {
                reader.beforeInnerMessageRead();

                reader.setCurrentReadClass(msg.getClass());

                msg.readFrom(null, reader);
            }
            finally {
                reader.afterInnerMessageRead(true);
            }
        }

        return (T)msg;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public <T> T[] readObjectArray(MessageCollectionItemType itemType, Class<T> itemCls,
        MessageReader reader) {
        Object[] objArr = null;

        int size = readInt();

        if (size >= 0) {
            objArr = itemCls != null ? (Object[])Array.newInstance(itemCls, size) : new Object[size];

            for (int i = 0; i < size; i++) {
                Object item = read(itemType, reader);

                objArr[i] = item;
            }
        }

        return (T[])objArr;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public <C extends Collection<?>> C readCollection(MessageCollectionItemType itemType,
        MessageReader reader) {

        Collection col = null;

        int size = readInt();

        if (size >= 0) {
            col = new ArrayList<>(size);

            for (int i = 0; i < size; i++)
                col.add(read(itemType, reader));
        }

        return (C)col;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public <M extends Map<?, ?>> M readMap(MessageCollectionItemType keyType,
        MessageCollectionItemType valType, boolean linked, MessageReader reader) {

        Map map = null;

        int size = readInt();

        if (size >= 0) {
            map = linked ? U.newLinkedHashMap(size) : U.newHashMap(size);

            for (int i = 0; i < size; i++)
                map.put(read(keyType, reader), read(valType, reader));
        }

        return (M)map;
    }

    /**
     * @param arr Array.
     * @param off Offset.
     * @param len Length.
     * @param bytes Length in bytes.
     */
    private void writeArray(Object arr, long off, int len, int bytes) {
        assert arr != null;
        assert arr.getClass().isArray() && arr.getClass().getComponentType().isPrimitive();
        assert off > 0;
        assert len >= 0;
        assert bytes >= 0;

        writeInt(len);

        int pos = dataObject.position();

        checkCapacity(bytes);

        if (bytes > 0) {
            GridUnsafe.copyMemory(arr, off, heapArr, BYTE_ARR_OFF + pos, bytes);

            dataObject.position(pos + bytes);
        }
    }

    /**
     * @param arr Array.
     * @param off Offset.
     * @param len Length.
     * @param typeSize Primitive type size in bytes. Needs for byte reverse.
     * @param shiftCnt Shift for length.
     */
    private void writeArrayLE(Object arr, long off, int len, int typeSize, int shiftCnt) {
        assert arr != null;
        assert arr.getClass().isArray() && arr.getClass().getComponentType().isPrimitive();
        assert off > 0;
        assert len >= 0;

        int toWrite = len << shiftCnt;

        assert toWrite >= 0;

        writeInt(len);

        checkCapacity(toWrite);

        int pos = dataObject.position();

        for (int i = 0; i < len; i++) {
            for (int j = 0; j < typeSize; j++)
                GridUnsafe.putByte(heapArr, BYTE_ARR_OFF + pos++, GridUnsafe.getByteField(arr, off + (typeSize - j - 1)));
        }

        dataObject.position(dataObject.position() + toWrite);
    }

    /**
     * @param creator Array creator.
     * @param lenShift Array length shift size.
     * @param off Base offset.
     * @return Array or special value if it was not fully read.
     */
    @SuppressWarnings("unchecked")
    private <T> T readArray(ArrayCreator<T> creator, int lenShift, long off) {
        assert creator != null;

        int len = readInt();

        if (len == -1)
            return null;
        else if (len == 0)
            return creator.create(0);

        Object arr = creator.create(len);
        int toRead = len << lenShift;

        int pos = dataObject.position();

        checkCapacity(toRead);

        GridUnsafe.copyMemory(heapArr, BYTE_ARR_OFF + pos, arr, off, toRead);

        dataObject.position(pos + toRead);

        return (T)arr;
    }

    /**
     * @param creator Array creator.
     * @param typeSize Primitive type size in bytes.
     * @param lenShift Array length shift size.
     * @param off Base offset.
     * @return Array or special value if it was not fully read.
     */
    @SuppressWarnings("unchecked")
    private <T> T readArrayLE(ArrayCreator<T> creator, int typeSize, int lenShift, long off) {
        assert creator != null;

        int len = readInt();

        if (len == -1)
            return null;
        else if (len == 0)
            return creator.create(0);

        Object arr = creator.create(len);
        int toRead = len << lenShift;

        checkCapacity(toRead);

        int pos = dataObject.position();

        for (int i = 0; i < len; i++) {
            for (int j = 0; j < typeSize; j++)
                GridUnsafe.putByteField(arr, off++, GridUnsafe.getByte(heapArr, BYTE_ARR_OFF + pos + (typeSize - j - 1)));

            pos += typeSize;
        }

        dataObject.position(dataObject.position() + toRead);

        return (T)arr;
    }

    /**
     * @param type Type.
     * @param val Value.
     * @param writer Writer.
     */
    private void write(MessageCollectionItemType type, Object val, MessageWriter writer) {
        switch (type) {
            case BYTE:
                writeByte((Byte)val);

                break;

            case SHORT:
                writeShort((Short)val);

                break;

            case INT:
                writeInt((Integer)val);

                break;

            case LONG:
                writeLong((Long)val);

                break;

            case FLOAT:
                writeFloat((Float)val);

                break;

            case DOUBLE:
                writeDouble((Double)val);

                break;

            case CHAR:
                writeChar((Character)val);

                break;

            case BOOLEAN:
                writeBoolean((Boolean)val);

                break;

            case BYTE_ARR:
                writeByteArray((byte[])val);

                break;

            case SHORT_ARR:
                writeShortArray((short[])val);

                break;

            case INT_ARR:
                writeIntArray((int[])val);

                break;

            case LONG_ARR:
                writeLongArray((long[])val);

                break;

            case FLOAT_ARR:
                writeFloatArray((float[])val);

                break;

            case DOUBLE_ARR:
                writeDoubleArray((double[])val);

                break;

            case CHAR_ARR:
                writeCharArray((char[])val);

                break;

            case BOOLEAN_ARR:
                writeBooleanArray((boolean[])val);

                break;

            case STRING:
                writeString((String)val);

                break;

            case BIT_SET:
                writeBitSet((BitSet)val);

                break;

            case UUID:
                writeUuid((UUID)val);

                break;

            case IGNITE_UUID:
                writeIgniteUuid((IgniteUuid)val);

                break;

            case MSG:
                try {
                    if (val != null)
                        writer.beforeInnerMessageWrite();

                    writeMessage((Message)val, writer);
                }
                finally {
                    if (val != null)
                        writer.afterInnerMessageWrite(true);
                }

                break;

            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    /**
     * @param type Type.
     * @param reader Reader.
     * @return Value.
     */
    private Object read(MessageCollectionItemType type, MessageReader reader) {
        switch (type) {
            case BYTE:
                return readByte();

            case SHORT:
                return readShort();

            case INT:
                return readInt();

            case LONG:
                return readLong();

            case FLOAT:
                return readFloat();

            case DOUBLE:
                return readDouble();

            case CHAR:
                return readChar();

            case BOOLEAN:
                return readBoolean();

            case BYTE_ARR:
                return readByteArray();

            case SHORT_ARR:
                return readShortArray();

            case INT_ARR:
                return readIntArray();

            case LONG_ARR:
                return readLongArray();

            case FLOAT_ARR:
                return readFloatArray();

            case DOUBLE_ARR:
                return readDoubleArray();

            case CHAR_ARR:
                return readCharArray();

            case BOOLEAN_ARR:
                return readBooleanArray();

            case STRING:
                return readString();

            case BIT_SET:
                return readBitSet();

            case UUID:
                return readUuid();

            case IGNITE_UUID:
                return readIgniteUuid();

            case MSG:
                return readMessage(reader);

            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    /**
     * Array creator.
     */
    private interface ArrayCreator<T> {
        /**
         * @param len Array length or {@code -1} if array was not fully read.
         * @return New array.
         */
        T create(int len);
    }

    private class OutputStreamEx extends OutputStream {
        @Override
        public void write(int b) {
            checkCapacity(1);

            GridUnsafe.putByte(heapArr, BYTE_ARR_OFF + dataObject.position(), (byte)(b & 0xff));

            move(1);
        }

        @Override
        public void write(byte b[], int off, int len) {
            if (b == null)
                throw new NullPointerException();

            if ((off < 0) || (off > b.length) || (len < 0) ||
                ((off + len) > b.length) || ((off + len) < 0))
                throw new IndexOutOfBoundsException();

            if (len == 0)
                return;

            checkCapacity(len);

            GridUnsafe.copyMemory(b, BYTE_ARR_OFF + off, heapArr, BYTE_ARR_OFF + dataObject.position(), len);

            move(len);
        }
    }

    private class InputStreamEx extends InputStream {
        @Override
        public int read() {
            int res = -1;

            if (available() > 0) {
                res = GridUnsafe.getByte(heapArr, BYTE_ARR_OFF + dataObject.position()) & 0xff;

                move(1);
            }

            return res;
        }

        @Override
        public int read(byte[] b, int off, int len) {
            if (b == null)
                throw new NullPointerException();

            if (off < 0 || len < 0 || len > b.length - off)
                throw new IndexOutOfBoundsException();

            if (len == 0)
                return 0;

            if (available() == 0)
                return -1;

            len = Math.min(len, available());

            GridUnsafe.copyMemory(heapArr, BYTE_ARR_OFF + dataObject.position(), b, BYTE_ARR_OFF + off, len);

            move(len);

            return len;
        }

        @Override
        public int available() {
            assert dataObject.limit() >= dataObject.position();

            return dataObject.limit() - dataObject.position();
        }
    }
}
