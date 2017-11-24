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

package org.apache.ignite.internal.processors.cache.local;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.cache.Cache;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.binary.BinaryRawReader;
import org.apache.ignite.binary.BinaryRawWriter;
import org.apache.ignite.binary.BinaryReader;
import org.apache.ignite.binary.BinaryWriter;
import org.apache.ignite.binary.Binarylizable;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.QueryEntity;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.cache.query.SqlQuery;
import org.apache.ignite.cache.query.annotations.QuerySqlField;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.internal.processors.cache.IgniteCacheAbstractQuerySelfTest;
import org.apache.ignite.internal.processors.cache.persistence.CacheDataRowAdapter;

import static org.apache.ignite.cache.CacheMode.PARTITIONED;

/**
 * Tests local query.
 */
public class IgniteCacheLocalQuerySelfTest extends IgniteCacheAbstractQuerySelfTest {
    /** Keys count. */
    private static final int KEYS = 1_000;
    /** Random. */
    private static Random RND = new Random();

    /** {@inheritDoc} */
    @Override protected int gridCount() {
        return 1;
    }

    /** {@inheritDoc} */
    @Override protected CacheMode cacheMode() {
        return PARTITIONED;
    }

    /** {@inheritDoc} */
    @Override protected CacheConfiguration cacheConfiguration() {
        return super.cacheConfiguration()
            .setQueryEntities(Arrays.asList(
                new QueryEntity(KeyObject.class, ValueObject.class),
                new QueryEntity(Integer.class, BigValue.class)));
    }

    /**
     * @throws Exception If test failed.
     */
    public void testQueryLocal() throws Exception {
        IgniteCache<Integer, String> cache = jcache(Integer.class, String.class);

        cache.put(1, "value1");
        cache.put(2, "value2");
        cache.put(3, "value3");
        cache.put(4, "value4");
        cache.put(5, "value5");

        // Tests equals query.
        QueryCursor<Cache.Entry<Integer, String>> qry =
            cache.query(new SqlQuery<Integer, String>(String.class, "_val='value1'").setLocal(true));

        Iterator<Cache.Entry<Integer, String>> iter = qry.iterator();

        Cache.Entry<Integer, String> entry = iter.next();

        assert !iter.hasNext();

        assert entry != null;
        assert entry.getKey() == 1;
        assert "value1".equals(entry.getValue());

        // Tests like query.
        qry = cache.query(new SqlQuery<Integer, String>(String.class, "_val like 'value%'").setLocal(true));

        iter = qry.iterator();

        assert iter.next() != null;
        assert iter.next() != null;
        assert iter.next() != null;
        assert iter.next() != null;
        assert iter.next() != null;
        assert !iter.hasNext();

        // Test explain for primitive index.
        List<List<?>> res = cache.query(new SqlFieldsQuery(
            "explain select _key from String where _val > 'value1'").setLocal(true)).getAll();

        assertTrue("__ explain: \n" + res, ((String)res.get(0).get(0)).toLowerCase().contains("_val_idx"));
    }

    /**
     * @throws Exception On failed.
     */
    public void testQueryLocalNoCopyFlag() throws Exception {
        IgniteCache<KeyObject, ValueObject> cache = jcache(KeyObject.class, ValueObject.class);

        for (int i = 0; i < KEYS; ++i)
            cache.put(new KeyObject(i), new ValueObject(i));

        for (int begIdx = KEYS - 3; begIdx < KEYS - 1; ++begIdx) {
            Set<ValueObject> set = new HashSet<>();

            for (int i = begIdx + 1; i < KEYS; ++i)
                set.add(new ValueObject(i));

            Iterator<Cache.Entry<KeyObject, ValueObject>> it = cache.query(
                new SqlQuery<KeyObject, ValueObject>(ValueObject.class, "longVal > " + begIdx)
                    .setLocalNoCopy(true).setLocal(true)).iterator();

            while (it.hasNext()) {
                Cache.Entry<KeyObject, ValueObject> e = it.next();

                assertTrue("Invalid val: e.getValue()", set.contains(e.getValue()));

                System.out.println("read " + e.getValue());

                set.remove(e.getValue());
            }

            assertTrue("Leak locks count: " + set.size(), set.isEmpty());
        }
    }

    /**
     * @throws Exception On failed.
     */
    public void _testQueryLocalNoCopySimpleBenchmark() throws Exception {
        IgniteCache<Integer, BigValue> cache = jcache(Integer.class, BigValue.class);

        for (int i = 0; i < KEYS; ++i)
            cache.put(i, new BigValue(RND.nextInt(10)));

        SqlQuery query = new SqlQuery(BigValue.class, "search = ?");
        query.setArgs(5);
        query.setLocal(true);
        query.setLocalNoCopy(true);

        System.out.println("+++ Benchmark");

        while (true) {
            long t0 = System.currentTimeMillis();

            for (int op = 0; op < 1000; ++op) {
                QueryCursor cur = cache.withKeepBinary().query(query);

                Iterator it = cur.iterator();

                it.next();
                it.next();

//                System.out.println("Lock before close: " + CacheDataRowAdapter.OffheapPageLocker.dbgMap.size());
                cur.close();
//                System.out.println("Lock after  close: " + CacheDataRowAdapter.OffheapPageLocker.dbgMap.size());
            }

            System.out.println("Throughput: " + ((System.currentTimeMillis() - t0) / 1000.0));
        }

    }

    /**
     *
     */
    private static class KeyObject {
        /** Key. */
        private long key;

        /**
         * @param key Key.
         */
        KeyObject(long key) {
            this.key = key;
        }

        /** {@inheritDoc} */
        @Override public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            KeyObject object = (KeyObject)o;

            return key == object.key;
        }

        /** {@inheritDoc} */
        @Override public int hashCode() {
            return (int)(key ^ (key >>> 32));
        }
    }

    /**
     *
     */
    private static class ValueObject {
        /** Long value. */
        @QuerySqlField
        private long longVal;

        /** String value. */
        private String strVal;

        /** String value. */
        private byte[] arr = new byte[2000];

        /**
         * @param key Key.
         */
        ValueObject(int key) {
            longVal = key;

            strVal = "String value " + key;
        }

        /** {@inheritDoc} */
        @Override public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            ValueObject object = (ValueObject)o;

            if (longVal != object.longVal)
                return false;
            return strVal != null ? strVal.equals(object.strVal) : object.strVal == null;
        }

        /** {@inheritDoc} */
        @Override public int hashCode() {
            int result = (int)(longVal ^ (longVal >>> 32));
            result = 31 * result + (strVal != null ? strVal.hashCode() : 0);
            return result;
        }

        @Override public String toString() {
            return "ValueObject{" +
                "longVal=" + longVal +
                ", strVal='" + strVal + '\'' +
                '}';
        }
    }

    /**
     *
     */
    private static class BigValue implements Binarylizable {
        /** Search. */
        @QuerySqlField(index = true)
        private int search;

        /**
         * @param search Search field.
         */
        public BigValue(int search) {
            this.search = search;
        }

        /** {@inheritDoc} */
        @Override public void writeBinary(BinaryWriter writer) {
            writer.writeInt("search", search);

            BinaryRawWriter rawWriter = writer.rawWriter();

            for (int i = 0; i < 400; i++)
                rawWriter.writeInt(123);
        }

        /** {@inheritDoc} */
        @Override public void readBinary(BinaryReader reader) {
            search = reader.readInt("search");

            BinaryRawReader rawReader = reader.rawReader();

            for (int i = 0; i < 400; i++)
                rawReader.readInt();
        }
    }
}