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
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.lang.IgniteOutClosure;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.jsr166.ThreadLocalRandom8;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Test for {@link GridIntSet}.
 */
public class GridIntSetSelfTest extends GridCommonAbstractTest {
    /** Max words. */
    private static final int MAX_WORDS = GridIntSet.SEGMENT_SIZE / Short.SIZE;

    private static final int MAX_VALUES = GridIntSet.SEGMENT_SIZE * GridIntSet.SEGMENT_SIZE;

    private GridRandom gridRandom = new GridRandom();

    /**
     * Tests array segment.
     */
    public void testArraySegment() throws GridIntSet.ConversionException {
        testRemoveAddRemoveRnd0(rndFill(new GridIntSet.ArraySegment(), false));

        testRemoveFirst0(rndFill(new GridIntSet.ArraySegment(), false));

        testRemoveFirstIter0(rndFill(new GridIntSet.ArraySegment(), false));

        testRemoveLast0(rndFill(new GridIntSet.ArraySegment(), false));

        testRemoveLastIter0(rndFill(new GridIntSet.ArraySegment(), false));

        final GridIntSet.Segment seg = rndFill(new GridIntSet.ArraySegment(), true);

        testIterators0(new IgniteOutClosure<GridIntSet.Iterator>() {
            @Override public GridIntSet.Iterator apply() {
                return seg.iterator();
            }
        }, new IgniteOutClosure<GridIntSet.Iterator>() {
            @Override public GridIntSet.Iterator apply() {
                return seg.reverseIterator();
            }
        });
    }

    /**
     * Tests flipped array segment.
     */
    public void testFlippedArraySegment() throws GridIntSet.ConversionException {
        testRemoveAddRemoveRnd0(new GridIntSet.FlippedArraySegment()); // FlippedArraySegment contains everything by default.

        testRemoveFirst0(new GridIntSet.FlippedArraySegment());

        testRemoveFirstIter0(new GridIntSet.FlippedArraySegment());

        testRemoveLast0(new GridIntSet.FlippedArraySegment());

        testRemoveLastIter0(new GridIntSet.FlippedArraySegment());

        final GridIntSet.Segment seg = clear(new GridIntSet.FlippedArraySegment(), true);

        testIterators0(new IgniteOutClosure<GridIntSet.Iterator>() {
            @Override public GridIntSet.Iterator apply() {
                return seg.iterator();
            }
        }, new IgniteOutClosure<GridIntSet.Iterator>() {
            @Override public GridIntSet.Iterator apply() {
                return seg.reverseIterator();
            }
        });
    }

    /**
     * Tests array segment.
     */
    public void testBitSetSegment() throws GridIntSet.ConversionException {
        testRemoveAddRemoveRnd0(rndFill(new GridIntSet.BitSetSegment(), false));

        testRemoveFirst0(rndFill(new GridIntSet.BitSetSegment(), false));

        testRemoveFirstIter0(rndFill(new GridIntSet.BitSetSegment(), false));

        testRemoveLast0(rndFill(new GridIntSet.BitSetSegment(), false));

        testRemoveLastIter0(rndFill(new GridIntSet.BitSetSegment(), false));

        final GridIntSet.Segment seg = rndFill(new GridIntSet.BitSetSegment(), true);

        testIterators0(new IgniteOutClosure<GridIntSet.Iterator>() {
            @Override public GridIntSet.Iterator apply() {
                return seg.iterator();
            }
        }, new IgniteOutClosure<GridIntSet.Iterator>() {
            @Override public GridIntSet.Iterator apply() {
                return seg.reverseIterator();
            }
        });
    }

    /**
     * Tests array segment.
     */
    public void testSet2() throws GridIntSet.ConversionException {
        // TODO testRemoveAddRemoveRnd0(rndFill(new GridIntSet(), false));

        long t0 = System.nanoTime();
        long t1 = 0;

        int cnt = 1;

        for (int i = 0; i < cnt; i++) {
            //long seed = ThreadLocalRandom8.current().nextLong();

            long seed = -6087285172100122953L;

            gridRandom.setSeed(seed);

            if ( i == 100)
                t1 = System.nanoTime();

            log().info("Seed: " + seed);

            testRemoveFirst0(rndFill2(new TestIntSetImpl(), false));

            testRemoveFirstIter0(rndFill2(new TestIntSetImpl(), false));

//            testRemoveLast0(rndFill2(new TestIntSetImpl(), false));
//
//            testRemoveLastIter0(rndFill2(new TestIntSetImpl(), false));
        }

        System.out.println("Time0: " + (System.nanoTime() - t0)/1000/1000.);
        System.out.println("Time: " + (System.nanoTime() - t1)/1000/1000.);

//        final TestIntSet set = new TestIntSetImpl();
//
//        rndFill2(new TestIntSetImpl(), true);
//
//        testIterators0(new IgniteOutClosure<GridIntSet.Iterator>() {
//            @Override public GridIntSet.Iterator apply() {
//                return set.iterator();
//            }
//        }, new IgniteOutClosure<GridIntSet.Iterator>() {
//            @Override public GridIntSet.Iterator apply() {
//                return set.reverseIterator();
//            }
//        });
    }

