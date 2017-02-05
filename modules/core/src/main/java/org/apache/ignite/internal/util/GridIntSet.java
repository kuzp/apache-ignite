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
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Holds set of integers.
 * <p>
 * Structure:
 * <p>
 * Each segment stores SEGMENT_SIZE values, possibly in compressed format.
 * <p>
 * Used storage format depends on segmen's fill factor.
 * <p>
 * Note: implementation is not thread safe.
 * <p>
 * TODO equals/hashcode
 * TODO FIXME cache bit masks like 1 << shift ?
 * TODO HashSegment worth it?
 * TODO replace power of two arythmetics with bit ops.
 */
public class GridIntSet implements Serializable {
    /** */
    private static final long serialVersionUID = 0L;

    public static final short SEGMENT_SIZE = 1024;

    private static final int SEGMENT_SHIFT_BITS = Integer.numberOfTrailingZeros(SEGMENT_SIZE);

    private static final int SHORT_BITS = Short.SIZE;

    private static final int MAX_WORDS = SEGMENT_SIZE / SHORT_BITS;

    private static final int WORD_SHIFT_BITS = 4;

    private static final int THRESHOLD = MAX_WORDS;

    private static final int THRESHOLD2 = SEGMENT_SIZE - THRESHOLD;

    private static final int WORD_MASK = 0xFFFF;

    private Segment indices = new ArraySegment(16);

    private Map<Short, Segment> segments = new HashMap<>();

    public boolean add(int v) {
        short segIdx = (short) (v >> SEGMENT_SHIFT_BITS);

        short segVal = (short) (v & (SEGMENT_SIZE - 1));

        Segment seg;

        try {
            boolean added = indices.add(segIdx);

            if (added)
                segments.put(segIdx, (seg = new ArraySegment()));
            else
                seg = segments.get(segIdx);
        } catch (ConvertException e) {
            indices = e.segment;

            segments.put(segIdx, (seg = new ArraySegment()));
        }

        try {
            return seg.add(segVal);
        } catch (ConvertException e) {
            segments.put(segIdx, e.segment);
        }

        return true;
    }

    public boolean remove(int v) {
        short segIdx = (short) (v >> SEGMENT_SHIFT_BITS);

        short segVal = (short) (v & (SEGMENT_SIZE - 1));

        Segment segment = segments.get(segIdx);

        if (segment == null)
            return false;

        try {
            return segment.remove(segVal);
        } catch (ConvertException e) {
            segments.put(segIdx, e.segment);
        }

        return true;
    }

    public boolean contains(int v) {
        short segIdx = (short) (v >> SEGMENT_SHIFT_BITS);

        short segVal = (short) (v & (SEGMENT_SIZE - 1));

        Segment segment = segments.get(segIdx);

        if (segment == null)
            return false;

        return segment.contains(segVal);
    }

    public int first() {
        short first = indices.first();

        if (first == -1)
            return -1;

        return segments.get(first).first() + first * SEGMENT_SIZE;
    }

    public int last() {
        short last = indices.last();

        if (last == -1)
            return -1;

        return segments.get(last).last() + last * SEGMENT_SIZE;
    }

    private abstract class IteratorImpl implements Iterator {
        private final Iterator segIter;

        private Iterator it;

        private int idx;

        public IteratorImpl() {
            this.segIter = getIt(indices);

            advance();
        }

        private void advance() {
            if (segIter.hasNext()) {
                idx = (short) segIter.next();

                Segment segment = segments.get((short)idx);

                it = getIt(segment);
            } else
                it = null;
        }

        protected abstract Iterator getIt(Segment segment);

        @Override public boolean hasNext() {
            return it != null && it.hasNext();
        }

        @Override public int next() {
            int id = it.next() + idx * SEGMENT_SIZE;

            advance();

            return id;
        }
    }

    /**
     *
     */
    public Iterator iterator() {
        return new IteratorImpl() {
            @Override protected Iterator getIt(Segment segment) {
                return segment.iterator();
            }
        };
    }

    /**
     *
     */
    public Iterator reverseIterator() {
        return new IteratorImpl() {
            @Override protected Iterator getIt(Segment segment) {
                return segment.reverseIterator();
            }
        };
    }

    public int size() {
        int size = 0;

        for (Segment segment : segments.values())
            size += segment.size();

        return size;
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

        public boolean contains(short v);

        public int size();

        /**
         * Returns min allowed size.
         * @return Min size.
         */
        public int minSize();

        /**
         * Returns max allowed size.
         * @return Max size.
         */
        public int maxSize();

        public short first();

        public short last();

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

            if (exists)
                return false;

            if (count == THRESHOLD2) { // convert to inverted array set.
                Segment seg = convertToInvertedArraySet();

                seg.add(val);

                throw new ConvertException(seg);
            }

            count++;

            words[(wordIdx)] |= mask;

            return true;
        }

