package org.apache.ignite.math.impls.storage;

import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;
import org.apache.ignite.math.VectorStorage;

/**
 * Implements vector that only stores non-zero doubles.
 * TODO wip
 */
public class RandomAccessSparseVectorStorage implements VectorStorage{
    private int size;
    private Int2DoubleOpenHashMap data;

    private static final int INITIAL_CAPACITY = 11;

    /** For serialization. */
    public RandomAccessSparseVectorStorage(){
        // No-op.
    }

    /**
     *
     * @param cardinality
     */
    public RandomAccessSparseVectorStorage(int cardinality){
        this(cardinality, Math.min(cardinality, INITIAL_CAPACITY));
    }

    /**
     *
     * @param size
     * @param initialCapacity
     */
    public RandomAccessSparseVectorStorage(int size, int initialCapacity) {
        this.size = size;
        this.data = new Int2DoubleOpenHashMap(initialCapacity, .5f);
    }

    private RandomAccessSparseVectorStorage(int size, Int2DoubleOpenHashMap values) {
        this.size = size;
        this.data = values;
    }

    /** {@inheritDoc} */
    @Override public int size() {
        return size;
    }

    /** {@inheritDoc} */
    @Override public double get(int i) {
        return data.get(i);
    }

    /** {@inheritDoc} */
    @Override public void set(int i, double v) {
        if (v == 0.0)
            data.remove(i);
        else
            data.put(i, v);
    }

    /** {@inheritDoc} */
    @Override public double[] data() {
        return data.values().toDoubleArray();
    }

    /** {@inheritDoc} */
    @Override public void writeExternal(ObjectOutput out) throws IOException {
        out.write(size);

        out.writeObject(data.entrySet());
    }

    /** {@inheritDoc} */
    @Override public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        size = in.readInt();

        data = new Int2DoubleOpenHashMap(size, .5f);

        ObjectSet<Map.Entry<Integer, Double>> rawData = (ObjectSet<Map.Entry<Integer, Double>>)in.readObject();

        rawData.forEach(pair -> data.put(pair.getKey().intValue(), pair.getValue().doubleValue()));
    }

    /** {@inheritDoc} */
    @Override public boolean isSequentialAccess() {
        return false;
    }

    /** {@inheritDoc} */
    @Override public boolean isDense() {
        return false;
    }

    /** {@inheritDoc} */
    @Override public double getLookupCost() {
        return 1;
    }

    /** {@inheritDoc} */
    @Override public boolean isAddConstantTime() {
        return true;
    }

    /** {@inheritDoc} */
    @Override public boolean isArrayBased() {
        return false;
    }

    @Override protected Object clone() throws CloneNotSupportedException {
        return new RandomAccessSparseVectorStorage(size, data.clone());
    }
}
