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

package org.apache.ignite.internal.util.intset;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.ignite.lang.IgniteInClosure;

/**
 * Holds set of integers. <p> Structure: <p> Each segment stores SEGMENT_SIZE values, possibly in compressed format. <p>
 * Used storage format depends on segmen's fill factor. <p> Note: implementation is not thread safe. <p> TODO
 * equals/hashcode TODO FIXME cache bit masks like 1 << shift ? TODO HashSegment worth it? TODO replace power of two
 * arythmetics with bit ops. TODO fixme overflows ? TODO optimization for increasing values (no sorting needed, need
 * store max value)
 */
public class GridIntSet implements Externalizable {
    /** */
    private static final long serialVersionUID = 0L;

    /** */
    private final Thresholds thresholds;

    /** */
    private final int segmentShift;

    /** */
    private Segment indices;

    /** */
    private Map<Short, Segment> segments = new HashMap<>();

    /** */
    private int size;

    /** Caches segment index for last update operation. */
    private transient short segIdx;

    public GridIntSet(int segmentSize) {
        // TODO assert power of two for segmentSize.
        this.thresholds = new Thresholds(segmentSize);

        this.indices = new ArraySegment(thresholds, 4);

        this.segmentShift = Integer.numberOfTrailingZeros(segmentSize);
    }

    /** Indices updater on conversion happened. */
    private transient final IgniteInClosure<Segment> c1 = new IgniteInClosure<Segment>() {
        @Override public void apply(Segment segment) {
            indices = segment;
        }
    };

    /** Segment updater on conversion happened. */
    private final IgniteInClosure<Segment> c2 = new IgniteInClosure<Segment>() {
        @Override public void apply(Segment segment) {
            segments.put(segIdx, segment);
        }
    };

    /**
     * @param v V.
     */
    public boolean add(int v) {
        segIdx = index(v);

        short segVal = val(v);

        Segment seg;

        if (indices.add(segIdx, c1))
            segments.put(segIdx, (seg = new ArraySegment(thresholds)));
        else
            seg = segments.get(segIdx);

        boolean added = seg.add(segVal, c2);

        if (added)
            size++;

        return added;
    }

    /**
     * @param v V.
     */
    public boolean remove(int v) {
        segIdx = index(v);

        short segVal = val(v);

        Segment seg = segments.get(segIdx);

        if (seg == null)
            return false;

        boolean rmv = seg.remove(segVal, c2);

        if (rmv) {
            size--;

            if (seg.cardinality() == 0) {
                indices.remove(segIdx, c1);

                segments.remove(segIdx);
            }
        }

        return rmv;
    }

    public boolean isEmpty() {
        return cardinality() == 0;
    }

    public boolean contains(int v) {
        short segIdx = index(v);

        short segVal = val(v);

        Segment segment = segments.get(segIdx);

        return segment != null && segment.contains(segVal);
    }

    public int first() {
        short idx = indices.first();

        if (idx == -1)
            return -1;

        return segments.get(idx).first() + idx * thresholds.segmentSize; // TODO FIXME use bit ops.
    }

    public int last() {
        short idx = indices.last();

        if (idx == -1)
            return -1;

        return segments.get(idx).last() + idx * thresholds.segmentSize;
    }

    private abstract class IteratorImpl implements Iterator {

        /** Segment index. */
        private short idx;

        /** Segment index iterator. */
        private InternalIterator idxIter;

        /** Current segment value iterator. */
        private InternalIterator it;

        /** Current segment. */
        private Segment seg;

        /** Current value. */
        private short cur;

        /** */
        private final IgniteInClosure<Segment> c1;

        /** */
        private final  IgniteInClosure<Segment> c2;

        public IteratorImpl() {
            this.idxIter = iter(indices);

            this.c1 = new IgniteInClosure<Segment>() {
                @Override public void apply(Segment segment) {
                    segments.put(idx, segment);

                    // Segment was changed, fetch new iterator and reposition it.
                    it = iter(seg = segment);

                    it.skipTo(cur);

                    advance();
                }
            };

            c2 = new IgniteInClosure<Segment>() {
                @Override public void apply(Segment segment) {
                    indices = segment;

                    // Re-crete iterator and move it to right position.
                    idxIter = iter(indices);

                    idxIter.skipTo(idx);
                }
            };
        }

        /** */
        private void advance() {
            if (it == null || !it.hasNext())
                if (idxIter.hasNext()) {
                    idx = (short)idxIter.next();

                    seg = segments.get(idx);

                    it = iter(seg);
                }
                else
                    it = null;
        }

