package org.apache.ignite.plugin;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.WALMode;
import org.apache.ignite.internal.util.typedef.CI1;
import org.apache.ignite.lang.IgniteFuture;
import org.apache.ignite.transactions.Transaction;

import static java.lang.Thread.MAX_PRIORITY;
import static java.util.concurrent.CompletableFuture.allOf;
import static org.apache.ignite.transactions.TransactionConcurrency.OPTIMISTIC;
import static org.apache.ignite.transactions.TransactionIsolation.REPEATABLE_READ;

public class TransactionPlugin {

    public static void threadId(long id) {

    }

    public static long threadId() {
        String name = Thread.currentThread().getName();

        int hash = name.hashCode();

        // System.err.println("thread " + name + " id " + hash);

        return hash;
    }

    private interface AsyncRes0 {
        void continueTx(Cls0 cls0);
    }

    private interface AsyncRes1<A> extends AsyncRes0 {
        void continueTx(Cls1<A> cls1);
    }

    private interface AsyncRes2<A, B> extends AsyncRes0 {
        void continueTx(Cls2<A, B> cls2);
    }

    private interface Cls0 {
        void apply();
    }

    private interface Cls1<A> {
        void apply(A a);
    }

    private interface Cls2<A, B> {
        void apply(A a, B b);
    }

    private interface Cls3<A, B, C> {
        void apply(A a, B b, C c);
    }

    private interface Cache<K, V> {
        CompletableFuture<V> getAsync(K key);

        CompletableFuture<Void> putAsync(K key, V value);
    }

    private interface Caches {
        <K, V> Cache<K, V> cache(String name);
    }

    private interface TransactionProcessor {
        Context next();
    }

    private interface Context {
        <T> CompletableFuture<T> startTx(Cls3<Transaction, Caches, CompletableFuture<T>> cls);

        <A> AsyncRes1<A> await(CompletableFuture<A> f1);

        <A, B> AsyncRes2<A, B> await(CompletableFuture<A> f1, CompletableFuture<B> f2);
    }

    private static class TxContext implements Context {
        private final long id;

        private final SingleThreadTransactionProcessor proc;

        private final Ignite ig;

        private final Caches caches;

        private TxContext(long id, SingleThreadTransactionProcessor proc, Ignite ig) {
            this.id = id;
            this.proc = proc;
            this.ig = ig;

            caches = new Caches() {
                @Override public <K, V> Cache<K, V> cache(String name) {
                    return new Cache<K, V>() {
                        private final IgniteCache<K, V> igCache = ig.cache(name);

                        @Override public CompletableFuture<V> getAsync(K key) {
                            return toFut(igCache.getAsync(key));
                        }

                        @Override public CompletableFuture<Void> putAsync(K key, V value) {
                            return toFut(igCache.putAsync(key, value));
                        }
                    };
                }
            };
        }

        @Override public <T> CompletableFuture<T> startTx(Cls3<Transaction, Caches, CompletableFuture<T>> cls) {
            CompletableFuture<T> done = new CompletableFuture<>();

            proc.async(new SingleThreadTransactionProcessor.TransactionTask() {
                @Override public long id() {
                    return id;
                }

                @Override public void run() {
                    Transaction tx = ig.transactions().txStart(OPTIMISTIC, REPEATABLE_READ);

                    cls.apply(tx, caches, done);
                }
            });
            return done;
        }

        @Override public <A> AsyncRes1<A> await(CompletableFuture<A> f1) {
            return null;
        }

