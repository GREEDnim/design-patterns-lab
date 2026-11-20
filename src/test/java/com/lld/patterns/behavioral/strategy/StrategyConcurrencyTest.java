package com.lld.patterns.behavioral.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

/**
 * The thread-safety guarantee the production lens depends on: strategies are
 * stateless and immutable, so one instance can be shared across threads, and a
 * context can be read concurrently and even swapped at runtime.
 *
 * <p>These complement {@link StrategyTest}, which is single-threaded. They add the
 * cases a single thread misses: shared stateless instances under load, concurrent
 * reads of a shared context, and a runtime swap racing the readers.
 */
class StrategyConcurrencyTest {

    private static final int THREADS = 8;

    /**
     * One shared stateless strategy, hammered concurrently on distinct subtotals,
     * must return the exact right answer for every input. Any hidden mutable state
     * in the strategy would surface as a wrong result under load.
     */
    @Test
    void a_shared_stateless_strategy_is_safe_under_concurrent_use() throws Exception {
        DiscountStrategy shared = new PercentageDiscount(10);
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREADS);
        ConcurrentLinkedQueue<Throwable> failures = new ConcurrentLinkedQueue<>();

        for (int t = 0; t < THREADS; t++) {
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < 10_000; i++) {
                        long subtotal = 100 + i;
                        assertEquals(subtotal / 10, shared.discountCents(subtotal),
                                "wrong discount for subtotal " + subtotal);
                    }
                } catch (Throwable e) {
                    failures.add(e);
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        assertTrue(done.await(30, TimeUnit.SECONDS), "workers did not finish in time");
        pool.shutdownNow();

        assertTrue(failures.isEmpty(), "observed failures: " + failures);
    }

    /**
     * A context shared by many reader threads returns correct prices concurrently.
     * This guards the common case where one calculator (a singleton bean) serves all
     * request threads.
     */
    @Test
    void a_shared_context_is_safe_for_concurrent_reads() throws Exception {
        PriceCalculator calculator = new PriceCalculator(new TieredDiscount());
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREADS);
        ConcurrentLinkedQueue<Throwable> failures = new ConcurrentLinkedQueue<>();

        for (int t = 0; t < THREADS; t++) {
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < 10_000; i++) {
                        // 10_000 is in the 5% tier => 500 discount => 9_500 final.
                        assertEquals(9_500, calculator.finalPriceCents(10_000),
                                "10_000 must always price to 9_500");
                    }
                } catch (Throwable e) {
                    failures.add(e);
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        assertTrue(done.await(30, TimeUnit.SECONDS), "workers did not finish in time");
        pool.shutdownNow();

        assertTrue(failures.isEmpty(), "observed failures: " + failures);
    }

    /**
     * Swapping the strategy while readers run must be safe: no exceptions, and every
     * observed price is one a valid strategy produces — either the old rule
     * ({@code 1_000}) or the new rule ({@code 1_000 - 400 = 600}). A non-volatile
     * strategy field can be observed mid-change; this is the smoke test that the
     * field is published safely.
     */
    @Test
    void swapping_the_strategy_while_reading_is_safe() throws Exception {
        PriceCalculator calculator = new PriceCalculator(new NoDiscount());
        ConcurrentLinkedQueue<Throwable> failures = new ConcurrentLinkedQueue<>();
        AtomicBoolean running = new AtomicBoolean(true);

        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREADS);

        for (int t = 0; t < THREADS - 1; t++) {
            pool.submit(() -> {
                try {
                    start.await();
                    while (running.get()) {
                        long price = calculator.finalPriceCents(1_000);
                        if (price != 1_000 && price != 600) {
                            failures.add(new AssertionError("unexpected price " + price));
                        }
                    }
                } catch (Throwable e) {
                    failures.add(e);
                } finally {
                    done.countDown();
                }
            });
        }
        // The last worker flips the strategy while the others read.
        pool.submit(() -> {
            try {
                start.await();
                for (int i = 0; i < 50_000 && running.get(); i++) {
                    calculator.setStrategy(i % 2 == 0 ? new FlatDiscount(400) : new NoDiscount());
                }
            } catch (Throwable e) {
                failures.add(e);
            } finally {
                running.set(false);
                done.countDown();
            }
        });

        start.countDown();
        assertTrue(done.await(30, TimeUnit.SECONDS), "workers did not finish in time");
        pool.shutdownNow();

        assertTrue(failures.isEmpty(), "observed failures: " + failures);
    }
}
