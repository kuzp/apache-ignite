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

import org.apache.ignite.internal.util.typedef.F;
import org.apache.ignite.lang.IgniteOutClosure;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.jsr166.ThreadLocalRandom8;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Test for {@link GridIntSet}.
 */
public class GridIntSetSelfTest extends GridCommonAbstractTest {
    /** Max words. */
    private static final int MAX_WORDS = GridIntSet.SEGMENT_SIZE / Short.SIZE;

    private static final int MAX_VALUES = GridIntSet.SEGMENT_SIZE * GridIntSet.SEGMENT_SIZE;

    private GridRandom gridRandom = new GridRandom();

    /** {@inheritDoc} */
    @Override protected void beforeTest() throws Exception {
        super.beforeTest();

        long seed = ThreadLocalRandom8.current().nextLong();

        gridRandom.setSeed(seed);

        log.info("Used seed: " + seed);
    }

    public void testRemoveAddRemoveRndArraySegment() {
        GridIntSet.ArraySegment seg = new GridIntSet.ArraySegment();

        int cnt = seg.maxSize() - seg.minSize();

        testRemoveAddRemoveRnd0(rndFill(new TestIntSetSegImpl(seg), cnt, GridIntSet.SEGMENT_SIZE), cnt);
    }

    public void testRemoveAddRemoveRndIntSet() {
        int cnt = MAX_VALUES / 10;

        // 7934495536614167519
        gridRandom.setSeed(7934495536614167519L);

        testRemoveAddRemoveRnd0(rndFill(new TestIntSetImpl(), cnt, MAX_VALUES), cnt);
    }

    /**
     * Tests array segment.
     */
//    public void testArraySegment() throws GridIntSet.ConversionException {
//
//
//        testRemoveFirst0(rndFill(new GridIntSet.ArraySegment(), false));
//
//        testRemoveFirstIter0(rndFill(new GridIntSet.ArraySegment(), false));
//
//        testRemoveLast0(rndFill(new GridIntSet.ArraySegment(), false));
//
//        testRemoveLastIter0(rndFill(new GridIntSet.ArraySegment(), false));
//
//        final GridIntSet.Segment seg = rndFill(new GridIntSet.ArraySegment(), true);
//
//        testIterators0(new IgniteOutClosure<GridIntSet.Iterator>() {
//            @Override public GridIntSet.Iterator apply() {
//                return seg.iterator();
//            }
//        }, new IgniteOutClosure<GridIntSet.Iterator>() {
//            @Override public GridIntSet.Iterator apply() {
//                return seg.reverseIterator();
//            }
//        });
//    }
//
//    /**
//     * Tests flipped array segment.
//     */
//    public void testFlippedArraySegment() throws GridIntSet.ConversionException {
//        testRemoveAddRemoveRnd0(new GridIntSet.FlippedArraySegment()); // FlippedArraySegment contains everything by default.
//
//        testRemoveFirst0(new GridIntSet.FlippedArraySegment());
//
//        testRemoveFirstIter0(new GridIntSet.FlippedArraySegment());
//
//        testRemoveLast0(new GridIntSet.FlippedArraySegment());
//
//        testRemoveLastIter0(new GridIntSet.FlippedArraySegment());
//
//        final GridIntSet.Segment seg = rndRmv(new GridIntSet.FlippedArraySegment(), true);
//
//        testIterators0(new IgniteOutClosure<GridIntSet.Iterator>() {
//            @Override
//            public GridIntSet.Iterator apply() {
//                return seg.iterator();
//            }
//        }, new IgniteOutClosure<GridIntSet.Iterator>() {
//            @Override
//            public GridIntSet.Iterator apply() {
//                return seg.reverseIterator();
//            }
//        });
//    }
//
//    /**
//     * Tests array segment.
//     */
//    public void testBitSetSegment() throws GridIntSet.ConversionException {
//        testRemoveAddRemoveRnd0(rndFill(new GridIntSet.BitSetSegment(), false));
//
//        testRemoveFirst0(rndFill(new GridIntSet.BitSetSegment(), false));
//
//        testRemoveFirstIter0(rndFill(new GridIntSet.BitSetSegment(), false));
//
//        testRemoveLast0(rndFill(new GridIntSet.BitSetSegment(), false));
//
//        testRemoveLastIter0(rndFill(new GridIntSet.BitSetSegment(), false));
//
//        final GridIntSet.Segment seg = rndFill(new GridIntSet.BitSetSegment(), true);
//
//        testIterators0(new IgniteOutClosure<GridIntSet.Iterator>() {
//            @Override
//            public GridIntSet.Iterator apply() {
//                return seg.iterator();
//            }
//        }, new IgniteOutClosure<GridIntSet.Iterator>() {
//            @Override
//            public GridIntSet.Iterator apply() {
//                return seg.reverseIterator();
//            }
//        });
//    }

