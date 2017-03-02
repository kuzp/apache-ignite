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

package org.apache.ignite.math.impls.vector;

import org.apache.ignite.lang.*;
import org.apache.ignite.math.*;
import org.apache.ignite.math.UnsupportedOperationException;
import org.apache.ignite.math.Vector;
import org.apache.ignite.math.impls.matrix.MatrixView;
import org.apache.ignite.math.impls.storage.vector.VectorNullStorage;

import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * This class provides a helper implementation of the {@link Vector}
 * interface to minimize the effort required to implement it.
 * Subclasses may override some of the implemented methods if a more
 * specific or optimized implementation is desirable.
 */
public abstract class AbstractVector implements Vector {
    /** Vector cardinality. */
    private int size;

    /** Vector storage implementation. */
    private VectorStorage sto;

    /** Meta attribute storage. */
    private Map<String, Object> meta = new HashMap<>();

    /** Vector's GUID. */
    private IgniteUuid guid = IgniteUuid.randomUuid();

    /** Cached value for length squared. */
    private double lenSq = 0.0;

    /** Readonly flag (false by default). */
    private boolean readOnly = false;

    /** Read-only error message. */
    private static final String RO_MSG = "Vector is read-only.";

    /**
     *
     */
    private void ensureReadOnly() {
        if (readOnly)
            throw new UnsupportedOperationException(RO_MSG);
    }

    /**
     *
     * @param sto Storage.
     */
    public AbstractVector(VectorStorage sto, int cardinality) {
        this(false, sto, cardinality);
    }

    /**
     * @param readOnly Is read only.
     * @param sto Storage.
     * @param cardinality Cardinality.
     */
    public AbstractVector(boolean readOnly, VectorStorage sto, int cardinality) {
        this.readOnly = readOnly;
        this.sto = sto;
        this.size = cardinality;

        if (size < 0 )
            throw new IllegalArgumentException("Size can't be negative");
    }

    /**
     * @param readOnly Is read only.
     * @param cardinality Cardinality.
     */
    public AbstractVector(boolean readOnly, int cardinality) {
        this(readOnly, null, cardinality);
    }

    /**
     *
     */
    public AbstractVector() {
        this(false, new VectorNullStorage(), 0);
    }

    /**
     * @param cardinality Cardinality.
     */
    public AbstractVector(int cardinality) {
        this(false, new VectorNullStorage(), cardinality);
    }

    /**
     * Create new vector from other vector.
     *
     * @param vector Other vector.
     */
    public AbstractVector(Vector vector){
        this(vector == null ? null : vector.getStorage(), vector == null ? 0 : vector.size());
    }

    /**
     * Set storage.
     *
     * @param sto Storage.
     */
    protected void setStorage(VectorStorage sto) {
        this.sto = sto == null ? new VectorNullStorage() : sto;

        if(this.sto.size() > size)
            size = this.sto.size();
    }

    /**
     * Set storage with max size. Make sense for non-fixed storages.
     *
     * @param sto Storage.
     * @param maxSize Max storage size.
     */
    protected void setStorage(VectorStorage sto, int maxSize) {
        this.sto = sto == null ? new VectorNullStorage() : sto;

        this.size = maxSize;
    }

    /**
     *
     * @param i Index.
     * @param v Value.
     */
    protected void storageSet(int i, double v) {
        ensureReadOnly();

        sto.set(i, v);

        lenSq = 0.0;
    }

    /**
     *
     * @param i Index.
     * @return Value.
     */
    protected double storageGet(int i) {
        return sto.get(i);
    }

    /** {@inheritDoc} */
    @Override public int size() {
        return size;
    }

    /**
     * Check index bounds.
     *
     * @param idx Index to check.
     */
    protected void checkIndex(int idx) {
        if (idx < 0 || idx >= size)
            throw new IndexException(idx);
    }

    /** {@inheritDoc} */
    @Override public double get(int idx) {
        checkIndex(idx);

        return storageGet(idx);
    }

    /** {@inheritDoc} */
    @Override public double getX(int idx) {
        return storageGet(idx);
    }

