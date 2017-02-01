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
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.jsr166.ThreadLocalRandom8;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Test for {@link GridHandleTable}.
 */
public class GridIntSetSelfTest extends GridCommonAbstractTest {
    /**
     * Tests array segment.
     */
    public void testArraySegment() throws GridIntSet.ConvertException {
    }

    /**
     * Tests array segment.
     */
    public void testFlippedArraySegment() throws GridIntSet.ConvertException {
        GridIntSet.FlippedArraySegment segment = new GridIntSet.FlippedArraySegment();

        List<Integer> rnd = source(GridIntSet.THRESHOLD);

        List<Integer> ordered = new ArrayList<>(rnd);

        Collections.sort(ordered);

        assertEquals("Size", GridIntSet.SEGMENT_SIZE, segment.size());

        for (Integer val : rnd) {
            assertTrue(segment.data().length <= GridIntSet.THRESHOLD);

            assertTrue(segment.contains(val.shortValue()));

            assertTrue(segment.remove(val.shortValue()));

            assertTrue(segment.data().length <= GridIntSet.THRESHOLD);
        }

        assertEquals("Size", GridIntSet.THRESHOLD2, segment.size());

        testSegment0(segment, rnd, ordered);
    }

    /** */
    private void testSegment0(GridIntSet.Segment segment, List<Integer> vals, List<Integer> sortedVals) throws GridIntSet.ConvertException {
        log().info("Data: " + vals);

        log().info("Sorted: " + sortedVals);

        int before = segment.size();

        for (Integer v : vals) {
            boolean val = segment.contains(v.shortValue());

            assertFalse("Contains: " + v, val);
        }

        for (Integer v : vals) {
            boolean val = segment.add(v.shortValue());

            assertTrue("Added: " + v, val);
        }

        for (Integer v : vals) {
            boolean val = segment.contains(v.shortValue());

            assertTrue("Contains: " + v, val);
        }

        Collections.shuffle(vals);

        assertEquals("After", before + vals.size(), segment.size());

        for (Integer v : vals) {
            boolean val = segment.remove(v.shortValue());

            assertTrue("Removed: " + v, val);
        }

        for (Integer v : vals) {
            boolean val = segment.contains(v.shortValue());

            assertFalse("Contains: " + v, val);
        }

        assertEquals("Size", before, segment.size());

//
//        assertEquals("First", sortedVals.get(0).intValue(), segment.first());
//
//        assertEquals("Last", sortedVals.get(vals.size() - 1).intValue(), segment.last());
    }

    /**
     * @param len Length.
     */
    private List<Integer> source(int len) {
        List<Integer> src = new ArrayList<>(len);

        while(src.size() != len) {
            int i = ThreadLocalRandom8.current().nextInt(0, GridIntSet.SEGMENT_SIZE);

            if (!src.contains(i))
                src.add(i);
        }

        return src;
    }

    /**
     * Tests set grow.
     */
    public void testSet() {
        for (short step = 1; step <= 5; step++) {
            GridIntSet set = new GridIntSet();

            short size = 1;

            short i;
            for (i = 0; i < GridIntSet.SEGMENT_SIZE; i += step, size++) {
                set.add(i);

                testPredicates(set, size);
            }

            /** Check using {@link GridIntSet#contains(int)}. */
            for (i = 0; i < set.size(); i ++)
                assertEquals("Contains: " + i, i % step == 0, set.contains(i));

            /** Double check using {@link GridIntSet#iterator()}. */
            GridIntSet.Iterator it = set.iterator();

            i = 0;

            while(it.hasNext()) {
                assertEquals(i, it.next());

                i += step;
            }

            size = GridIntSet.SEGMENT_SIZE - 1;

            for (i = 0; i < GridIntSet.SEGMENT_SIZE; i += step, size--) {
                set.remove(i);

                testPredicates(set, size);
            }
        }
    }

    /** */
    private void testPredicates(GridIntSet set, short expSize) {
        GridIntSet.Segment seg = segment(set, 0);

        if (set.size() <= GridIntSet.THRESHOLD)
            assertTrue(seg instanceof GridIntSet.ArraySegment);
        else if (set.size() > GridIntSet.THRESHOLD2)
            assertTrue(seg instanceof GridIntSet.FlippedArraySegment);
        else
            assertTrue(seg.getClass().toString(), seg instanceof GridIntSet.BitSetSegment);

        assertEquals(expSize, seg.size());

        // Array length is upper power of two in relation to used elements length.
        assertEquals(seg.data().length, seg.used() == 0 ? 0 :
                1 << (Integer.SIZE - Integer.numberOfLeadingZeros(seg.used() - 1)));

        assertTrue(seg.data().length <= GridIntSet.MAX_WORDS);
    }

    /** */
    private GridIntSet.Segment segment(GridIntSet set, int idx) {
        GridIntSet.Segment[] segments = U.field(set, "segments");

        return segments[idx];
    }

    /**
     * @throws Exception If failed.
     */
    public void testAdd() throws Exception {
//        GridIntSet set = new GridIntSet();
//
//        for (int i = 0; i < Short.SIZE; i++) {
//            set.add(i);
//
//            short[][] segments = U.field(set, "segments");
//
//            assertEquals("Words used", 1, segments[0].length);
//        }
//
//        set.dump();
//
//        set.add(60);
//        set.dump();
//
//        short tmp = -1;
//
//        System.out.println(Integer.toBinaryString(tmp));
    }
}
