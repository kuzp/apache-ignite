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
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.jsr166.ThreadLocalRandom8;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Test for {@link GridIntSet}.
 */
public class GridIntSetSelfTest extends GridCommonAbstractTest {
    /**
     * Tests array segment.
     */
    public void testArraySegment() throws GridIntSet.ConvertException {
        testRemoveAddRemoveRnd0(fill(new GridIntSet.ArraySegment(), -1));

        testRemoveFirst0(fill(new GridIntSet.ArraySegment(), -1));

        testRemoveLast0(fill(new GridIntSet.ArraySegment(), -1));

        GridIntSet.ArraySegment seg = new GridIntSet.ArraySegment();

        int size = seg.maxSize() - seg.minSize();

        testIterators0(fill(new GridIntSet.ArraySegment(), ThreadLocalRandom8.current().nextInt(0, size)));
    }

    /**
     * Tests flipped array segment.
     */
    public void testFlippedArraySegment() throws GridIntSet.ConvertException {
        testRemoveAddRemoveRnd0(new GridIntSet.FlippedArraySegment());

        testRemoveFirst0(new GridIntSet.FlippedArraySegment());

        testRemoveLast0(new GridIntSet.FlippedArraySegment());

        GridIntSet.FlippedArraySegment seg = new GridIntSet.FlippedArraySegment();

        int size = seg.maxSize() - seg.minSize();

        testIterators0(clear(new GridIntSet.FlippedArraySegment(), ThreadLocalRandom8.current().nextInt(0, size)));
    }

    /**
     * Tests array segment.
     */
    public void testBitSetSegment() throws GridIntSet.ConvertException {
        testRemoveAddRemoveRnd0(fill(new GridIntSet.BitSetSegment(), -1));

        testRemoveFirst0(fill(new GridIntSet.BitSetSegment(), -1));

        testRemoveLast0(fill(new GridIntSet.BitSetSegment(), -1));

        GridIntSet.BitSetSegment seg = new GridIntSet.BitSetSegment();

        int size = seg.maxSize() - seg.minSize();

        testIterators0(fill(new GridIntSet.BitSetSegment(), ThreadLocalRandom8.current().nextInt(0, size)));
    }

    public void testConvertSegment() {

    }

    /**
     * Tests array segment.
     */
    private void testRemoveAddRemoveRnd0(GridIntSet.Segment segment) throws GridIntSet.ConvertException {
        int size = segment.size();

        assertEquals(segment.maxSize(), size);

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
    private void testRemoveFirst0(GridIntSet.Segment segment) throws GridIntSet.ConvertException {
        int size = segment.size();

        assertEquals(segment.maxSize(), size);

        while(size != segment.minSize()) {
            validateSize(segment);

            short val = segment.first();

            assertTrue(segment.remove(val));

            assertEquals(--size, segment.size());

            validateSize(segment);
        }
    }

    /**
     * Tests removal from right side.
     */
    private void testRemoveLast0(GridIntSet.Segment segment) throws GridIntSet.ConvertException {
        int size = segment.size();

        assertEquals(segment.maxSize(), size);

        while(size != segment.minSize()) {
            validateSize(segment);

            short val = segment.last();

            assertTrue(segment.remove(val));

            assertEquals(--size, segment.size());

            validateSize(segment);
        }
    }

    /**
     *
     * @param segment Segment.
     */
    private void testIterators0(GridIntSet.Segment segment) {
        List<Integer> fwd = new ArrayList<>();

        GridIntSet.Iterator fwdIt = segment.iterator();

        while(fwdIt.hasNext())
            fwd.add(fwdIt.next());

        List<Integer> rev = new ArrayList<>();

        GridIntSet.Iterator revIt = segment.reverseIterator();

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

        assertTrue(String.valueOf(segment.data().length), segment.data().length <= GridIntSet.THRESHOLD);
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
     * Fills segment with random values until full capacity.
     *
     * @param segment Segment.
     */
    private GridIntSet.Segment fill(GridIntSet.Segment segment, int cnt) throws GridIntSet.ConvertException {
        if (cnt == -1)
            cnt = segment.maxSize();

        while(segment.size() != cnt) {
            short rnd = (short) ThreadLocalRandom8.current().nextInt(0, GridIntSet.SEGMENT_SIZE);

            if (!segment.contains(rnd))
                assertTrue(segment.add(rnd));
        }

        return segment;
    }

    /**
     * Fills segment with random values until full capacity.
     *
     * @param segment Segment.
     */
    private GridIntSet.Segment clear(GridIntSet.Segment segment, int cnt) throws GridIntSet.ConvertException {
//        if (cnt == -1)
//            cnt = segment.maxSize();
//
//        while(segment.size() != (segment.maxSize() - cnt)) {
//            short rnd = (short) ThreadLocalRandom8.current().nextInt(0, GridIntSet.SEGMENT_SIZE);
//
//            if (segment.contains(rnd))
//                assertTrue(segment.remove(rnd));
//        }

        //segment.remove(0);

        return segment;
    }
}