        protected abstract InternalIterator iter(Segment segment);

        @Override public boolean hasNext() {
            return idxIter.hasNext() || (it != null && it.hasNext());
        }

        @Override public int next() {
            advance();

            cur = (short)it.next();

            return cur + idx * thresholds.segmentSize;
        }

        /** {@inheritDoc} */
        @Override public void remove() {
            it.remove(c1);

            if (seg.cardinality() == 0) {
                idxIter.remove(c2);

                segments.remove(idx);
            }

            size--;
        }

        /** {@inheritDoc} */
        @Override public void skipTo(int v) {
            short segIdx = index(v);

            short segVal = val(v);

            if (segIdx == idx && it != null) {
                it.skipTo(segVal);

                advance();

                return;
            }

            idxIter.skipTo(segIdx);

            it = null;

            advance(); // Force iterator refresh.

            it.skipTo(segVal);

            advance(); // Switch to next segment if needed.
        }
    }

    /** */
    public Iterator iterator() {
        return new IteratorImpl() {
            @Override protected InternalIterator iter(Segment segment) {
                return segment.iterator();
            }
        };
    }

    /** */
    public Iterator reverseIterator() {
        return new IteratorImpl() {
            @Override protected InternalIterator iter(Segment segment) {
                return segment.reverseIterator();
            }
        };
    }

    public final int cardinality() {
        return size;
    }

    public static interface Iterator {
        public boolean hasNext();

        public int next();

        public void remove();

        // Next call must be hasNext to ensure value is present.
        public void skipTo(int val);
    }

    static interface InternalIterator {
        public boolean hasNext();

        public int next();

        public void remove(IgniteInClosure<Segment> clo);

        // Next call must be hasNext to ensure value is present.
        public void skipTo(int val);
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        StringBuilder b = new StringBuilder("[");

        Iterator it = iterator();

        while (it.hasNext()) {
            b.append(it.next());

            if (it.hasNext())
                b.append(", ");
        }

        b.append("]");

        return b.toString();
    }

    /** */
    interface Segment extends Externalizable {
        public boolean add(short val, IgniteInClosure<Segment> convertClo);

        public boolean remove(short base, IgniteInClosure<Segment> convertClo);

        public boolean contains(short v);

        public int cardinality();

        public short first();

        public short last();

        public InternalIterator iterator();

        public InternalIterator reverseIterator();
    }

    /**
     * TODO store used counter.
     */
    static class BitSetSegment implements Segment {
        /** */
        private static final int LONG_BITS = Long.SIZE;

        /** */
        private static final int WORD_SHIFT_BITS = 6;

        /** */
        private static final long WORD_MASK = 0xffffffffffffffffL;

        private long[] words;

        private int count;

        private Thresholds thresholds;

        public BitSetSegment(Thresholds t, int maxVal) {
            thresholds = t;
            words = new long[wordIndex(maxVal)];
        }

        /** {@inheritDoc} */
        @Override public boolean add(short val, IgniteInClosure<Segment> convertClo) {
            int wordIdx = wordIndex(val);

            if (wordIdx >= words.length)
                words = Arrays.copyOf(words, Math.max(words.length << 1, wordIdx + 1));

            int wordBit = val - (wordIdx << WORD_SHIFT_BITS); // TODO use bit op to compute remainder

            assert 0 <= wordBit && wordBit < LONG_BITS : "Word bit is within range";

            long mask = 1L << wordBit;

            boolean isSet = (words[(wordIdx)] & mask) != 0;

            if (isSet)
                return false;

            if (count == thresholds.threshold2) { // convert to inverted array set.
                assert convertClo != null : "Unexpected conversion has occurred";

                Segment seg = convertToInvertedArraySet();

                seg.add(val, null);

                convertClo.apply(seg);

                return true;
            }

            count++;

            words[(wordIdx)] |= mask;

            return true;
        }

        /** {@inheritDoc} */
        @Override public boolean remove(short val, IgniteInClosure<Segment> convertClo) {
            int wordIdx = wordIndex(val);

            int wordBit = val - (wordIdx << WORD_SHIFT_BITS); // TODO FIXME

            long mask = 1L << wordBit;

            boolean exists = (words[(wordIdx)] & mask) != 0;

            if (!exists)
                return false;

            count--;

            words[wordIdx] &= ~mask;

            if (count == thresholds.threshold1 - 1)
                convertClo.apply(convertToArraySet());

            return true;
        }