        @Override public <A, B> AsyncRes2<A, B> await(CompletableFuture<A> f1, CompletableFuture<B> f2) {
            return new AsyncRes2<A, B>() {
                @Override public void continueTx(Cls2<A, B> cls2) {
                    allOf(f1, f2).thenRun(() -> proc.async(new SingleThreadTransactionProcessor.TransactionTask() {
                        @Override public long id() {
                            return id;
                        }

                        @Override public void run() {
                            try {
                                cls2.apply(f1.get(), f2.get());
                            }
                            catch (Throwable e) {
                                System.err.println(e);
                            }
                        }
                    }));
                }

                @Override public void continueTx(Cls0 cls0) {
                    allOf(f1, f2).thenRun(() -> proc.async(new SingleThreadTransactionProcessor.TransactionTask() {
                        @Override public long id() {
                            return id;
                        }

                        @Override public void run() {
                            cls0.apply();
                        }
                    }));
                }
            };
        }

        private static <T> CompletableFuture<T> toFut(IgniteFuture<T> fut) {
            CompletableFuture<T> f = new CompletableFuture<>();

            fut.listen((CI1<IgniteFuture<T>>)future -> f.complete(future.get()));

            return f;
        }
    }

    private static class SingleThreadTransactionProcessor implements TransactionProcessor, Runnable {

        private static final AtomicLong ID_GEN = new AtomicLong();

        private final BlockingQueue<TransactionTask> tasks = new LinkedBlockingQueue<>();

        private final Thread th;

        private final Ignite ig;

        private SingleThreadTransactionProcessor(Ignite ig) {
            this.ig = ig;

            th = new Thread(this);

            th.setDaemon(true);
            th.setPriority(MAX_PRIORITY);
            th.start();

            Thread.setDefaultUncaughtExceptionHandler(
                (t, e) -> System.err.println("Thread " + t.getName() + " e " + e)
            );
        }

        @Override public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    TransactionTask t = tasks.take();

                    long id = t.id();

                    //System.out.println("tx -> " + id);

                    Thread.currentThread().setName(String.valueOf(id));