    public void testSet3() throws GridIntSet.ConversionException {
        // TODO testRemoveAddRemoveRnd0(rndFill(new GridIntSet(), false));

        long t0 = System.nanoTime();
        long t1 = 0;

        for (int i = 0; i < 1000; i++) {
            if (i < 100)
                continue;
            else
                t1 = System.nanoTime();

            testRemoveFirst0(rndFill2(new TestIntSetImpl2(), false));

            testRemoveFirstIter0(rndFill2(new TestIntSetImpl2(), false));

            testRemoveLast0(rndFill2(new TestIntSetImpl2(), false));

            testRemoveLastIter0(rndFill2(new TestIntSetImpl2(), false));
        }

        System.out.println("Time0: " + (System.nanoTime() - t0) / 1000 / 1000.);
        System.out.println("Time: " + (System.nanoTime() - t1) / 1000 / 1000.);
    }

    public void testSet4() throws GridIntSet.ConversionException {
        // TODO testRemoveAddRemoveRnd0(rndFill(new GridIntSet(), false));

        long t0 = System.nanoTime();
        long t1 = 0;

        for (int i = 0; i < 1000; i++) {
            if (i < 100)
                continue;
            else
                t1 = System.nanoTime();

            testRemoveFirst0(rndFill2(new TestIntSetImpl3(), false));

            testRemoveFirstIter0(rndFill2(new TestIntSetImpl3(), false));

            testRemoveLast0(rndFill2(new TestIntSetImpl3(), false));

            testRemoveLastIter0(rndFill2(new TestIntSetImpl3(), false));
        }

        System.out.println("Time0: " + (System.nanoTime() - t0) / 1000 / 1000.);
        System.out.println("Time: " + (System.nanoTime() - t1) / 1000 / 1000.);
    }

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
        final GridIntSet set = new GridIntSet();

        rndFill(set, true);

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
     * Tests array segment.
     */
    private void testRemoveAddRemoveRnd0(GridIntSet.Segment segment) throws GridIntSet.ConversionException {
        int size = segment.size();

        List<Integer> vals = new ArrayList<>();

        // Fetch values to list.
        GridIntSet.Iterator it = segment.iterator();

        while(it.hasNext())
            vals.add(it.next());

        // Check segment data size.
        int maxSize = segment.maxSize() - segment.minSize();

        assertTrue(maxSize <= vals.size());

        // Double check containment.
        for (Integer val : vals)
            assertTrue(segment.contains(val.shortValue()));

        // Select random sub set.
        int rndCnt = ThreadLocalRandom8.current().nextInt(0, maxSize);

        List<Integer> rndVals = rndValues(vals, rndCnt);

        List<Integer> sortedVals = new ArrayList<>(rndVals);

        Collections.sort(sortedVals);

        log().info("Data: " + rndVals);

        log().info("Sorted: " + sortedVals);

        for (Integer val : rndVals) {
            validateSize(segment);

            assertTrue(segment.contains(val.shortValue()));

            assertTrue(segment.remove(val.shortValue()));

            validateSize(segment);
        }

        assertEquals("Size", size - rndCnt, segment.size());

        assertContainsNothing(segment, rndVals);

        for (Integer v : rndVals) {
            validateSize(segment);

            boolean val = segment.add(v.shortValue());

            assertTrue("Added: " + v, val);

            validateSize(segment);
        }

        assertContainsAll(segment, rndVals);

        // Randomize removal order.
        Collections.shuffle(rndVals);

        assertEquals("After", size, segment.size());

        for (Integer v : rndVals) {
            validateSize(segment);

            boolean val = segment.remove(v.shortValue());

            assertTrue("Removed: " + v, val);

            validateSize(segment);
        }

        assertContainsNothing(segment, rndVals);

        assertEquals("Size", size - rndCnt, segment.size());
    }

    /**
     * Tests removal from left size.
     */
    private void testRemoveFirst0(GridIntSet.Segment segment) throws GridIntSet.ConversionException {
        log().info("testRemoveFirst0");

        int size = segment.size();

        while(size != segment.minSize()) {
            validateSize(segment);

            short val = segment.first();

            assertTrue(segment.remove(val));

            assertEquals(--size, segment.size());

            validateSize(segment);
        }
    }