        /** */
        private Segment convertToInvertedArraySet() throws ConversionException {
            FlippedArraySegment seg = new FlippedArraySegment(thresholds, thresholds.threshold1);

            InternalIterator it = iterator();

            int i = 0;

            while (it.hasNext()) {
                int id = it.next();

                for (; i < id; i++)
                    seg.remove((short)i, null);

                i = id + 1;
            }

            while (i < thresholds.segmentSize)
                seg.remove((short)i++, null);

            return seg;
        }

        /** */
        private Segment convertToArraySet() throws ConversionException {
            ArraySegment seg = new ArraySegment(thresholds, thresholds.threshold1);

            InternalIterator it = iterator();

            while (it.hasNext()) {
                int id = it.next();

                seg.add((short)id, null);
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
        private static short nextSetBit(long[] words, int fromIdx) {
            if (fromIdx < 0)
                return -1;

            int wordIdx = wordIndex(fromIdx);

            if (wordIdx >= words.length)
                return -1;

            int shift = fromIdx & (Long.SIZE - 1);

            long word = words[wordIdx] & (WORD_MASK << shift);

            while (true) {
                if (word != 0)
                    return (short)((wordIdx * Long.SIZE) + Long.numberOfTrailingZeros(word));

                if (++wordIdx == words.length)
                    return -1;

                word = words[wordIdx];
            }
        }

        /** */
        private static short prevSetBit(long[] words, int fromIdx) {
            if (fromIdx < 0)
                return -1;

            int wordIdx = wordIndex(fromIdx);

            if (wordIdx >= words.length)
                return -1;

            long word = words[wordIdx] & (WORD_MASK >>> -(fromIdx + 1));

            while (true) {
                if (word != 0)
                    return (short)((wordIdx + 1) * Long.SIZE - 1 - Long.numberOfLeadingZeros(word));

                if (wordIdx-- == 0)
                    return -1;

                word = words[wordIdx];
            }
        }

        /** {@inheritDoc} */
        @Override public boolean contains(short val) {
            int wordIdx = wordIndex(val);

            if (wordIdx >= words.length)
                return false;

            int wordBit = val - (wordIdx << WORD_SHIFT_BITS);

            long mask = 1L << wordBit;

            return (words[(wordIdx)] & mask) != 0;
        }

        @Override public int cardinality() {
            return count;
        }

        @Override public short first() {
            return nextSetBit(words, 0);
        }

        @Override public short last() {
            return prevSetBit(words, (words.length << WORD_SHIFT_BITS) - 1);
        }

        @Override public InternalIterator iterator() {
            return new BitSetIterator();
        }

        @Override public InternalIterator reverseIterator() {
            return new ReverseBitSetIterator();
        }

        /** {@inheritDoc} */
        @Override public String toString() {
            StringBuilder b = new StringBuilder("[");

            InternalIterator it = iterator();

            while (it.hasNext()) {
                b.append(it.next());

                if (it.hasNext())
                    b.append(", ");
            }

            b.append("]");

            return b.toString();
        }

        /** */
        private class BitSetIterator implements InternalIterator {
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

            @Override public void remove(IgniteInClosure<Segment> clo) {
                BitSetSegment.this.remove((short)cur, clo);
            }

            /** {@inheritDoc} */
            @Override public void skipTo(int val) {
                next = nextSetBit(words, val);
            }
        }

        /** */
        private class ReverseBitSetIterator implements InternalIterator {
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
            @Override public void remove(IgniteInClosure<Segment> clo) {
                BitSetSegment.this.remove((short)cur, clo);
            }

            /** {@inheritDoc} */
            @Override public void skipTo(int val) {
                next = prevSetBit(words, val);
            }
        }

        /** {@inheritDoc} */
        @Override public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            BitSetSegment that = (BitSetSegment)o;

            return Arrays.equals(words, that.words);
        }

        /** {@inheritDoc} */
        @Override public int hashCode() {
            return Arrays.hashCode(words);
        }

        /** {@inheritDoc} */
        @Override public void writeExternal(ObjectOutput out) throws IOException {
            // No-op.
        }

        /** {@inheritDoc} */
        @Override public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            // No-op.
        }
    }

    /**
     * Sorted list of shorts.
     */
    static class ArraySegment implements Segment {
        /** */
        protected final Thresholds thresholds;

        /** */
        private short[] data;