    /** {@inheritDoc} */
    @Override public boolean isArrayBased() {
        return sto.isArrayBased();
    }

    /** {@inheritDoc} */
    @Override public Vector map(DoubleFunction<Double> fun) {
        if (sto.isArrayBased()) {
            double[] data = sto.data();

            Arrays.setAll(data, (idx) -> fun.apply(data[idx]));
        }
        else {
            int len = size();

            for (int i = 0; i < len; i++)
                storageSet(i, fun.apply(storageGet(i)));
        }

        return this;
    }

    /** {@inheritDoc} */
    @Override public Vector map(Vector vec, BiFunction<Double, Double, Double> fun) {
        checkCardinality(vec);

        int len = size();

        for (int i = 0; i < len; i++)
            storageSet(i, fun.apply(storageGet(i), vec.get(i)));

        return this;
    }

    /** {@inheritDoc} */
    @Override public Vector map(BiFunction<Double, Double, Double> fun, double y) {
        int len = size();

        for (int i = 0; i < len; i++)
            storageSet(i, fun.apply(storageGet(i), y));

        return this;
    }

    /**
     *
     * @param idx Index.
     * @return Value.
     */
    protected Element makeElement(int idx) {
        checkIndex(idx);

        return new Element() {
            /** {@inheritDoc} */
            @Override public double get() {
                return storageGet(idx);
            }

            /** {@inheritDoc} */
            @Override public int index() {
                return idx;
            }

            /** {@inheritDoc} */
            @Override public void set(double val) {
                storageSet(idx, val);
            }
        };
    }

    /** {@inheritDoc} */
    @Override public Element minValue() {
        int minIdx = 0;
        int len = size();

        for (int i = 0; i < len; i++)
            if (storageGet(i) < storageGet(minIdx))
                minIdx = i;

        return makeElement(minIdx);
    }

    /** {@inheritDoc} */
    @Override public Element maxValue() {
        int maxIdx = 0;
        int len = size();

        for (int i = 0; i < len; i++)
            if (storageGet(i) > storageGet(maxIdx))
                maxIdx = i;

        return makeElement(maxIdx);
    }

    /** {@inheritDoc} */
    @Override public Vector set(int idx, double val) {
        checkIndex(idx);

        storageSet(idx, val);

        return this;
    }

    /** {@inheritDoc} */
    @Override public Vector setX(int idx, double val) {
        storageSet(idx, val);

        return this;
    }

    /** {@inheritDoc} */
    @Override public Vector increment(int idx, double val) {
        checkIndex(idx);

        storageSet(idx, storageGet(idx) + val);

        return this;
    }

    /** {@inheritDoc} */
    @Override public Vector incrementX(int idx, double val) {
        storageSet(idx, storageGet(idx) + val);

        return this;
    }

    /**
     * Tests if given value is considered a zero value.
     *
     * @param val Value to check.
     */
    protected boolean isZero(double val) {
        return val == 0.0;
    }

    /** {@inheritDoc} */
    @Override public double sum() {
        double sum = 0;
        int len = size();

        for (int i = 0; i < len; i++)
            sum += storageGet(i);

        return sum;
    }

    /** {@inheritDoc} */
    @Override public IgniteUuid guid() {
        return guid;
    }

    /** {@inheritDoc} */
    @Override public int hashCode() {
        return guid.hashCode();
    }

    /** {@inheritDoc} */
    @Override public boolean equals(Object o) {
        return this == o || o != null && ((getClass() == o.getClass())) && ((sto.equals(((AbstractVector)o).sto)));
    }

    /** {@inheritDoc} */
    @Override public Iterable<Element> all() {
        return new Iterable<Element>() {
            private int idx = 0;

            @Override public Iterator<Element> iterator() {
                return new Iterator<Element>() {
                    @Override public boolean hasNext() {
                        return size() > 0 && idx < size();
                    }

                    @Override public Element next() {
                        if (hasNext())
                            return getElement(idx++);

                        throw new NoSuchElementException();
                    }
                };
            }
        };
    }

