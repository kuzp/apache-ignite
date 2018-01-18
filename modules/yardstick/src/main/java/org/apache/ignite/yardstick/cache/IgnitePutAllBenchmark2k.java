package org.apache.ignite.yardstick.cache;

/**
 *
 */
public class IgnitePutAllBenchmark2k extends IgnitePutAllBenchmark {
    /**
     *
     */
    @SuppressWarnings("unused")
    public static class Value {
        /** Value. */
        private byte[] val = new byte[2048];
    }

    /** {@inheritDoc} */
    @Override protected Object createValue(Integer key) {
        return new Value();
    }
}