        /** */
        private short used;

        public ArraySegment(Thresholds thresholds) {
            this(thresholds, 4);
        }

        /** */
        public ArraySegment(Thresholds thresholds, int size) {
            this.thresholds = thresholds;
            this.data = new short[size];
        }

        /** */
        public boolean add(short val, IgniteInClosure<Segment> convertClo) throws ConversionException {
            assert 0 <= used;

            int idx = used == 0 ? -1 : Arrays.binarySearch(data, 0, used, val);

            if (idx >= 0)
                return false; // Already exists.

            if (used == thresholds.threshold1) { // Convert to bit set on reaching threshold.
                convertClo.apply(convertToBitSetSegment(val));

                return true;
            }

            int pos = -(idx + 1);

            // Insert a segment.

            if (used == data.length)
                data = Arrays.copyOf(data, Math.min(data.length << 1, thresholds.threshold1));

            System.arraycopy(data, pos, data, pos + 1, used - pos);

            data[pos] = val;

            used++;

            return true;
        }

        /** {@inheritDoc} */
        public boolean remove(short base, IgniteInClosure<Segment> convertClo) throws ConversionException {
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

//            if (used == (data.length >> 1))
//                data = Arrays.copyOf(data, used);

            return true;
        }

        /** {@inheritDoc} */
        public boolean contains(short v) {
            return Arrays.binarySearch(data, 0, used, v) >= 0;
        }

        @Override public int cardinality() {
            return used;
        }

        @Override public short first() {
            return cardinality() == 0 ? -1 : data[0];
        }

        @Override public short last() {
            return cardinality() == 0 ? -1 : data[used - 1];
        }

        /** {@inheritDoc} */
        public InternalIterator iterator() {
            return new ArrayIterator();
        }

        /** {@inheritDoc} */
        @Override public InternalIterator reverseIterator() {
            return new ReverseArrayIterator();
        }

        /**
         * {@inheritDoc}
         *
         * @param val
         */
        private Segment convertToBitSetSegment(short val) throws ConversionException {
            Segment seg = new BitSetSegment(thresholds, Math.max(last(), val));

            InternalIterator it = iterator();

            while (it.hasNext())
                seg.add((short)it.next(), null);

            // Different behavior depending on segment type.
            if (this.getClass() == ArraySegment.class)
                seg.add(val, null);
            else
                seg.remove(val, null);

            return seg;
        }

        /** {@inheritDoc} */
        @Override public String toString() {
            StringBuilder b = new StringBuilder("[");

            InternalIterator it = iterator();

            while (it.hasNext()) {
                b.append(it.next());

                if (it.hasNext())
                    b.append(", ");
            }

            b.append("]");

            return b.toString();
        }

        /** */
        class ArrayIterator implements InternalIterator {
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
            @Override public void remove(IgniteInClosure<Segment> clo) {
                ArraySegment.this.remove0((short)(--cur));
            }

            @Override public void skipTo(int val) {
                int idx = Arrays.binarySearch(data, 0, used, (short)val);

                cur = idx >= 0 ? idx : -(idx + 1);
            }
        }

        /** */
        class ReverseArrayIterator implements InternalIterator {
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
            @Override public void remove(IgniteInClosure<Segment> clo) throws ConversionException {
                ArraySegment.this.remove0((short)(cur + 1));
            }

            /** {@inheritDoc} */
            @Override public void skipTo(int val) {
                int idx = Arrays.binarySearch(data, 0, used, (short)val);

                cur = idx >= 0 ? idx : -(idx + 1) - 1;
            }
        }

        /** {@inheritDoc} */
        @Override public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            ArraySegment that = (ArraySegment)o;

            if (used != that.used)
                return false;

            for (int i = 0; i < used; i++)
                if (data[i] != that.data[i])
                    return false;

            return true;
        }

        /** {@inheritDoc} */
        @Override public int hashCode() {
            int res = 1;

            for (int i = 0; i < used; i++)
                res = 31 * res + data[i];

            return res;
        }

        /** {@inheritDoc} */
        @Override public void writeExternal(ObjectOutput out) throws IOException {
            // No-op.
        }

