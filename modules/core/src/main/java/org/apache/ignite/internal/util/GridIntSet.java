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

package org.apache.ignite.internal.util;

import org.apache.ignite.internal.util.typedef.internal.U;

import java.io.Serializable;
import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * Holds set of integers.
 * <p/>
 * Structure:
 * <p/>
 * Each segment stores SEGMENT_SIZE values.
 * <p/>
 * On example, segment size 1024:
 * <p/>
 * segIds[0]: 0, highest bit cleared && segments[0].length < THRESHOLD, segments[0]=  [0, 5, 10, 1023] - stores present values
 * segIds[1]: 5, highest bit set, segments[1]=  [0, 5, 10, 10243 - stores absent values
 * segIds[2]: 10, segments[0].length == THRESHOLD, segments[2]=  [0, 5, 10, 1024] - stores values as bits
 * <p/>
 * <p/>
 * Note: implementation is not thread safe.
 *
 * TODO equals/hashcode
 */
public class GridIntSet implements Serializable {
    enum Mode {
        STRAIGHT, BITSET, INVERTED
    }

    /** */
    private static final long serialVersionUID = 0L;

    /**
     * Segment index. Sorted in ascending order by segment id.
     */
    private short[] segIds = new short[1]; // TODO FIXME allow preallocate.

    private Mode[] modes = new Mode[] {Mode.STRAIGHT};

    private short segmentsUsed = 0;

    private short[][] segments = new short[1][0];

    public static final short SEGMENT_SIZE = 1024;

    public static final int SHORT_BITS = Short.SIZE;

    public static final int MAX_WORDS = SEGMENT_SIZE / SHORT_BITS;

    public static final int WORD_SHIFT_BITS = 4;

    public static final int MAX_SEGMENTS = Short.MAX_VALUE;

    public static final int THRESHOLD = SEGMENT_SIZE / 8; // TODO bit shift

    public static final int THRESHOLD2 = SEGMENT_SIZE - THRESHOLD;

    public static final short MASK = (short) (1 << (SHORT_BITS - 1));

    private static final int WORD_MASK = 0xFFFF;

    public GridIntSet() {
    }

    public GridIntSet(int first, int cnt) {
    }

    public void add(int v) {
        //U.debug("Add " + v);

        short idx = (short) (v / SEGMENT_SIZE);

        short inc = (short) (v - idx * SEGMENT_SIZE); // TODO use modulo bit hack.

        // Determine addition type.

        int segIdx = segmentIndex(idx); // TODO binary search.

        short[] segment = segments[segIdx];

        switch (modes[segIdx]) {
            case STRAIGHT:
                segments[segIdx] = insertArray(modes, segIdx, segment, inc);
                break;
            case BITSET:
                segments[segIdx] = insertBitSet(segment, inc);
                break;
            case INVERTED:
                break;
        }
    }

    public void remove(int v) {
        short idx = (short) (v / SEGMENT_SIZE);

        short inc = (short) (v - idx * SEGMENT_SIZE); // TODO use modulo bit hack.

        int segIdx = segmentIndex(idx); // TODO binary search.

        short[] segment = segments[segIdx];

        switch (modes[segIdx]) {
            case STRAIGHT:
                removeArray(segment, inc);
                break;
            case BITSET:
                break;
            case INVERTED:
                break;
        }
    }

    public boolean contains(int v) {
        return false;
    }

    private int segmentIndex(int v) {
        //assert segIds.length > 0 && segments.length > 0 : "At least one segment";

        return 0;
    }

    public void dump() {
        for (short[] segment : segments) {
            for (short word : segment)
                U.debug(Integer.toBinaryString(word & 0xFFFF));
        }
    }

    public Iterator iterator() {
        int segIdx = 0;

        switch (modes[segIdx]) {
            case STRAIGHT:
                return new RawIterator(segments[0]);
            case BITSET:
                return new BitSetIterator(segments[0]);
            case INVERTED:
                break;
        }

        return new BitSetIterator(segments[0]);
    }

    public int size() {
        return 0;
    }

    /** */
    private static class RawIterator implements Iterator {
        /** Vals. */
        private final short[] vals;

        /** Word index. */
        private short wordIdx;

        /** Limit. */
        private final int limit;

        /**
         * @param segment Segment.
         */
        public RawIterator(short[] segment) {
            this.vals = segment;
            this.wordIdx = 0;
            this.limit = segment.length;
        }

        /** {@inheritDoc} */
        @Override public boolean hasNext() {
            return wordIdx < limit;
        }

        /** {@inheritDoc} */
        @Override public int next() {
            return vals[wordIdx++];
        }

        @Override public int size() {
            return this.limit;
        }
    }

    /** */
    private static class ArrayIterator implements Iterator {
        /** Vals. */
        private final short[] vals;

        /** Word index. */
        private short wordIdx;

        /** Limit. */
        private final int limit;

        /**
         * @param segment Segment.
         */
        public ArrayIterator(short[] segment) {
            this.vals = segment;
            this.wordIdx = 0;
            this.limit = freeIndex(segment);
        }

        /** {@inheritDoc} */
        @Override public boolean hasNext() {
            return wordIdx < limit;
        }

        /** {@inheritDoc} */
        @Override public int next() {
            return vals[wordIdx++];
        }

        /** {@inheritDoc} */
        @Override public int size() {
            return this.limit;
        }
    }

    /** */
    private static class BitSetIterator implements Iterator {
        private final short[] words;

        private int bit;

        /**
         * @param words Words.
         */
        public BitSetIterator(short[] words) {
            this.words = words;

            this.bit = nextSetBit(words, 0);
        }

        /** {@inheritDoc} */
        @Override public boolean hasNext() {
            return bit != -1;
        }

        /** {@inheritDoc} */
        @Override public int next() {
            if (bit != -1) {
                int ret = bit;

                bit = nextSetBit(words, bit+1);

                return ret;
            } else
                throw new NoSuchElementException();
        }

        /** {@inheritDoc} */
        @Override public int size() {
            return 0;
        }
    }

    /** */
    private static short[] insertBitSet(short[] words, short bit) {
        int wordIdx = wordIndex(bit);

        short[] tmp = words;

        if (wordIdx >= words.length)
            tmp = Arrays.copyOf(words, Math.min(MAX_WORDS, Math.max(words.length * 2, wordIdx + 1)));

        short wordBit = (short) (bit - wordIdx * SHORT_BITS);

        assert 0 <= wordBit && wordBit < SHORT_BITS : "Word bit is within range";

        try {
            tmp[(wordIdx)] |= (1 << wordBit);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tmp;
    }

    /**
     * @param val Value.
     */
    private static int wordIndex(int val) {
        return val >> WORD_SHIFT_BITS;
    }

    /** */
    private static int nextSetBit(short[] words, int fromIdx) {
        int u = wordIndex(fromIdx);

        if (u >= words.length)
            return -1;

        int shift = fromIdx & (SHORT_BITS - 1);

        short word = (short)((words[u] & 0xFFFF) & (WORD_MASK << shift));

        while (true) {
            if (word != 0)
                return (u * SHORT_BITS) + Long.numberOfTrailingZeros(word & 0xFFFF);

            if (++u == words.length)
                return -1;

            word = words[u];
        }
    }

    /** */
    private static short[] insertArray(Mode[] modes, int mIdx, short[] arr, short base) {
        assert base < SEGMENT_SIZE: base;

        if (arr == null || arr.length == 0) {
            arr = new short[1];

            arr[0] = base;

            return arr;
        }

        int freeIdx = freeIndex(arr);

        assert 0 <= freeIdx;

        int idx = Arrays.binarySearch(arr, 0, freeIdx, base);

        if (idx >= 0)
            return arr; // Already exists.

        if (freeIdx == THRESHOLD - 1) { // Convert to bitset on reaching threshold.
            ArrayIterator it = new ArrayIterator(arr);

            short[] tmp = new short[THRESHOLD];

            while(it.hasNext()) {
                int val = it.next();

                tmp = insertBitSet(tmp, (short) val);
            }

            tmp = insertBitSet(tmp, base);

            modes[mIdx] = Mode.BITSET;

            return tmp;
        }

        int pos = -(idx + 1);

        // Insert a segment.

        if (freeIdx >= arr.length) {
            int newSize = Math.min(arr.length * 2, THRESHOLD);

            arr = Arrays.copyOf(arr, newSize);
        }

        System.arraycopy(arr, pos, arr, pos + 1, freeIdx - pos);

        arr[pos] = base;

        return arr;
    }

    public void flip() {
        segments[0] = flipArray(new ArrayIterator(segments[0]));
    }

    private static void removeArray(short[] arr, short base) {
        assert base < SEGMENT_SIZE: base;

        int freeIdx = freeIndex(arr);

        assert 0 <= freeIdx;

        int idx = Arrays.binarySearch(arr, 0, freeIdx, base);

        if (idx < 0)
            return;

        int moved = freeIdx - idx - 1;

        if (moved > 0)
            System.arraycopy(arr, idx + 1, arr, idx, moved);

        arr[freeIdx - 1] = 0;
    }

    private short[] flipArray(Iterator it) {
        int i = 0;

        short[] tmp = new short[0];

        while(it.hasNext()) {
            int id = it.next();

            for (; i < id; i++)
                tmp = insertArray(modes, 0, tmp, (short) i);

            i = id + 1;
        }

        while(i < 128)
            tmp = insertArray(modes, 0, tmp, (short) i++);

        return tmp;
    }

    /**
     *
     * @param arr Array.
     * @return Index of first empty item in array.
     */
    private static int freeIndex(short[] arr) {
        // TODO FIXME use binary search to find last used word ?
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] == 0)
                return i;
        }

        return arr.length;
    }

    public static void main(String[] args) {
        GridIntSet set = new GridIntSet();

        set.add(5);
        set.add(10);
        System.out.println(set.toString());

        set.add(15);
        System.out.println(set.toString());

        set.add(30);
        System.out.println(set.toString());

        set.add(35);
        System.out.println(set.toString());

        set.flip();
        System.out.println(set.toString());

        set.flip();
        System.out.println(set.toString());

        set.flip();
        System.out.println(set.toString());

        set.flip();
        System.out.println(set.toString());

//        set.remove(11);
//        System.out.println(set.toString());
//
//        set.remove(5);
//        System.out.println(set.toString());
//
//        set.remove(15);
//        System.out.println(set.toString());
//
//        set.remove(10);
//        System.out.println(set.toString());
//
//        set.remove(30);
//        System.out.println(set.toString());

//        for (short val = 0; val < SEGMENT_SIZE; val++)
//            set.add(val);
//
//        Iterator it = set.iterator();
//        while(it.hasNext()) {
//            int id = it.next();
//
//            System.out.print(id);
//            System.out.print(" ");
//        }
//
//        System.out.println();
    }

    public static interface Iterator {

        public boolean hasNext();

        public int next();

        public int size();
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        StringBuilder b = new StringBuilder("[");

        Iterator it = iterator();

        while(it.hasNext()) {
            b.append(it.next());

            if (it.hasNext()) b.append(", ");
        }

        b.append("]");

        return b.toString();
    }
}