    /** {@inheritDoc} */
    @Override public int nonZeroElements() {
        int cnt = 0;

        for (Element ignored : nonZeroes())
            cnt++;

        return cnt;
    }

    /** {@inheritDoc} */
    @Override public <T> T foldMap(BiFunction<T, Double, T> foldFun, DoubleFunction<Double> mapFun, T zeroVal) {
        T res = zeroVal;
        int len = size();

        for (int i = 0; i < len; i++)
            res = foldFun.apply(res, mapFun.apply(storageGet(i)));

        return res;
    }

    /** {@inheritDoc} */
    @Override public <T> T foldMap(Vector vec, BiFunction<T, Double, T> foldFun, BiFunction<Double, Double, Double> combFun, T zeroVal) {
        checkCardinality(vec);

        T res = zeroVal;
        int len = size();

        for (int i = 0; i < len; i++)
            res = foldFun.apply(res, combFun.apply(storageGet(i), vec.getX(i)));

        return res;
    }

    /** {@inheritDoc} */
    @Override public Iterable<Element> nonZeroes() {
        return new Iterable<Element>() {
            private int idx = 0;
            private int idxNext = -1;

            @Override public Iterator<Element> iterator() {
                return new Iterator<Element>() {
                    @Override public boolean hasNext() {
                        findNext();

                        return !over();
                    }

                    @Override public Element next() {
                        if (hasNext()) {
                            idx = idxNext;

                            return getElement(idxNext);
                        }

                        throw new NoSuchElementException();
                    }

                    private void findNext() {
                        if (over())
                            return;

                        if (idxNextInitialized() && idx != idxNext)
                            return;

                        if (idxNextInitialized())
                            idx = idxNext + 1;

                        while (idx < size() && isZero(get(idx)))
                            idx++;

                        idxNext = idx++;
                    }

                    private boolean over() {
                        return idxNext >= size();
                    }

                    private boolean idxNextInitialized() {
                        return idxNext != -1;
                    }
                };
            }
        };
    }

    /** {@inheritDoc} */
    @Override public Map<String, Object> getMetaStorage() {
        return meta;
    }

    /** {@inheritDoc} */
    @Override public Vector assign(double val) {
        if (sto.isArrayBased()) {
            ensureReadOnly();

            Arrays.fill(sto.data(), val);
        }
        else {
            int len = size();

            for (int i = 0; i < len; i++)
                storageSet(i, val);
        }

        return this;
    }

    /** {@inheritDoc} */
    @Override public Vector assign(double[] vals) {
        checkCardinality(vals);

        if (sto.isArrayBased()) {
            ensureReadOnly();

            System.arraycopy(vals, 0, sto.data(), 0, vals.length);

            lenSq = 0.0;
        }
        else {
            int len = size();

            for (int i = 0; i < len; i++)
                storageSet(i, vals[i]);
        }

        return this;
    }

    /** {@inheritDoc} */
    @Override public Vector assign(Vector vec) {
        checkCardinality(vec);

        for (Vector.Element x : vec.all())
            storageSet(x.index(), x.get());

        return this;
    }

    /** {@inheritDoc} */
    @Override public Vector assign(IntToDoubleFunction fun) {
        assert fun != null;

        if (sto.isArrayBased()) {
            ensureReadOnly();

            Arrays.setAll(sto.data(), fun);
        }
        else {
            int len = size();

            for (int i = 0; i < len; i++)
                storageSet(i, fun.applyAsDouble(i));
        }

        return this;
    }

    /** {@inheritDoc} */
    @Override public Spliterator<Double> allSpliterator() {
        return new Spliterator<Double>() {
            /** {@inheritDoc} */
            @Override public boolean tryAdvance(Consumer<? super Double> act) {
                int len = size();

                for (int i = 0; i < len; i++)
                    act.accept(storageGet(i));

                return true;
            }

            /** {@inheritDoc} */
            @Override public Spliterator<Double> trySplit() {
                return null; // No Splitting.
            }

            /** {@inheritDoc} */
            @Override public long estimateSize() {
                return size();
            }

            /** {@inheritDoc} */
            @Override public int characteristics() {
                return ORDERED | SIZED;
            }
        };
    }