    /**
     *
     */
    public void testSet() {
        GridIntSet set = new GridIntSet();

        assertEquals(-1, set.first());

        assertFalse(set.iterator().hasNext());

        assertFalse(set.reverseIterator().hasNext());

        int size = 1024;

        for (int i = 0; i < size; i++)
            assertTrue(set.add(i * size));

        assertEquals("Size", size, set.size());

        List<Integer> vals = toList(set.iterator());

        List<Integer> vals2 = toList(set.reverseIterator());

        Collections.reverse(vals2);

        assertEqualsCollections(vals, vals2);

        assertEquals("First", 0, set.first());

        assertEquals("Last", (size - 1) * size, set.last());

        for (int i = 0; i < size; i++)
            assertTrue(set.contains(i * size));

        for (int i = 0; i < size; i++)
            assertTrue(set.remove(i * size));

        assertEquals("Size", 0, set.size());
    }

    /**
     * Tests set iterators.
     */
    public void testSetIterators() {
        final TestIntSetImpl set = new TestIntSetImpl();

        rndFill(set, gridRandom.nextInt(100_000), MAX_VALUES);

        testIterators0(new IgniteOutClosure<GridIntSet.Iterator>() {
            @Override public GridIntSet.Iterator apply() {
                return set.iterator();
            }
        }, new IgniteOutClosure<GridIntSet.Iterator>() {
            @Override public GridIntSet.Iterator apply() {
                return set.reverseIterator();
            }
        });
    }

    /** */
    private List<Integer> toList(GridIntSet.Iterator it) {
        List<Integer> l = new ArrayList<>();

        while(it.hasNext())
            l.add(it.next());

        return l;
    }

    /**
     * Tests array set.
     */
    private void testRemoveAddRemoveRnd0(TestIntSet set, int maxSize) throws GridIntSet.ConversionException {
        int size = set.size();

        List<Integer> vals = new ArrayList<>();

        // Fetch values to list.
        GridIntSet.Iterator it = set.iterator();

        while(it.hasNext())
            vals.add(it.next());

        assertTrue(maxSize <= vals.size());

        // Double check containment.
        for (Integer val : vals)
            assertTrue(set.contains(val));

        // Select random sub set.
        int cnt = gridRandom.nextInt(maxSize);

        // Define random subset of set elements.
        List<Integer> rndVals = rndSubset(vals, cnt);

        List<Integer> sortedVals = new ArrayList<>(rndVals);

        Collections.sort(sortedVals);

        for (Integer val : rndVals) {
            validateSize(set);

            assertTrue(set.contains(val));

            assertTrue(set.remove(val));

            validateSize(set);
        }

        assertEquals("Size", size - cnt, set.size());

        assertContainsNothing(set, rndVals);

        for (Integer v : rndVals) {
            validateSize(set);

            boolean val = set.add(v);

            assertTrue("Added: " + v, val);

            validateSize(set);
        }

        assertContainsAll(set, rndVals);

        // Randomize removal order.
        Collections.shuffle(rndVals);

        assertEquals("After", size, set.size());

        for (Integer v : rndVals) {
            validateSize(set);

            boolean val = set.remove(v);

            assertTrue("Removed: " + v, val);

            validateSize(set);
        }

        assertContainsNothing(set, rndVals);

        assertEquals("Size", size - cnt, set.size());
    }

    /**
     * Tests removal from left size.
     */
    private void testRemoveFirst0(TestIntSet segment, int cnt) throws GridIntSet.ConversionException {
        int size = segment.size();

        int i = cnt;

        while(i-- > 0) {
            validateSize(segment);

            int val = segment.first();

            assertTrue(segment.remove(val));

            assertEquals(--size, segment.size());

            validateSize(segment);
        }

        assertEquals(size - cnt, segment.size());
    }

    /**
     * Tests removal from left size.
     */
    private void testRemoveFirstIter0(TestIntSet segment, int cnt) throws GridIntSet.ConversionException {
        int size = segment.size();

        GridIntSet.Iterator iter = segment.iterator();

        int i = cnt;

        while(iter.hasNext() && i-- > 0) {
            validateSize(segment);

            iter.next();

            iter.remove();

            assertEquals(--size, segment.size());

            validateSize(segment);
        }

        assertEquals(size - cnt, segment.size());
    }
    /**
     * Tests removal from right side.
     */
    private void testRemoveLast0(TestIntSet segment, int cnt) throws GridIntSet.ConversionException {
        int size = segment.size();

        int i = cnt;

        while(i-- > 0) {
            validateSize(segment);

            int val = segment.last();

            assertTrue(segment.remove(val));

            assertEquals(--size, segment.size());

            validateSize(segment);
        }

        assertEquals(size - cnt, segment.size());
    }

