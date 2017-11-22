package org.apache.ignite.bench;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import org.jsr166.ConcurrentHashMap8;

public class Benchmark {

    private final int marks;
    private final String name;

    private ConcurrentHashMap8<Thread, Bench> threadBenches = new ConcurrentHashMap8<>();
    private ConcurrentHashMap8.Fun<Thread, Bench> ifAbsent = new ConcurrentHashMap8.Fun<Thread, Bench>() {
        @Override public Bench apply(Thread th) {
            return new Bench(marks);
        }
    };

    public Benchmark(String name) {
        this(name, Benchmarks.marks());
    }

    public Benchmark(String name, int marks) {
        this.marks = marks;
        this.name = name;

        Benchmarks.register(this);
    }

    public void supply(long mark) {
        threadBenches.computeIfAbsent(Thread.currentThread(), ifAbsent).supply((int)mark);
    }

    public void report(String now, PrintWriter aggregateLog, PrintWriter threadsLog) {
        StringBuilder builder = new StringBuilder();

        long[] columnValues = new long[Benchmarks.COLS];
        int[] columnCounts = new int[Benchmarks.COLS];
        long total = 0;

        for (Map.Entry<Thread, Bench> entry : threadBenches.entrySet()) {
            int[] marks = entry.getValue().gather();

            total += marks.length;

            Arrays.sort(marks);

            builder.append(now).append('\t');

            if (marks.length < this.marks)
                builder.append('*');

            builder.append(marks.length).append('\t');

            int columns = 0;
            int index = marks.length;
            while (index > 0 && columns < Benchmarks.COLS) {
                index = Math.min(index, (marks.length * (Benchmarks.COLS - columns) / Benchmarks.COLS)) - 1;
                builder.append(marks[index]).append('\t');
                columnValues[columns] += marks[index];
                columnCounts[columns]++;
                columns++;
            }

            while (columns++ < Benchmarks.COLS)
                builder.append('\t');

            builder.append(name).append('\t').append(entry.getKey().getName());
            threadsLog.println(builder.toString());
            builder.setLength(0);
        }

        // XXX Not fair percentiles
        builder.append(now).append('\t').append(total).append('\t');

        for (int i = 0; i < Benchmarks.COLS; i++) {
            if (columnCounts[i] > 0)
                builder.append(columnValues[i] / columnCounts[i]);

            builder.append('\t');
        }

        builder.append(name);

        aggregateLog.println(builder.toString());
    }

    public void reset() {
        for (Bench bench : threadBenches.values())
            bench.reset();
    }
}
