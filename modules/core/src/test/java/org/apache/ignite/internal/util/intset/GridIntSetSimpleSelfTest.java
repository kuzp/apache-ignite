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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;

/**
 * Basic tests for {@link GridIntSet}.
 */
public class GridIntSetSimpleSelfTest extends GridCommonAbstractTest {
    /** */
    public void testAddRemoveSimple() {
        GridIntSet set = new GridIntSet(1024);

        GridIntSet.Thresholds thresholds = set.thresholds();

        int c = 0;

        for (int i = 0; i < thresholds.segmentSize; i++) {
            assertEquals(c, set.cardinality());

            assertFalse(set.contains(i));

            assertTrue("Failed to add: idx=" + i, set.add(i));

            assertEquals(++c, set.cardinality());

            assertTrue(set.contains(i));
        }

        assertEquals(thresholds.segmentSize, set.cardinality());

        c = thresholds.segmentSize;

        for (int i = 0; i < thresholds.segmentSize; i++) {
            assertEquals(c, set.cardinality());

            assertTrue(set.contains(i));

            assertTrue("Failed to remove: idx=" + i, set.remove(i));

            assertEquals(--c, set.cardinality());

            assertFalse(set.contains(i));
        }

        assertEquals(0, set.cardinality());
    }

    /** */
    public void testIteratorsSimple() {
        GridIntSet set = new GridIntSet(1024);

        assertEquals(-1, set.first());

        assertEquals(-1, set.last());

        assertFalse(set.iterator().hasNext());

        assertFalse(set.reverseIterator().hasNext());

        int size = set.thresholds().segmentSize;

        for (int i = 0; i < size; i++)
            assertTrue(set.add(i * size));

        assertEquals("Size", size, set.cardinality());

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

        assertEquals("Size", 0, set.cardinality());
    }
//
//    /** */
//    public void testSerialization() throws IOException, ClassNotFoundException {
//        GridIntSet set = new GridIntSet();
//
//        int size = GridIntSet.segmentSize;
//
//        for (int i = 0; i < size; i++)
//            assertTrue(set.add(i * size));
//
//        assertEquals("Size", size, set.cardinality());
//
//        GridByteArrayOutputStream bos = new GridByteArrayOutputStream();
//
//        ObjectOutputStream oos = new ObjectOutputStream(bos);
//
//        oos.writeObject(set);
//
//        oos.close();
//
//        byte[] bytes = bos.toByteArray();
//
//        System.out.println(bytes.length);
//
//        ObjectInputStream ois = new ObjectInputStream(new GridByteArrayInputStream(bytes));
//
//        GridIntSet set2 = (GridIntSet) ois.readObject();
//
//        assertEquals(set, set2);
//    }
//
    /** */
    private List<Integer> toList(GridIntSet.Iterator it) {
        List<Integer> l = new ArrayList<>();

        while(it.hasNext())
            l.add(it.next());

        return l;
    }
}
