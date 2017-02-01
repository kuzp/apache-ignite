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
 * TODO FIXME cache bit masks like 1 << shift ?
 */
public class GridIntSet implements Serializable {
    /** */
    private static final long serialVersionUID = 0L;

    public static final short SEGMENT_SIZE = 1024;

    public static final int SHORT_BITS = Short.SIZE;

    public static final int MAX_WORDS = SEGMENT_SIZE / SHORT_BITS;

    public static final int WORD_SHIFT_BITS = 4;

    public static final int MAX_SEGMENTS = Short.MAX_VALUE;

    public static final int THRESHOLD = MAX_WORDS;

    public static final int THRESHOLD2 = SEGMENT_SIZE - THRESHOLD;

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
        /** Segment data. */
        public short[] data();

        /**
         * Returns number of element if array used to hold data.
         * @return used elements count.
         */
        public int used();

        public boolean add(short val) throws ConvertException;

        public boolean remove(short base) throws ConvertException;

        public void flip() throws ConvertException;

        public boolean contains(short v);

        public int size();

        public int first();

        public int last();

        public Iterator iterator();

        public Iterator reverseIterator();
    }

    /**
     * TODO store used counter.
     */
    public static class BitSetSegment implements Segment {
        short[] words;

        private int count;

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

        @Override public boolean add(short val) throws ConvertException {
            int wordIdx = wordIndex(val);

            if (wordIdx >= words.length)
                words = Arrays.copyOf(words, Math.min(MAX_WORDS, Math.max(words.length * 2, wordIdx + 1))); // TODO shift

            short wordBit = (short) (val - wordIdx * SHORT_BITS); // TODO FIXME

            assert 0 <= wordBit && wordBit < SHORT_BITS : "Word bit is within range";

            int mask = (1 << wordBit);

            boolean exists = (words[(wordIdx)] & mask) == mask;

            if (!exists) {
                if (count == THRESHOLD2) { // convert to inverted array set.
                    Segment seg = convertToInvertedArraySet();

                    seg.add(val);

                    throw new ConvertException(seg);
                }

                count++;

                words[(wordIdx)] |= mask;
            }
            return exists;
        }

        @Override public boolean remove(short val) throws ConvertException {
            int wordIdx = wordIndex(val);

            short wordBit = (short) (val - wordIdx * SHORT_BITS); // TODO FIXME

            int mask = 1 << wordBit;

            boolean exists = (words[(wordIdx)] & mask) == mask;

            if (exists) {
                count--;

                words[wordIdx] &= ~mask;

                if (count < THRESHOLD)
                    throw new ConvertException(convertToArraySet());
            }
            return exists;
        }

        /** */
        private Segment convertToInvertedArraySet() throws ConvertException {
            FlippedArraySegment seg = new FlippedArraySegment();

            Iterator it = iterator();

            int i = 0;

            while (it.hasNext()) {
                int id = it.next();

                for (; i < id; i++)
                    seg.remove((short) i);

                i = id + 1;
            }

            while(i < SEGMENT_SIZE)
                seg.remove((short) i++);

            return seg;
        }

        private Segment convertToArraySet() throws ConvertException {
            ArraySegment seg = new ArraySegment(THRESHOLD);

            Iterator it = iterator();

            while (it.hasNext()) {
                int id = it.next();

                seg.add((short) id);
            }

            return seg;
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
            return count;
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

        @Override public Iterator reverseIterator() {
            return new ReverseBitSetIterator(words);
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
        }

        /** */
        private static class ReverseBitSetIterator implements Iterator {
            private final short[] words;

            private int bit;

            /**
             * @param words Words.
             */
            public ReverseBitSetIterator(short[] words) {
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
        }
    }

    /** */
    public static class ArraySegment implements Segment {
        private short[] data;

        private short used;

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
         * @return Used items.
         */
        public int used() {
            return used;
        }

        /** */
        public boolean add(short val) throws ConvertException {
            assert val < SEGMENT_SIZE: val;

            if (used == 0) {
                data = new short[1];

                data[0] = val;

                used++;

                return true;
            }

            int freeIdx = used();

            assert 0 <= freeIdx;

            int idx = Arrays.binarySearch(data, 0, freeIdx, val);

            if (idx >= 0)
                return false; // Already exists.

            if (freeIdx == THRESHOLD) { // Convert to bit set on reaching threshold.
                Segment converted = convertToBitSetSegment(val);

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

            used++;

            return true;
        }

        /** {@inheritDoc} */
        public boolean remove(short base) throws ConvertException {
            assert base < SEGMENT_SIZE: base;

            int idx = Arrays.binarySearch(data, 0, used, base);

            if (idx < 0)
                return false;

            short[] tmp = data;

            int moved = used - idx - 1;

            if (moved > 0)
                System.arraycopy(data, idx + 1, tmp, idx, moved);

            used--;

            data[used] = 0;

            if (used == (data.length >> 1))
                data = Arrays.copyOf(data, used);

            return true;
        }

        /** {@inheritDoc} */
        public boolean contains(short v) {
            return Arrays.binarySearch(data, 0, used, v) >= 0;
        }

        @Override public int size() {
            return used();
        }

        @Override public int first() {
            return size() == 0 ? -1 : data[0]; // TODO FIXME size issue.
        }

        @Override public int last() {
            return size() == 0 ? -1 : data[used() - 1];
        }

        /** {@inheritDoc} */
        public Iterator iterator() {
            return new ArrayIterator(data, size());
        }

        /** {@inheritDoc} */
        @Override public Iterator reverseIterator() {
            return new ReverseArrayIterator(data, size());
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

        /** {@inheritDoc}
         * @param val*/
        private Segment convertToBitSetSegment(short val) throws ConvertException {
            Segment seg = new BitSetSegment(val);

            Iterator it = iterator();

            while(it.hasNext())
                seg.add((short) it.next());

            seg.add(val);

            return seg;
        }

        /** */
        static class ArrayIterator implements Iterator {
            /** Values. */
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
        }

        /** */
        static class ReverseArrayIterator implements Iterator {
            /** Values. */
            private final short[] vals;

            /** Word index. */
            private short wordIdx;

            /** Limit. */
            private final int limit;

            /**
             * @param segment Segment.
             */
            ReverseArrayIterator(short[] segment, int size) {
                this.vals = segment;
                this.wordIdx = (short) (size - 1);
                this.limit = 0;
            }

            /** {@inheritDoc} */
            @Override public boolean hasNext() {
                return wordIdx >= limit;
            }

            /** {@inheritDoc} */
            @Override public int next() {
                return vals[wordIdx--];
            }
        }
    }

    public static class FlippedArraySegment extends ArraySegment {
        public FlippedArraySegment() {
            this(0);
        }

        public FlippedArraySegment(int size) {
            super(size);
        }

        @Override public boolean add(short val) throws ConvertException {
            return super.remove(val);
        }

        @Override public boolean remove(short base) throws ConvertException {
            return super.add(base);
        }

        @Override public boolean contains(short v) {
            return !super.contains(v);
        }

        @Override public int size() {
            return SEGMENT_SIZE - super.size();
        }

        @Override public int first() {
            Iterator iter = iterator();

            if (!iter.hasNext())
                return -1;

            return iter.next();
        }

        @Override public int last() {
            Iterator iter = reverseIterator();

            if (!iter.hasNext())
                return -1;

            return iter.next();
        }

        @Override public Iterator iterator() {
            return new FlippedArrayIterator(data(), super.size());
        }

        @Override public Iterator reverseIterator() {
            return new FlippedReverseArrayIterator(data(), super.size());
        }

        private Segment convertToBitSetSegment() throws ConvertException {
            Segment seg = new BitSetSegment(last());

            Iterator it = iterator();

            while(it.hasNext())
                seg.add((short) it.next());

            return seg;
        }

        /** */
        private static class FlippedArrayIterator extends ArrayIterator {
            private int skipIdx = -1;

            private int val;

            /**
             * @param segment Segment.
             */
            FlippedArrayIterator(short[] segment, int size) {
                super(segment, size);

                if (super.hasNext())
                    skipIdx = super.next();
            }

            /** {@inheritDoc} */
            @Override public boolean hasNext() {
                return val < SEGMENT_SIZE;
            }

            /** {@inheritDoc} */
            @Override public int next() {
                while(val == skipIdx && super.hasNext()) {
                    skipIdx = super.next();

                    val++;
                }
                return val++;
            }
        }

        /** */
        private static class FlippedReverseArrayIterator extends ReverseArrayIterator {
            private int skipIdx = -1;

            private int val;

            /**
             * @param segment Segment.
             */
            FlippedReverseArrayIterator(short[] segment, int size) {
                super(segment, size);

                if (super.hasNext())
                    skipIdx = super.next();

                val = SEGMENT_SIZE - 1;
            }

            /** {@inheritDoc} */
            @Override public boolean hasNext() {
                return val >= 0;
            }

            /** {@inheritDoc} */
            @Override public int next() {
                while(val == skipIdx && super.hasNext()) {
                    skipIdx = super.next();

                    val--;
                }
                return val--;
            }
        }
    }

    /** */
    public static class ConvertException extends Exception {
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