    /**
     * Tests removal from left size.
     */
    private void testRemoveFirst0(TestIntSet set) throws GridIntSet.ConversionException {
        log().info("testRemoveFirst0");

        int size = set.size();

        while(size != 0) {
            int val = set.first();

            assertTrue(set.remove(val));

            --size;

            assertEquals(size, set.size());
        }
    }

    /**
     * Tests removal from left size.
     */
    private void testRemoveFirstIter0(GridIntSet.Segment segment) throws GridIntSet.ConversionException {
        log().info("testRemoveFirstIter0");

        int size = segment.size();

        GridIntSet.Iterator iter = segment.iterator();

        while(iter.hasNext() && size != segment.minSize()) {
            validateSize(segment);

            iter.next();

            iter.remove();

            assertEquals(--size, segment.size());

            validateSize(segment);
        }
    }

    /**
     * Tests removal from left size.
     */
    private void testRemoveFirstIter0(TestIntSet set) throws GridIntSet.ConversionException {
        log().info("testRemoveFirstIter0");

        int size = set.size();

        GridIntSet.Iterator iter = set.iterator();

        while(iter.hasNext()) {
            iter.next();

            iter.remove();

            assertEquals(--size, set.size());
        }

        assertEquals(0, set.size());
    }

    /**
     * Tests removal from right side.
     */
    private void testRemoveLast0(GridIntSet.Segment segment) throws GridIntSet.ConversionException {
        log().info("testRemoveLast0");

        int size = segment.size();

        while(size != segment.minSize()) {
            validateSize(segment);

            short val = segment.last();

            assertTrue(segment.remove(val));

            assertEquals(--size, segment.size());

            validateSize(segment);
        }
    }

    /**
     * Tests removal from right side.
     */
    private void testRemoveLast0(TestIntSet set) throws GridIntSet.ConversionException {
        log().info("testRemoveLast0");

        int size = set.size();

        while(size != 0) {
            int val = set.last();

            assertTrue(set.remove(val));

            assertEquals(--size, set.size());
        }
    }

    /**
     * Tests removal from right side.
     */
    private void testRemoveLastIter0(GridIntSet.Segment segment) throws GridIntSet.ConversionException {
        log().info("testRemoveLastIter0");

        int size = segment.size();

        GridIntSet.Iterator iter = segment.reverseIterator();

        while(iter.hasNext() && size != segment.minSize()) {
            validateSize(segment);

            iter.next();

            iter.remove();

            assertEquals(--size, segment.size());

            validateSize(segment);
        }
    }