    /** {@inheritDoc} */
    @Override public Spliterator<Double> nonZeroSpliterator() {
        return new Spliterator<Double>() {
            /** {@inheritDoc} */
            @Override public boolean tryAdvance(Consumer<? super Double> act) {
                int len = size();

                for (int i = 0; i < len; i++) {
                    double val = storageGet(i);

                    if (!isZero(val))
                        act.accept(val);
                }

                return true;
            }

            /** {@inheritDoc} */
            @Override public Spliterator<Double> trySplit() {
                return null; // No Splitting.
            }

            /** {@inheritDoc} */
            @Override public long estimateSize() {
                return nonZeroElements();
            }

            /** {@inheritDoc} */
            @Override public int characteristics() {
                return ORDERED | SIZED;
            }
        };
    }

    /** {@inheritDoc} */
    @Override public double dot(Vector vec) {
        checkCardinality(vec);

        double sum = 0.0;
        int len = size();

        for (int i = 0; i < len; i++)
            sum += storageGet(i) * vec.getX(i);

        return sum;
    }

    /** {@inheritDoc} */
    @Override public double getLengthSquared() {
        if (lenSq == 0.0)
            lenSq = dotSelf();

        return lenSq;
    }

    /** {@inheritDoc} */
    @Override public boolean isDense() {
        return sto.isDense();
    }

    /** {@inheritDoc} */
    @Override public boolean isSequentialAccess() {
        return sto.isSequentialAccess();
    }

    /** {@inheritDoc} */
    @Override public double getLookupCost() {
        return sto.getLookupCost();
    }

    /** {@inheritDoc} */
    @Override public boolean isAddConstantTime() {
        return sto.isAddConstantTime();
    }

    /** {@inheritDoc} */
    @Override public VectorStorage getStorage() {
        return sto;
    }

    /** {@inheritDoc} */
    @Override public Vector viewPart(int off, int len) {
        return new VectorView(this, off, len);
    }

    /** {@inheritDoc} */
    @Override public Matrix cross(Vector vec) {
        Matrix res = likeMatrix(size(), vec.size());

        if (res == null)
            return null;

        for (Element e : nonZeroes()) {
            int row = e.index();

            res.assignRow(row, vec.times(getX(row)));
        }

        return res;
    }

    /** {@inheritDoc} */
    @Override public Matrix toMatrix(boolean rowLike) {
        Matrix res = likeMatrix(rowLike ? 1 : size(), rowLike ? size() : 1);

        if (res == null)
            return null;

        if (rowLike)
            res.assignRow(0, this);
        else
            res.assignColumn(0, this);

        return res;
    }

    /** {@inheritDoc} */
    @Override public Matrix toMatrixPlusOne(boolean rowLike, double zeroVal) {
        Matrix res = likeMatrix(rowLike ? 1 : size() + 1, rowLike ? size() + 1 : 1);

        if (res == null)
            return null;

        res.set(0, 0, zeroVal);

        if (rowLike)
            new MatrixView(res, 0, 1, 1, size()).assignRow(0, this);
        else
            new MatrixView(res, 1, 0, size(), 1).assignColumn(0, this);

        return res;
    }

    /** {@inheritDoc} */
    @Override public double getDistanceSquared(Vector vec) {
        checkCardinality(vec);

        double thisLenSq = getLengthSquared();
        double thatLenSq = vec.getLengthSquared();
        double dot = dot(vec);
        double distEst = thisLenSq + thatLenSq - 2 * dot;

        if (distEst > 1.0e-3 * (thisLenSq + thatLenSq))
            // The vectors are far enough from each other that the formula is accurate.
            return Math.max(distEst, 0);
        else
            return foldMap(vec, Functions.PLUS, Functions.MINUS_SQUARED, 0d);
    }

    /** */
    protected void checkCardinality(Vector vec) {
        if (vec.size() != size())
            throw new CardinalityException(size(), vec.size());
    }

