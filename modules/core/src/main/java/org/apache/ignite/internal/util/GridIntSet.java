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
    /** */
    private static final long serialVersionUID = 0L;

    public static final short SEGMENT_SIZE = 1024;

    public static final int SHORT_BITS = Short.SIZE;

    public static final int MAX_WORDS = SEGMENT_SIZE / SHORT_BITS;

    public static final int WORD_SHIFT_BITS = 4;

    public static final int MAX_SEGMENTS = Short.MAX_VALUE;

    public static final int THRESHOLD = SEGMENT_SIZE / SHORT_BITS; // TODO bit shift

    public static final int THRESHOLD2 = SEGMENT_SIZE - THRESHOLD;

    public static final short MASK = (short) (1 << (SHORT_BITS - 1));

    private static final int WORD_MASK = 0xFFFF;

    /**
     * Segment index. Sorted in ascending order by segment id.
     */
    private short[] segIds = new short[1]; // TODO FIXME allow preallocate.

    private short segmentsUsed = 0;

    private Segment[] segments = new Segment[1];

    public GridIntSet() {
        // TODO set segment size - power of two.
        segments[0] = new ArraySegment();
    }

    public GridIntSet(int first, int cnt) {
    }

    public void add(int v) {
        //U.debug("Add " + v);

        short div = (short) (v / SEGMENT_SIZE);

        short mod = (short) (v - div * SEGMENT_SIZE); // TODO use modulo bit hack.

        // Determine addition type.

        int segIdx = segmentIndex(div); // TODO binary search.

        try {
            segments[segIdx].add(mod);
        } catch (ConvertException e) {
            segments[segIdx] = e.segment();
        }
    }

    public void remove(int v) {
        short div = (short) (v / SEGMENT_SIZE);

        short mod = (short) (v - div * SEGMENT_SIZE); // TODO use modulo bit hack.

        int segIdx = segmentIndex(div); // TODO binary search.

        try {
            segments[segIdx].remove(mod);
        } catch (ConvertException e) {
            e.printStackTrace();
        }
    }

    public boolean contains(int v) {
        short div = (short) (v / SEGMENT_SIZE);

        short mod = (short) (v - div * SEGMENT_SIZE); // TODO use modulo bit hack.

        return segments[0].contains(mod);
    }

    private int segmentIndex(int v) {
        //assert segIds.length > 0 && segments.length > 0 : "At least one segment";

        return 0;
    }

    public Iterator iterator() {
        return segments[0].iterator();
    }

    public int size() {
        return segments[0].size();
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

    public interface Segment {
        public short[] data();

        /**
         * Returns number of element if array used to hold data.
         * @return used elements count.
         */
        public int used();

        public void add(short val) throws ConvertException;

        public void remove(short base) throws ConvertException;

        public void flip() throws ConvertException;

        public boolean contains(short v);

        public int size();

        public int first();

        public int last();

        public Iterator iterator();
    }

    /**
     * TODO store used counter.
     */
    public static class BitSetSegment implements Segment {
        short[] words;

        public BitSetSegment() {
            this(0);
        }

        public BitSetSegment(int maxValue) {
            assert maxValue <= SEGMENT_SIZE;

            words = new short[1 << (Integer.SIZE - Integer.numberOfLeadingZeros(wordIndex(maxValue)))];
        }

        @Override public int used() {
            return words.length;
        }

        @Override public void add(short val) {
            int wordIdx = wordIndex(val);

            if (wordIdx >= words.length)
                words = Arrays.copyOf(words, Math.min(MAX_WORDS, Math.max(words.length * 2, wordIdx + 1))); // TODO shift

            short wordBit = (short) (val - wordIdx * SHORT_BITS); // TODO FIXME

            assert 0 <= wordBit && wordBit < SHORT_BITS : "Word bit is within range";

            words[(wordIdx)] |= (1 << wordBit);
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

        @Override public void remove(short val) {
            int wordIdx = wordIndex(val);

            short wordBit = (short) (val - wordIdx * SHORT_BITS); // TODO FIXME

            int mask = 1 << wordBit;

            words[wordIdx] &= ~mask;
        }

        @Override public boolean contains(short val) {
            int wordIdx = wordIndex(val);

            short wordBit = (short) (val - wordIdx * SHORT_BITS); // TODO FIXME

            int mask = 1 << wordBit;

            return (words[wordIdx] & mask) == mask;
        }

        @Override public short[] data() {
            return words;
        }

        @Override public void flip() {

        }

        @Override public int size() {
            int size = 0;

            int used = used();

            for (int i = 0; i < used; i++)
                size += Integer.bitCount(words[i] & 0xFFFF);

            return size;
        }

        @Override public int first() {
            return nextSetBit(words, 0);
        }

        @Override public int last() {
            return -1; // TODO
        }

        @Override public Iterator iterator() {
            return new BitSetIterator(words);
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
    }

    /** */
    public static class ArraySegment implements Segment {
        private short[] data;

        public ArraySegment() {
            this(0);
        }

        public ArraySegment(int size) {
            this.data = new short[size];
        }

        /** {@inheritDoc} */
        @Override public short[] data() {
            return data;
        }

        /**
         *
         * @return Used items.
         */
        public int used() {
            // TODO FIXME use binary search to find last used word ?
            for (int i = 1; i < data.length; i++) {
                if (data[i] == 0)
                    return i;
            }

            return data.length;
        }

        /** */
        public void add(short val) throws ConvertException {
            assert val < SEGMENT_SIZE: val;

            if (data == null || data.length == 0) {
                data = new short[1];

                data[0] = val;

                return;
            }

            int freeIdx = used();

            assert 0 <= freeIdx;

            int idx = Arrays.binarySearch(data, 0, freeIdx, val);

            if (idx >= 0)
                return; // Already exists.

            if (freeIdx == THRESHOLD) { // Convert to bit set on reaching threshold.
                Segment converted = toBitSetSegment();

                converted.add(val);

                throw new ConvertException(converted);
            }

            int pos = -(idx + 1);

            // Insert a segment.

            if (freeIdx >= data.length) {
                int newSize = Math.min(data.length * 2, THRESHOLD);

                data = Arrays.copyOf(data, newSize);
            }

            System.arraycopy(data, pos, data, pos + 1, freeIdx - pos);

            data[pos] = val;
        }

        /** {@inheritDoc} */
        public void remove(short base) throws ConvertException {
            assert base < SEGMENT_SIZE: base;

            int used = used();

            assert 0 <= used;

            int idx = Arrays.binarySearch(data, 0, used, base);

            if (idx < 0)
                return;

            int moved = used - idx - 1;

            if (moved > 0)
                System.arraycopy(data, idx + 1, data, idx, moved);

            data[used - 1] = 0;
        }

        /** {@inheritDoc} */
        public boolean contains(short v) {
            return Arrays.binarySearch(data, v) >= 0;
        }

        @Override public int size() {
            return used();
        }

        @Override public int first() {
            return data[0];
        }

        @Override public int last() {
            return data[used() - 1];
        }

        /** {@inheritDoc} */
        public Iterator iterator() {
            return new ArrayIterator(data, size());
        }

        /** {@inheritDoc} */
        public void flip() {
//            Iterator it = iterator();
//
//            int i = 0;
//
//            ArraySegment seg = new ArraySegment(Mode.NORMAL);
//
//            while(it.hasNext()) {
//                int id = it.next();
//
//                for (; i < id; i++)
//                    seg.add((short) i);
//
//                i = id + 1;
//            }
//
//            while(i < SEGMENT_SIZE)
//                seg.add((short) i++);
//
//            this.data = seg.data();
//
//            seg.mode = mode == Mode.NORMAL ? Mode.INVERTED : Mode.NORMAL;
        }

        /** {@inheritDoc} */
        private Segment toBitSetSegment() throws ConvertException {
            Segment seg = new BitSetSegment(last());

            Iterator it = iterator();

            while(it.hasNext())
                seg.add((short) it.next());

            return seg;
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
            ArrayIterator(short[] segment, int size) {
                this.vals = segment;
                this.wordIdx = 0;
                this.limit = size;
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
    }

    public static class InvertedArraySegment extends ArraySegment {
        public InvertedArraySegment() {
            this(0);
        }

        public InvertedArraySegment(int size) {
            super(size);
        }

        @Override public void add(short val) throws ConvertException {
            super.remove(val);
        }

        @Override public void remove(short base) throws ConvertException {
            super.add(base);
        }

        @Override public boolean contains(short v) {
            return !super.contains(v);
        }

        @Override public int size() {
            return THRESHOLD - super.size();
        }
    }

    /** */
    private static class ConvertException extends Exception {
        private Segment segment;

        public ConvertException(Segment segment) {
            this.segment = segment;
        }

        /**
         * @return Segment.
         */
        public Segment segment() {
            return segment;
        }

        /**
         * @param segment New segment.
         */
        public void segment(Segment segment) {
            this.segment = segment;
        }
    }
}

