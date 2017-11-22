package org.apache.ignite.bench;

import java.util.Arrays;

public class Bench {
    public static final int[] NONE = new int[0];

    private int[] measurements;
    private int position;

    public Bench(int marks) {
        measurements = new int[marks];
        position = marks;
    }

    public int[] gather() {
        if (position == measurements.length)
            return NONE;

        if (position <= 0)
            return measurements;

        return Arrays.copyOfRange(measurements, position, measurements.length);
    }

    public void reset() {
        position = measurements.length;
    }

    public void supply(int measurement) {
        if (position <= 0) return;

        measurements[--position] = measurement;
    }
}
