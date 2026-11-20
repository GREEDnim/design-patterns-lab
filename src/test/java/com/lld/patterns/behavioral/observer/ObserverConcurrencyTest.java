package com.lld.patterns.behavioral.observer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

/**
 * The thread-safety guarantee: subscribers can register, cancel and publish
 * concurrently without losing or duplicating deliveries for stable subscribers.
 *
 * <p>These complement {@link ObserverTest}, which is single-threaded. They add the
 * cases a single thread misses: many subscribers under load, concurrent publishes,
 * and subscribe/cancel churn racing the publisher.
 */
class ObserverConcurrencyTest {

    private static final int THREADS = 8;

    /**
     * Every active listener receives each event exactly once — no loss (a dropped
     * notification) and no duplication (a torn list or a fan-out bug).
     */
    @Test
    void every_active_listener_receives_each_event_exactly_once() {
        EventBus<Integer> bus = new EventBus<>();
        int subscribers = 10;
        int events = 1_000;

        List<AtomicInteger> counts = new ArrayList<>();
        for (int i = 0; i < subscribers; i++) {
            AtomicInteger count = new AtomicInteger();
            counts.add(count);
            bus.subscribe(e -> count.incrementAndGet());
        }

        for (int i = 0; i < events; i++) {
            bus.publish(i);
        }

        for (int i = 0; i < counts.size(); i++) {
            assertEquals(events, counts.get(i).get(),
                    "subscriber " + i + " missed or double-counted events");
        }
    }

    /**
     * Events published from many threads are each delivered exactly once. This is the
     * case a single-threaded suite misses: concurrent fan-out must not drop a
     * notification (a lost publish) or double it (a broken iteration).
     */
    @Test
    void concurrent_publishes_deliver_every_event_exactly_once() throws Exception {
        EventBus<Integer> bus = new EventBus<>();
        AtomicInteger count = new AtomicInteger();
        bus.subscribe(e -> count.incrementAndGet());

        int perThread = 1_000;
        runConcurrently(() -> {
            for (int i = 0; i < perThread; i++) {
                bus.publish(1);
            }
        });

        assertEquals(THREADS * perThread, count.get(),
                "every published event must be delivered exactly once");
    }

    /**
     * Subscribe/cancel churn racing the publisher must never throw — for instance a
     * {@link java.util.ConcurrentModificationException} from iterating a list that is
     * mutated mid-notification — and must never lose a stable subscriber.
     */
    @Test
    void concurrent_subscribe_cancel_and_publish_do_not_throw() throws Exception {
        EventBus<String> bus = new EventBus<>();
        AtomicBoolean running = new AtomicBoolean(true);
        AtomicInteger stableDeliveries = new AtomicInteger();
        ConcurrentLinkedQueue<Throwable> failures = new ConcurrentLinkedQueue<>();

        // A long-lived subscriber that must keep receiving throughout the churn.
        bus.subscribe(e -> stableDeliveries.incrementAndGet());

        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREADS);

        for (int t = 0; t < THREADS; t++) {
            boolean publisher = t % 2 == 0;
            pool.submit(() -> {
                try {
                    start.await();
                    if (publisher) {
                        while (running.get()) {
                            bus.publish("e");
                        }
                    } else {
                        while (running.get()) {
                            Subscription subscription = bus.subscribe(e -> { });
                            subscription.cancel();
                        }
                    }
                } catch (Throwable e) {
                    failures.add(e);
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        Thread.sleep(200); // let the workers churn for a bounded window
        running.set(false);
        assertTrue(done.await(30, TimeUnit.SECONDS), "workers did not finish in time");
        pool.shutdownNow();

        assertTrue(failures.isEmpty(), "observed failures: " + failures);
        assertTrue(stableDeliveries.get() > 0, "the stable subscriber should have received events");
    }

    /** Runs {@code task} on {@link #THREADS} threads released together. */
    private static void runConcurrently(Runnable task) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREADS);

        for (int t = 0; t < THREADS; t++) {
            pool.submit(() -> {
                try {
                    start.await();
                    task.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        assertTrue(done.await(30, TimeUnit.SECONDS), "workers did not finish in time");
        pool.shutdownNow();
    }
}
