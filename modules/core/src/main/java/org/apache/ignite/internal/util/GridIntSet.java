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
 * TODO fixme overflows ?
 * TODO constant time size.
 */
public class GridIntSet implements Serializable {
    public static final GridIntSet EMPTY = new GridIntSet();

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

    /**
     * @param v V.
     */
    public boolean add(int v) {
        short segIdx = (short) (v >> SEGMENT_SHIFT_BITS);

        short segVal = (short) (v & (SEGMENT_SIZE - 1));

        Segment seg;

        try {
            if (indices.add(segIdx))
                segments.put(segIdx, (seg = new ArraySegment()));
            else
                seg = segments.get(segIdx);
        } catch (ConversionException e) {
            indices = e.segment;

            segments.put(segIdx, (seg = new ArraySegment()));
        }

        try {
            return seg.add(segVal);
        } catch (ConversionException e) {
            segments.put(segIdx, e.segment);
        }

        return true;
    }

    /**
     * @param v V.
     */
    public boolean remove(int v) {
        short segIdx = (short) (v >> SEGMENT_SHIFT_BITS);

        short segVal = (short) (v & (SEGMENT_SIZE - 1));

        Segment seg = segments.get(segIdx);

        if (seg == null)
            return false;

        try {
            boolean rmv = seg.remove(segVal);

            if (rmv && seg.size() == 0) {
                try {
                    indices.remove(segIdx);
                } catch (ConversionException e) {
                    indices = e.segment;
                }

                segments.remove(segIdx);
            }

            return rmv;
        } catch (ConversionException e) {
            segments.put(segIdx, e.segment);
        }

        return true;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean contains(int v) {
        short segIdx = (short) (v >> SEGMENT_SHIFT_BITS);

        short segVal = (short) (v & (SEGMENT_SIZE - 1));

        Segment segment = segments.get(segIdx);

        return segment != null && segment.contains(segVal);
    }

    public int first() {
        short idx = indices.first();

        if (idx == -1)
            return -1;

        return segments.get(idx).first() + idx * SEGMENT_SIZE;
    }

    public int last() {
        short idx = indices.last();

        if (idx == -1)
            return -1;

        return segments.get(idx).last() + idx * SEGMENT_SIZE;
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
            if (it == null || !it.hasNext()) {
                if (segIter.hasNext()) {
                    idx = (short) segIter.next();

                    Segment segment = segments.get((short) idx);

                    it = getIt(segment);
                } else
                    it = null;
            }
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

        @Override public void remove() {
            it.remove();
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

        public void remove();
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

        public boolean add(short val) throws ConversionException;

        public boolean remove(short base) throws ConversionException;

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

        @Override public boolean add(short val) throws ConversionException {
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

                throw new ConversionException(seg);
            }

            count++;

            words[(wordIdx)] |= mask;

            return true;
        }

        @Override public boolean remove(short val) throws ConversionException {
            int wordIdx = wordIndex(val);

            short wordBit = (short) (val - wordIdx * SHORT_BITS); // TODO FIXME

            int mask = 1 << wordBit;

            boolean exists = (words[(wordIdx)] & mask) == mask;

            if (!exists)
                return false;

            count--;

            words[wordIdx] &= ~mask;

            if (count < THRESHOLD)
                throw new ConversionException(convertToArraySet());

            return true;
        }

        /** */
        private Segment convertToInvertedArraySet() throws ConversionException {
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

        private Segment convertToArraySet() throws ConversionException {
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

        /** {@inheritDoc} */
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
            return new BitSetIterator();
        }

        @Override public Iterator reverseIterator() {
            return new ReverseBitSetIterator();
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

        /** */
        private class BitSetIterator implements Iterator {
            /** */
            private int next;

            /** */
            private int cur;

            /** */
            public BitSetIterator() {
                this.next = nextSetBit(words, 0);
            }

            /** {@inheritDoc} */
            @Override public boolean hasNext() {
                return next != -1;
            }

            /** {@inheritDoc} */
            @Override public int next() {
                cur = next;

                next = nextSetBit(words, next + 1);

                return cur;
            }

            /** {@inheritDoc} */
            @Override public void remove() {
                BitSetSegment.this.remove((short) cur);
            }
        }

        /** */
        private class ReverseBitSetIterator implements Iterator {
            /** */
            private int next;

            /** */
            private int cur;

            /** */
            public ReverseBitSetIterator() {
                this.next = prevSetBit(words, (words.length << WORD_SHIFT_BITS) - 1);
            }

            /** {@inheritDoc} */
            @Override public boolean hasNext() {
                return next != -1;
            }

            /** {@inheritDoc} */
            @Override public int next() {
                cur = next;

                next = prevSetBit(words, next - 1);

                return cur;
            }

            /** {@inheritDoc} */
            @Override public void remove() {
                BitSetSegment.this.remove((short) cur);
            }
        }
    }

    /** */
    public static class ArraySegment implements Segment {
        /** */
        private short[] data;

        /** */
        private short used;

        /** */
        public ArraySegment() {
            this(0);
        }

        /** */
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
        public boolean add(short val) throws ConversionException {
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

                throw new ConversionException(converted);
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
        public boolean remove(short base) throws ConversionException {
            assert base < SEGMENT_SIZE : base;

            int idx = Arrays.binarySearch(data, 0, used, base);

            return idx >= 0 && remove0(idx);
        }

        /** */
        private boolean remove0(int idx) throws ConversionException {
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
            return new ArrayIterator();
        }

        /** {@inheritDoc} */
        @Override public Iterator reverseIterator() {
            return new ReverseArrayIterator();
        }

        /** {@inheritDoc}
         * @param val*/
        private Segment convertToBitSetSegment(short val) throws ConversionException {
            Segment seg = new BitSetSegment(val);

            Iterator it = iterator();

            while(it.hasNext())
                seg.add((short) it.next());

            seg.add(val);

            return seg;
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

        /** */
        class ArrayIterator implements Iterator {
            /** Word index. */
            private int cur;

            /** {@inheritDoc} */
            @Override public boolean hasNext() {
                return cur < used;
            }

            /** {@inheritDoc} */
            @Override public int next() {
                return data[cur++];
            }

            /** {@inheritDoc} */
            @Override public void remove() {
                ArraySegment.this.remove0((short) (--cur));
            }
        }

        /** */
        class ReverseArrayIterator implements Iterator {
            /** Word index. */
            private int cur = used - 1;

            /** {@inheritDoc} */
            @Override public boolean hasNext() {
                return cur >= 0;
            }

            /** {@inheritDoc} */
            @Override public int next() {
                return data[cur--];
            }

            /** {@inheritDoc} */
            @Override public void remove() throws ConversionException {
                ArraySegment.this.remove0((short) (cur + 1));
            }
        }
    }

    /** */
    public static class FlippedArraySegment extends ArraySegment {
        /**
         * Default constructor.
         */
        public FlippedArraySegment() {
            this(0);
        }

        /**
         * @param size Size.
         */
        public FlippedArraySegment(int size) {
            super(size);
        }

        /** {@inheritDoc} */
        @Override public boolean add(short val) throws ConversionException {
            return super.remove(val);
        }

        /** {@inheritDoc} */
        @Override public boolean remove(short base) throws ConversionException {
            try {
                return super.add(base);
            } catch (ConversionException e) {
                e.segment.remove(base);

                throw e;
            }
        }

        /** {@inheritDoc} */
        @Override public boolean contains(short v) {
            return !super.contains(v);
        }

        /** {@inheritDoc} */
        @Override public int size() {
            return SEGMENT_SIZE - super.size();
        }

        /** {@inheritDoc} */
        @Override public int minSize() {
            return THRESHOLD2;
        }

        /** {@inheritDoc} */
        @Override public int maxSize() {
            return SEGMENT_SIZE;
        }

        /** {@inheritDoc} */
        @Override public short first() {
            Iterator iter = iterator();

            if (!iter.hasNext())
                return -1;

            return (short) iter.next();
        }

        /** {@inheritDoc} */
        @Override public short last() {
            Iterator it = reverseIterator();

            return (short) (it.hasNext() ? it.next(): -1);
        }

        /** {@inheritDoc} */
        @Override public Iterator iterator() {
            return new FlippedArrayIterator();
        }

        /** {@inheritDoc} */
        @Override public Iterator reverseIterator() {
            return new FlippedReverseArrayIterator();
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

        /** */
        private class FlippedArrayIterator extends ArrayIterator {
            /** */
            private int skipVal = -1;

            /** */
            private int next = -1;

            /** */
            private int cur;

            /** */
            FlippedArrayIterator() {
                advance();
            }

            /** */
            private void advance() {
                while(skipVal == next && next < SEGMENT_SIZE) {
                    if (super.hasNext())
                        skipVal = super.next();

                    next++;
                }
            }

            /** {@inheritDoc} */
            @Override public boolean hasNext() {
                return next < SEGMENT_SIZE;
            }

            /** {@inheritDoc} */
            @Override public int next() {
                cur = next++;

                advance();

                return cur;
            }

            /** {@inheritDoc} */
            @Override public void remove() {
                FlippedArraySegment.this.remove((short) cur);
            }
        }

        /** */
        private class FlippedReverseArrayIterator extends ReverseArrayIterator {
            /** */
            private int skipVal = SEGMENT_SIZE;

            /** */
            private int next = SEGMENT_SIZE;

            /** */
            private int cur;

            /** */
            FlippedReverseArrayIterator() {
                advance();
            }

            /** */
            private void advance() {
                while(skipVal == next && next >= 0) {
                    if (super.hasNext())
                        skipVal = super.next();

                    next--;
                }
            }

            /** {@inheritDoc} */
            @Override public boolean hasNext() {
                return next >= 0;
            }

            /** {@inheritDoc} */
            @Override public int next() {
                cur = next--;

                advance();

                return cur;
            }

            /** {@inheritDoc} */
            @Override public void remove() {
                FlippedArraySegment.this.remove((short) cur);
            }
        }
    }

    /** */
    public static class ConversionException extends RuntimeException {
        private Segment segment;

        public ConversionException(Segment segment) {
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