    /**
     * Tests removal from right side.
     */
    private void testRemoveLastIter0(TestIntSet segment, int cnt) throws GridIntSet.ConversionException {
        int size = segment.size();

        GridIntSet.Iterator iter = segment.reverseIterator();

        int i = cnt;

        while(iter.hasNext() && i-- > 0) {
            validateSize(segment);

            iter.next();

            iter.remove();

            assertEquals(--size, segment.size());

            validateSize(segment);
        }

        assertEquals(size - cnt, segment.size());
    }

    /** */
    private void testIterators0(IgniteOutClosure<GridIntSet.Iterator> fwdFactoryClo, IgniteOutClosure<GridIntSet.Iterator> revFactoryClo) {
        List<Integer> fwd = new ArrayList<>();

        GridIntSet.Iterator fwdIt = fwdFactoryClo.apply();

        while(fwdIt.hasNext())
            fwd.add(fwdIt.next());

        List<Integer> rev = new ArrayList<>();

        GridIntSet.Iterator revIt = revFactoryClo.apply();

        while(revIt.hasNext())
            rev.add(revIt.next());

        Collections.reverse(rev);

        assertEquals(rev.toString(), fwd, rev);
    }

    /**
     * @param set Segment.
     */
    private void validateSize(TestIntSet set) {
        if (set instanceof TestIntSetSegImpl) {
            TestIntSetSegImpl tmp = (TestIntSetSegImpl) set;

            GridIntSet.Segment segment = tmp.segment();

            assertTrue(String.valueOf(segment.size()), segment.minSize() <= segment.size() && segment.size() <= segment.maxSize());

            assertTrue(String.valueOf(segment.data().length), segment.data().length <= MAX_WORDS);
        }
    }

    /**
     * @param set Segment.
     * @param vals Values.
     */
    public void assertContainsNothing(TestIntSet set, List<Integer> vals) {
        for (Integer v : vals) {
            boolean val = set.contains(v.shortValue());

            assertFalse("Contains: " + v, val);
        }
    }

    /**
     * @param seg Segment.
     * @param vals Vals.
     */
    public void assertContainsAll(TestIntSet seg, List<Integer> vals) {
        for (Integer v : vals) {
            boolean val = seg.contains(v.shortValue());

            assertTrue("Contains: " + v, val);
        }
    }

    /**
     * @param vals Values.
     * @param len Length.
     */
    private List<Integer> rndSubset(List<Integer> vals, int len) {
        assert len <= vals.size();

        List<Integer> src = new ArrayList<>(len);

        while(src.size() != len) {
            int val = F.rand(vals);

            if (!src.contains(val))
                src.add(val);
        }

        return src;
    }

    private TestIntSet rndFill(TestIntSet set, int cnt, int max) {
        assert cnt <= max;

        boolean flip = cnt > max / 2;

        int tmpCnt = flip  ? max - cnt : cnt;

        SortedSet<Integer> buf = new TreeSet<>();

        while(buf.size() != tmpCnt) {
            int rnd = gridRandom.nextInt(max);

            buf.add(rnd);
        }

        if (flip) { // Flip.
            Iterator<Integer> it = buf.iterator();

            int i = 0;

            while (it.hasNext()) {
                int id = it.next();

                for (; i < id; i++)
                    set.add((short) i);

                i = id + 1;
            }

            while(i < cnt)
                set.add((short) i++);
        } else
            for (Integer val : buf)
                set.add(val);

        assertEquals(cnt, set.size());

        return set;
    }

    /**
     * Fills set with random values until full capacity.
     *
     * @param set Segment.
     */
    private TestIntSet rndRmv(TestIntSet set, int cnt, int max) {
        assert cnt <= max;

        boolean flip  = cnt > max / 2;

        int tmpCnt = flip  ? max - cnt : cnt;

        SortedSet<Integer> buf = new TreeSet<>();

        while(set.size() != tmpCnt) {
            int rnd = gridRandom.nextInt(max);

            buf.add(rnd);
        }

        if (flip) { // Flip.
            Iterator<Integer> it = buf.iterator();

            int i = 0;

            while (it.hasNext()) {
                int id = it.next();

                for (; i < id; i++)
                    set.remove((short) i);

                i = id + 1;
            }

            while(i < cnt)
                set.remove((short) i++);
        } else
            for (Integer val : buf)
                set.add(val);

        return set;
    }