    /**
     * Tests removal from right side.
     */
    private void testRemoveLastIter0(TestIntSet set) throws GridIntSet.ConversionException {
        log().info("testRemoveLastIter0");

        int size = set.size();

        GridIntSet.Iterator iter = set.reverseIterator();

        while(iter.hasNext()) {
            iter.next();

            iter.remove();

            assertEquals(--size, set.size());
        }

        assertEquals(0, set.size());
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
     * @param segment Segment.
     */
    private void validateSize(GridIntSet.Segment segment) {
        assertTrue(String.valueOf(segment.size()), segment.minSize() <= segment.size() && segment.size() <= segment.maxSize());

        assertTrue(String.valueOf(segment.data().length), segment.data().length <= MAX_WORDS);
    }

    /**
     * @param segment Segment.
     * @param vals Values.
     */
    public void assertContainsNothing(GridIntSet.Segment segment, List<Integer> vals) {
        for (Integer v : vals) {
            boolean val = segment.contains(v.shortValue());

            assertFalse("Contains: " + v, val);
        }
    }

    /**
     * @param segment Segment.
     * @param vals Vals.
     */
    public void assertContainsAll(GridIntSet.Segment segment, List<Integer> vals) {
        for (Integer v : vals) {
            boolean val = segment.contains(v.shortValue());

            assertTrue("Contains: " + v, val);
        }
    }

    /**
     * @param vals Values.
     * @param len Length.
     */
    private List<Integer> rndValues(List<Integer> vals, int len) {
        List<Integer> src = new ArrayList<>(len);

        while(src.size() != len) {
            int val = F.rand(vals);

            if (!src.contains(val))
                src.add(val);
        }

        return src;
    }

    /**
     * Fills segment with random values.
     *
     * @param seg Segment.
     * @param randomCnt If {@code true} operations count will be randomized.
     */
    private GridIntSet.Segment rndFill(GridIntSet.Segment seg, boolean randomCnt) throws GridIntSet.ConversionException {
        int cnt = seg.maxSize() - seg.minSize();

        if (randomCnt)
            cnt = ThreadLocalRandom8.current().nextInt(0, cnt);

        while(seg.size() != cnt) {
            short rnd = (short) ThreadLocalRandom8.current().nextInt(0, GridIntSet.SEGMENT_SIZE);

            if (!seg.contains(rnd))
                assertTrue(seg.add(rnd));
        }

        return seg;
    }

    /**
     * Fills set with random values.
     *
     * @param set Set.
     * @param randomCnt If {@code true} operations count will be randomized.
     */
    private GridIntSet rndFill(GridIntSet set, boolean randomCnt) {
        int cnt = MAX_VALUES / 10;

        if (randomCnt)
            cnt = ThreadLocalRandom8.current().nextInt(0, cnt);

        while(set.size() != cnt) {
            int rnd = ThreadLocalRandom8.current().nextInt(0, MAX_VALUES);

            if (!set.contains(rnd))
                assertTrue(set.add(rnd));
        }

        return set;
    }

    private TestIntSet rndFill2(TestIntSet set, boolean randomCnt) {
        int cnt = MAX_VALUES / 100;

        if (randomCnt)
            cnt = gridRandom.nextInt(cnt);

        while(set.size() != cnt) {
            int rnd = gridRandom.nextInt(MAX_VALUES);

            if (!set.contains(rnd))
                assertTrue(set.add(rnd));
        }

        return set;
    }

    /**
     * Fills segment with random values until full capacity.
     *
     * @param segment Segment.
     */
    private GridIntSet.Segment clear(GridIntSet.Segment segment, boolean randomize) throws GridIntSet.ConversionException {
        int cnt = segment.maxSize() - segment.minSize();

        if (randomize)
            cnt = ThreadLocalRandom8.current().nextInt(0, cnt);

        while(segment.size() != (segment.maxSize() - cnt)) {
            short rnd = (short) ThreadLocalRandom8.current().nextInt(0, GridIntSet.SEGMENT_SIZE);

            if (segment.contains(rnd))
                assertTrue(segment.remove(rnd));
        }

        return segment;
    }

    public interface TestIntSet {

        int size();

        int first();

        boolean remove(int val);

        boolean contains(int val);

        boolean add(int val);

        GridIntSet.Iterator iterator();

        int last();

        GridIntSet.Iterator reverseIterator();
    }

    public static class TestIntSetImpl implements TestIntSet {
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

    public static class TestIntSetImpl2 implements TestIntSet {
        private final ArrayList<Integer> list;

        public TestIntSetImpl2() {
            list = new ArrayList<>();
        }

        @Override
        public int size() {
            return list.size();
        }

        @Override
        public int first() {
            return list.get(0);
        }

        @Override
        public boolean remove(int val) {
            return list.remove((Integer)val);
        }

        @Override
        public boolean contains(int val) {
            return list.contains(val);
        }

        @Override
        public boolean add(int val) {
            return list.add(val);
        }

        @Override
        public GridIntSet.Iterator iterator() {
            final ListIterator<Integer> it = list.listIterator();

            return new GridIntSet.Iterator() {
                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public int next() {
                    return it.next();
                }

                @Override
                public void remove() {
                    it.remove();
                }

                @Override
                public void skipTo(int val) {

                }
            };
        }

        @Override
        public int last() {
            return list.get(list.size() - 1);
        }

        @Override
        public GridIntSet.Iterator reverseIterator() {
            final ListIterator<Integer> it = list.listIterator();

            // Move to the end.
            while(it.hasNext())
                it.next();

            return new GridIntSet.Iterator() {
                @Override
                public boolean hasNext() {
                    return it.hasPrevious();
                }

                @Override
                public int next() {
                    return it.previous();
                }

                @Override
                public void remove() {
                    it.remove();
                }

                @Override
                public void skipTo(int val) {

                }
            };
        }
    }

    public static class TestIntSetImpl3 implements TestIntSet {
        private final HashSet<Integer> list;

        public TestIntSetImpl3() {
            list = new HashSet<>();
        }

        @Override
        public int size() {
            return list.size();
        }

        @Override
        public int first() {
            return list.iterator().next();
        }

        @Override
        public boolean remove(int val) {
            return list.remove((Integer)val);
        }

        @Override
        public boolean contains(int val) {
            return list.contains(val);
        }

        @Override
        public boolean add(int val) {
            return list.add(val);
        }

        @Override
        public GridIntSet.Iterator iterator() {
            final Iterator<Integer> it = list.iterator();

            return new GridIntSet.Iterator() {
                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public int next() {
                    return it.next();
                }

                @Override
                public void remove() {
                    it.remove();
                }

                @Override
                public void skipTo(int val) {

                }
            };
        }

        @Override
        public int last() {
            return list.iterator().next();
        }

        @Override
        public GridIntSet.Iterator reverseIterator() {
            return iterator();
        }
    }

}