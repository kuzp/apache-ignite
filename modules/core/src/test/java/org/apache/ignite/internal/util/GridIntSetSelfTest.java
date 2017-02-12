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
import java.util.List;

/**
 * Test for {@link GridIntSet}.
 */
public class GridIntSetSelfTest extends GridCommonAbstractTest {
    /** Max words. */
    private static final int MAX_WORDS = GridIntSet.SEGMENT_SIZE / Short.SIZE;

    private static final int MAX_VALUES = GridIntSet.SEGMENT_SIZE * GridIntSet.SEGMENT_SIZE;

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
    private void testRemoveFirstIter0(GridIntSet.Segment segment) throws GridIntSet.ConversionException {
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
     * Tests removal from right side.
     */
    private void testRemoveLast0(GridIntSet.Segment segment) throws GridIntSet.ConversionException {
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
    private void testRemoveLastIter0(GridIntSet.Segment segment) throws GridIntSet.ConversionException {
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
     * @param randomizeCnt If {@code true} operations count will be randomized.
     */
    private GridIntSet.Segment rndFill(GridIntSet.Segment seg, boolean randomizeCnt) throws GridIntSet.ConversionException {
        int cnt = seg.maxSize() - seg.minSize();

        if (randomizeCnt)
            cnt = ThreadLocalRandom8.current().nextInt(0, cnt);

        while(seg.size() != cnt) {
            short rnd = (short) ThreadLocalRandom8.current().nextInt(0, GridIntSet.SEGMENT_SIZE);

            if (!seg.contains(rnd))
                assertTrue(seg.add(rnd));
        }

        return seg;
    }

    /**
     *
     * Fills set with random values.
     *
     * @param set Set.
     * @param randomizeCnt If {@code true} operations count will be randomized.
     */
    private void rndFill(GridIntSet set, boolean randomizeCnt) {
        int cnt = MAX_VALUES / 10;

        if (randomizeCnt)
            cnt = ThreadLocalRandom8.current().nextInt(0, cnt);

        while(set.size() != cnt) {
            int rnd = ThreadLocalRandom8.current().nextInt(0, MAX_VALUES);

            if (!set.contains(rnd))
                assertTrue(set.add(rnd));
        }
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
}