    /** */
    protected void checkCardinality(double[] vec) {
        if (vec.length != size())
            throw new CardinalityException(size(), vec.length);
    }

    /** */
    protected void checkCardinality(int[] arr) {
        if (arr.length != size())
            throw new CardinalityException(size(), arr.length);
    }

    /** {@inheritDoc} */
    @Override public Vector minus(Vector vec) {
        checkCardinality(vec);

        Vector cp = copy();

        cp.map(vec, Functions.MINUS);

        return cp;
    }

    /** {@inheritDoc} */
    @Override public Vector plus(double x) {
        Vector cp = copy();

        if (x != 0.0)
            cp.map(Functions.plus(x));

        return cp;
    }

    /** {@inheritDoc} */
    @Override public Vector divide(double x) {
        Vector cp = copy();

        if (x != 1.0)
            for (Element element : cp.all())
                element.set(element.get() / x);

        return cp;
    }

    /** {@inheritDoc} */
    @Override public Vector times(double x) {
        if (x == 0.0)
            return like(size());
        else
            return copy().map(Functions.mult(x));
    }

    /** {@inheritDoc} */
    @Override public Vector times(Vector vec) {
        checkCardinality(vec);

        return copy().map(vec, Functions.MULT);
    }

    /** {@inheritDoc} */
    @Override public Vector plus(Vector vec) {
        checkCardinality(vec);

        Vector cp = copy();

        cp.map(vec, Functions.PLUS);

        return cp;
    }

    /** {@inheritDoc} */
    @Override public Vector logNormalize() {
        return logNormalize(2.0, Math.sqrt(getLengthSquared()));
    }

    /** {@inheritDoc} */
    @Override public Vector logNormalize(double power) {
        return logNormalize(power, kNorm(power));
    }

    /**
     * @param power Power.
     * @param normLen Normalized length.
     * @return logNormalized value.
     */
    private Vector logNormalize(double power, double normLen) {
        assert !(Double.isInfinite(power) || power <= 1.0);

        double denominator = normLen * Math.log(power);

        Vector cp = copy();

        for (Element element : cp.all())
            element.set(Math.log1p(element.get()) / denominator);

        return cp;
    }

    /** {@inheritDoc} */
    @Override public double kNorm(double power) {
        assert power >= 0.0;

        // Special cases.
        if (Double.isInfinite(power))
            return foldMap(Math::max, Math::abs, 0d);
        else if (power == 2.0)
            return Math.sqrt(getLengthSquared());
        else if (power == 1.0)
            return foldMap(Functions.PLUS, Math::abs, 0d);
        else if (power == 0.0)
            return nonZeroElements();
        else
            // Default case.
            return Math.pow(foldMap(Functions.PLUS, Functions.pow(power), 0d), 1.0 / power);
    }

    /** {@inheritDoc} */
    @Override public Vector normalize() {
        return divide(Math.sqrt(getLengthSquared()));
    }

    /** {@inheritDoc} */
    @Override public Vector normalize(double power) {
        return divide(kNorm(power));
    }

    /** {@inheritDoc} */
    @Override public Vector copy() {
        return like(size()).assign(this);
    }

    /**
     *
     * @return Result of dot with self.
     */
    protected double dotSelf() {
        double sum = 0.0;
        int len = size();

        for (int i = 0; i < len; i++) {
            double v = storageGet(i);

            sum += v * v;
        }

        return sum;
    }

    /** {@inheritDoc} */
    @Override public Element getElement(int idx) {
        return makeElement(idx);
    }

    /** {@inheritDoc} */
    @Override public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(sto);
        out.writeObject(meta);
        out.writeObject(guid);
        out.writeBoolean(readOnly);
        out.writeInt(size);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        sto = (VectorStorage)in.readObject();
        meta = (Map<String, Object>)in.readObject();
        guid = (IgniteUuid)in.readObject();
        readOnly = in.readBoolean();
        size = in.readInt();
    }

    /** {@inheritDoc} */
    @Override public void destroy() {
        sto.destroy();
    }
}