        @Override public boolean remove(short val) throws ConvertException {
            int wordIdx = wordIndex(val);

            short wordBit = (short) (val - wordIdx * SHORT_BITS); // TODO FIXME

            int mask = 1 << wordBit;

            boolean exists = (words[(wordIdx)] & mask) == mask;

            if (!exists)
                return false;

            count--;

            words[wordIdx] &= ~mask;

            if (count < THRESHOLD)
                throw new ConvertException(convertToArraySet());

            return true;
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

        /** TODO needs refactoring */
        private static short nextSetBit(short[] words, int fromIdx) {
            if (fromIdx < 0)
                return -1;

            int wordIdx = wordIndex(fromIdx);

            if (wordIdx >= words.length)
                return -1;

            int shift = fromIdx & (SHORT_BITS - 1);

            short word = (short)((words[wordIdx] & 0xFFFF) & (WORD_MASK << shift));

            while (true) {
                if (word != 0)
                    return (short) ((wordIdx * SHORT_BITS) + Integer.numberOfTrailingZeros(word & 0xFFFF));

                if (++wordIdx == words.length)
                    return -1;

                word = words[wordIdx];
            }
        }

        /** */
        private static short prevSetBit(short[] words, int fromIdx) {
            if (fromIdx < 0)
                return -1;

            int wordIdx = wordIndex(fromIdx);

            if (wordIdx >= words.length)
                return -1;

            int shift = SHORT_BITS - (fromIdx & (SHORT_BITS - 1)) - 1;

            short word = (short)((words[wordIdx] & 0xFFFF) & (WORD_MASK >> shift));

            while (true) {
                if (word != 0)
                    return (short) ((wordIdx * SHORT_BITS) + SHORT_BITS - 1 - (Integer.numberOfLeadingZeros(word & 0xFFFF) - SHORT_BITS));

                if (--wordIdx == -1)
                    return -1;

                word = words[wordIdx];
            }
        }

        @Override public boolean contains(short val) {
            int wordIdx = wordIndex(val);

            if (wordIdx >= used())
                return false;

            short wordBit = (short) (val - wordIdx * SHORT_BITS); // TODO FIXME

            int mask = 1 << wordBit;

            return (words[wordIdx] & mask) == mask;
        }

        @Override public short[] data() {
            return words;
        }

        @Override public int size() {
            return count;
        }

        @Override public int minSize() {
            return THRESHOLD;
        }

        @Override public int maxSize() {
            return THRESHOLD2;
        }

        @Override public short first() {
            return nextSetBit(words, 0);
        }

        @Override public short last() {
            return prevSetBit(words, (used() << WORD_SHIFT_BITS) - 1);
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

                this.bit = prevSetBit(words, (words.length << WORD_SHIFT_BITS) - 1);
            }

            /** {@inheritDoc} */
            @Override public boolean hasNext() {
                return bit != -1;
            }

            /** {@inheritDoc} */
            @Override public int next() {
                if (bit != -1) {
                    int ret = bit;

                    bit = prevSetBit(words, bit-1);

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

        @Override public int minSize() {
            return 0;
        }

        @Override public int maxSize() {
            return THRESHOLD;
        }

        @Override public short first() {
            return size() == 0 ? -1 : data[0]; // TODO FIXME size issue.
        }

        @Override public short last() {
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

        @Override public int minSize() {
            return THRESHOLD2;
        }

        @Override public int maxSize() {
            return SEGMENT_SIZE;
        }

        @Override public short first() {
            Iterator iter = iterator();

            if (!iter.hasNext())
                return -1;

            return (short) iter.next();
        }

        @Override public short last() {
            Iterator iter = reverseIterator();

            if (!iter.hasNext())
                return -1;

            return (short) iter.next();
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
            private int skipVal = -1;

            /** */
            private int val;

            /**
             * @param segment Segment.
             * @param size Size.
             */
            FlippedArrayIterator(short[] segment, int size) {
                super(segment, size);

                if (super.hasNext())
                    skipVal = super.next();

                advance();
            }

            /** */
            private void advance() {
                while(skipVal == val && val < SEGMENT_SIZE) {
                    if (super.hasNext())
                        skipVal = super.next();

                    val++;
                }
            }

            /** {@inheritDoc} */
            @Override public boolean hasNext() {
                return val < SEGMENT_SIZE;
            }

            /** {@inheritDoc} */
            @Override public int next() {
                int ret = val++;

                advance();

                return ret;
            }
        }

        /** */
        private static class FlippedReverseArrayIterator extends ReverseArrayIterator {
            /** */
            private int skipVal = -1;

            /** */
            private int val;

            /**
             * @param segment Segment.
             */
            FlippedReverseArrayIterator(short[] segment, int size) {
                super(segment, size);

                val = SEGMENT_SIZE - 1;

                if (super.hasNext())
                    skipVal = super.next();

                advance();
            }

            /** */
            private void advance() {
                while(skipVal == val && val >= 0) {
                    if (super.hasNext())
                        skipVal = super.next();

                    val--;
                }
            }

            /** {@inheritDoc} */
            @Override public boolean hasNext() {
                return val >= 0;
            }

            /** {@inheritDoc} */
            @Override public int next() {
                int ret = val--;

                advance();

                return ret;
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