        /** {@inheritDoc} */
        @Override public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            // No-op.
        }
    }

    /**
     * By default contains  all values from segment range.
     */
    static class FlippedArraySegment extends ArraySegment {
        /**
         * Default constructor.
         */
        public FlippedArraySegment(Thresholds thresholds, int size) {
            super(thresholds, size);
        }

        /** {@inheritDoc} */
        @Override public boolean add(short val, IgniteInClosure<Segment> conversionClo) throws ConversionException {
            return super.remove(val, null);
        }

        /** {@inheritDoc} */
        @Override public boolean remove(final short val,
            final IgniteInClosure<Segment> conversionClo) throws ConversionException {
            return super.add(val, conversionClo);
        }

        /** {@inheritDoc} */
        @Override public boolean contains(short v) {
            return !super.contains(v);
        }

        /** {@inheritDoc} */
        @Override public int cardinality() {
            return thresholds.segmentSize - super.cardinality();
        }

        /** {@inheritDoc} */
        @Override public short first() {
            InternalIterator iter = iterator();

            if (!iter.hasNext())
                return -1;

            return (short)iter.next();
        }

        /** {@inheritDoc} */
        @Override public short last() {
            InternalIterator it = reverseIterator();

            return (short)(it.hasNext() ? it.next() : -1);
        }

        /** {@inheritDoc} */
        @Override public InternalIterator iterator() {
            return new FlippedArrayIterator();
        }

        /** {@inheritDoc} */
        @Override public InternalIterator reverseIterator() {
            return new FlippedReverseArrayIterator();
        }

        /** {@inheritDoc} */
        @Override public String toString() {
            StringBuilder b = new StringBuilder("[");

            InternalIterator it = iterator();

            while (it.hasNext()) {
                b.append(it.next());

                if (it.hasNext())
                    b.append(", ");
            }

            b.append("]");

            return b.toString();
        }

        /** */
        private class FlippedArrayIterator extends ArrayIterator {
            /** */
            private int skipVal = -1;

            /** */
            private int next = 0;

            /** */
            private int cur;

            /** */
            FlippedArrayIterator() {
                advance();
            }

            /** */
            private void advance() {
                if (skipVal == -1)
                    if (super.hasNext())
                        skipVal = super.next();

                while (skipVal == next && next < thresholds.segmentSize) {
                    if (super.hasNext())
                        skipVal = super.next();

                    next++;
                }
            }

            /** {@inheritDoc} */
            @Override public boolean hasNext() {
                return next < thresholds.segmentSize;
            }

            /** {@inheritDoc} */
            @Override public int next() {
                cur = next++;

                advance();

                return cur;
            }

            /** {@inheritDoc} */
            @Override public void remove(IgniteInClosure<Segment> clo) {
                FlippedArraySegment.this.remove((short)cur, clo);
            }

            /** {@inheritDoc} */
            @Override public void skipTo(int val) {
                assert val >= 0;

                next = val;

                advance();
            }
        }

        /** */
        private class FlippedReverseArrayIterator extends ReverseArrayIterator {
            /** */
            private int next = thresholds.segmentSize - 1;

            /** */
            private int cur;

            /** */
            FlippedReverseArrayIterator() {
                advance();
            }

            /** */
            private void advance() {
                while (super.hasNext() && super.next() == next && next-- >= 0)
                    ;
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
            @Override public void remove(IgniteInClosure<Segment> clo) {
                FlippedArraySegment.this.remove((short)cur, clo);
            }

            /** {@inheritDoc} */
            @Override public void skipTo(int val) {
                assert val >= 0;

                next = val;

                advance();
            }
        }
    }

    /** */
    static class ConversionException extends RuntimeException {
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

    /** {@inheritDoc} */
    @Override public void writeExternal(ObjectOutput out) throws IOException {
    }

    /** {@inheritDoc} */
    @Override public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        GridIntSet that = (GridIntSet)o;

        if (size != that.size)
            return false;

        if (!indices.equals(that.indices))
            return false;

        return segments.equals(that.segments);
    }

    @Override public int hashCode() {
        int res = indices.hashCode();

        res = 31 * res + segments.hashCode();

        res = 31 * res + size;

        return res;
    }

    public static class Thresholds {
        final int segmentSize;

        final int maxWords;

        final int threshold1;

        final int threshold2;

        public Thresholds(int segmentSize) {
            this.segmentSize = segmentSize;
            this.maxWords = segmentSize >> 4;
            this.threshold1 = maxWords;
            this.threshold2 = segmentSize - maxWords;
        }
    }

    private short index(int v) {
        return (short)(v >> segmentShift);
    }

    private short val(int v) {
        return (short)(v & (thresholds.segmentSize - 1));
    }

    public Thresholds thresholds() {
        return thresholds;
    }
}