    interface TestIntSet {
        int size();

        int first();

        boolean remove(int val);

        boolean contains(int val);

        boolean add(int val);

        GridIntSet.Iterator iterator();

        int last();

        GridIntSet.Iterator reverseIterator();
    }

    static class TestIntSetSegImpl implements TestIntSet {
        private GridIntSet.Segment seg;

        public TestIntSetSegImpl(GridIntSet.Segment seg) {
            this.seg = seg;
        }

        @Override public int size() {
            return seg.size();
        }

        @Override public int first() {
            return seg.first();
        }

        @Override public boolean remove(int val) {
            return seg.remove((short) val);
        }

        @Override public boolean contains(int val) {
            return seg.contains((short) val);
        }

        @Override public boolean add(int val) {
            return seg.add((short) val);
        }

        @Override public GridIntSet.Iterator iterator() {
            return seg.iterator();
        }

        @Override public int last() {
            return seg.last();
        }

        @Override public GridIntSet.Iterator reverseIterator() {
            return seg.reverseIterator();
        }

        GridIntSet.Segment segment() {
            return seg;
        }
    }

    static class TestIntSetImpl implements TestIntSet {
        private final GridIntSet set;

        public TestIntSetImpl() {
            set = new GridIntSet();
        }

        @Override
        public int size() {
            return set.size();
        }

        @Override
        public int first() {
            return set.first();
        }

        @Override
        public boolean remove(int val) {
            return set.remove(val);
        }

        @Override
        public boolean contains(int val) {
            return set.contains(val);
        }

        @Override
        public boolean add(int val) {
            return set.add(val);
        }

        @Override
        public GridIntSet.Iterator iterator() {
            return set.iterator();
        }

        @Override
        public int last() {
            return set.last();
        }

        @Override
        public GridIntSet.Iterator reverseIterator() {
            return set.reverseIterator();
        }
    }

    static class TestIntSetArrayListImpl implements TestIntSet {
        private final ArrayList<Integer> list;

        public TestIntSetArrayListImpl() {
            list = new ArrayList<>();
        }

        @Override public int size() {
            return list.size();
        }

        @Override public int first() {
            return list.get(0);
        }

        @Override public boolean remove(int val) {
            return list.remove((Integer) val);
        }

        @Override public boolean contains(int val) {
            return list.contains(val);
        }

        @Override public boolean add(int val) {
            return list.add(val);
        }

        @Override public int last() {
            return list.get(list.size() - 1);
        }

        @Override public GridIntSet.Iterator iterator() {
            final ListIterator<Integer> it = list.listIterator();

            return new GridIntSet.Iterator() {
                @Override public boolean hasNext() {
                    return it.hasNext();
                }

                @Override public int next() {
                    return it.next();
                }

                @Override public void remove() {
                    it.remove();
                }

                @Override public void skipTo(int val) {

                }
            };
        }

        @Override public GridIntSet.Iterator reverseIterator() {
            final ListIterator<Integer> it = list.listIterator(list.size());

            return new GridIntSet.Iterator() {
                @Override public boolean hasNext() {
                    return it.hasPrevious();
                }

                @Override public int next() {
                    return it.previous();
                }

                @Override public void remove() {
                    it.remove();
                }

                @Override public void skipTo(int val) {
                }
            };
        }
    }

    static class TestIntSetImpl3 implements TestIntSet {
        private final HashSet<Integer> list;

        public TestIntSetImpl3() {
            list = new HashSet<>();
        }

        @Override public int size() {
            return list.size();
        }

        @Override public int first() {
            return list.iterator().next();
        }

        @Override public boolean remove(int val) {
            return list.remove((Integer) val);
        }

        @Override public boolean contains(int val) {
            return list.contains(val);
        }

        @Override public boolean add(int val) {
            return list.add(val);
        }

        @Override public int last() {
            return list.iterator().next();
        }

        @Override public GridIntSet.Iterator iterator() {
            final Iterator<Integer> it = list.iterator();

            return new GridIntSet.Iterator() {
                @Override public boolean hasNext() {
                    return it.hasNext();
                }

                @Override public int next() {
                    return it.next();
                }

                @Override public void remove() {
                    it.remove();
                }

                @Override public void skipTo(int val) {
                }
            };
        }

        @Override public GridIntSet.Iterator reverseIterator() {
            return iterator();
        }
    }

}