                    t.run();
                }
                catch (Throwable e) {
                    System.err.println(e);
                }
            }
        }

        private void async(TransactionTask t) {
            tasks.add(t);
        }

        @Override public Context next() {
            return new TxContext(ID_GEN.getAndIncrement(), this, ig);
        }

        private interface TransactionTask {
            long id();

            void run();
        }
    }

    private static final String CACHE_NAME = "cache";

    private static final int ACCOUNTS = 5_000;

    private static final int MAX_TRANSFER = 1000;

    private static final int TXS = 500;

    private static final int ITER = 10;

    public static void main(String[] args) throws Throwable {
        boolean async = Boolean.valueOf(args[0]);
        boolean client = Boolean.valueOf(args[1]);

        IgniteConfiguration cfg = new IgniteConfiguration();

        if (client)
            cfg.setClientMode(true);

        cfg.setCacheConfiguration(
            new CacheConfiguration(CACHE_NAME)
                .setCacheMode(CacheMode.PARTITIONED)
                .setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL)
        );

        if (!client)
            cfg.setDataStorageConfiguration(
                new DataStorageConfiguration()
                    .setWalMode(WALMode.LOG_ONLY)
                    .setDefaultDataRegionConfiguration(
                        new DataRegionConfiguration()
                            .setPersistenceEnabled(true)
                    )
            );

        List<Long> keys = new LinkedList<>();

        Ignite ig = Ignition.start(cfg);

        Thread.currentThread().setName("-1");

        ig.cluster().active(true);

        load(ig);

        TransactionProcessor txProc = new SingleThreadTransactionProcessor(ig);

        //load2(txProc);

        long collector = 0L;

        for (int i = 0; i < ITER; i++) {
            System.out.println("Iteration " + i);

            AtomicLong time = new AtomicLong(System.currentTimeMillis());

            if (async)
                txAsync(time, txProc);
            else
                txSync(time, ig);

            check(ig);

            collector += time.get();
        }

        System.out.println("Execute time=" + collector / ITER + " txs=" + TXS);
    }

    private static void txSync(AtomicLong time, Ignite ig) {
        Random rnd = new Random();

        IgniteCache<Long, Account> cache = ig.cache(CACHE_NAME);

        for (int i = 0; i < TXS; i++) {
            long id1 = rnd.nextInt(ACCOUNTS);

            long id2;

            do {
                id2 = rnd.nextInt(ACCOUNTS);
            }
            while (id1 == id2);

            int money = rnd.nextInt(MAX_TRANSFER);

            try (Transaction tx = ig.transactions().txStart()) {
                Account acc1 = cache.get(id1);
                Account acc2 = cache.get(id2);

                acc1.balance += money;
                acc2.balance -= money;

                cache.put(acc1.id, acc1);
                cache.put(acc2.id, acc2);

                tx.commit();
            }
        }

        long start = time.get();

        time.set(System.currentTimeMillis() - start);
    }

    private static void txAsync(AtomicLong time, TransactionProcessor txProc) throws Throwable {
        CompletableFuture<?> finish = new CompletableFuture<>();

        CompletableFuture<?>[] futs = new CompletableFuture<?>[TXS];

        Random rnd = new Random();

        for (int i = 0; i < TXS; i++) {
            long id1 = rnd.nextInt(ACCOUNTS);

            long id2;

            do {
                id2 = rnd.nextInt(ACCOUNTS);
            }
            while (id1 == id2);

            int money = rnd.nextInt(MAX_TRANSFER);

            Context ctx = txProc.next();

            final long key1 = id1;
            final long key2 = id2;

            futs[i] = ctx.startTx((tx, caches, doneFut) -> {
                Cache<Long, Account> cache = caches.cache(CACHE_NAME);

                ctx.await(
                    cache.getAsync(key1),
                    cache.getAsync(key2)
                ).continueTx((acc1, acc2) -> {
                    acc1.balance -= money;
                    acc2.balance += money;

                    ctx.await(
                        cache.putAsync(acc1.id, acc1),
                        cache.putAsync(acc2.id, acc2)
                    ).continueTx(() -> {
                        tx.commit();

                        tx.close();

                        doneFut.complete(null);
                    });
                });
            });
        }

        allOf(futs).thenRun(() -> {
            long start = time.get();

            time.set(System.currentTimeMillis() - start);

            finish.complete(null);
        });

        finish.get();
    }

    public static void check(Ignite ig) throws IgniteCheckedException {
        IgniteCache<Long, Account> cache = ig.cache(CACHE_NAME);

        long sum = 0L;

        for (long i = 0; i < ACCOUNTS; i++) {
            Account acc = cache.get(i);

            if (acc == null)
                throw new IgniteCheckedException("Account not found id=" + i);

            sum += acc.balance;
        }

        assert sum == ACCOUNTS * 10_000 : sum;
    }

    private static void load(Ignite ig) {
        long start = System.currentTimeMillis();

        IgniteCache<Long, Account> cache = ig.cache(CACHE_NAME);

        for (long i = 0; i < ACCOUNTS; i++) {
            Account acc = new Account();

            acc.id = i;
            acc.balance = 10_000;

            cache.put(acc.id, acc);

            if (i % 10 == 0)
                System.out.println("loaded " + i);
        }

        System.out.println("Load time=" + (System.currentTimeMillis() - start));
    }

    private static void load2(TransactionProcessor txProc) throws ExecutionException, InterruptedException {
        long start = System.currentTimeMillis();

        txProc.next().startTx((tx, cache, done) -> {
            Cache<Long, Account> userCache = cache.cache(CACHE_NAME);

            for (long i = 0; i < ACCOUNTS; i++) {
                Account acc = new Account();

                acc.id = i;
                acc.balance = 10_000;

                try {
                    userCache.putAsync(acc.id, acc).get();
                }
                catch (InterruptedException | ExecutionException e) {
                    System.err.println(e);
                }
            }

            tx.commit();

            tx.close();

            done.complete(null);
        }).get();

        System.out.println("Load time=" + (System.currentTimeMillis() - start));
    }

    private static class Account {
        long id;
        long balance;

        @Override public String toString() {
            return "Account{" +
                "id=" + id +
                ", balance=" + balance +
                '}';
        }
    }
}
