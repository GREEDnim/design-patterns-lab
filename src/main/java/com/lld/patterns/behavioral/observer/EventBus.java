package com.lld.patterns.behavioral.observer;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The Subject in the Observer pattern: keeps the set of interested listeners and
 * notifies them when an event is published, without knowing who they are.
 *
 * <p>Contract:
 * <ul>
 *   <li>{@link #subscribe(EventListener)} returns a {@link Subscription}; a null
 *       listener is rejected with {@link NullPointerException}. Subscribing the same
 *       listener twice creates two independent subscriptions.</li>
 *   <li>{@link #publish(Object)} notifies every <em>currently active</em> subscriber,
 *       in subscription order, each exactly once. A listener that throws must not
 *       stop the others, and must not propagate out of {@code publish}.</li>
 *   <li>{@link #subscriberCount()} is the number of active subscriptions.</li>
 *   <li>All three are safe to call concurrently.</li>
 * </ul>
 *
 * <p>Thread-safety rests on two ideas. The registrations live in a
 * {@link CopyOnWriteArrayList}, so {@code publish} iterates a stable <em>snapshot</em>
 * while other threads subscribe and cancel — no {@code ConcurrentModificationException},
 * and no shared lock on the hot path. Each registration carries an {@code active}
 * flag, so a subscriber cancelled mid-publish is simply skipped rather than
 * notified.
 */
public final class EventBus<T> {

    private final List<Registration<T>> registrations = new CopyOnWriteArrayList<>();

    /** Register a listener. Must reject null. */
    public Subscription subscribe(EventListener<T> listener) {
        Objects.requireNonNull(listener, "listener");
        Registration<T> registration = new Registration<>(listener);
        registrations.add(registration);
        return new BusSubscription<>(registrations, registration);
    }

    /** Notify every active subscriber, isolating a failing listener. */
    public void publish(T event) {
        // Copy-on-write iterator: a snapshot taken now, immune to concurrent
        // subscribe/cancel while we notify.
        for (Registration<T> registration : registrations) {
            if (!registration.active) {
                continue; // cancelled between the snapshot and here
            }
            try {
                registration.listener.onEvent(event);
            } catch (RuntimeException failure) {
                // Fault isolation: a broken listener must not stop the others. In
                // production this would also go to a metric/log/dead-letter sink
                // rather than being swallowed.
            }
        }
    }

    /** @return the number of active subscriptions. */
    public int subscriberCount() {
        int count = 0;
        for (Registration<T> registration : registrations) {
            if (registration.active) {
                count++;
            }
        }
        return count;
    }

    /**
     * One subscription's mutable state. The {@code active} flag is volatile so a
     * cancel on one thread is seen promptly by {@code publish}/{@code subscriberCount}
     * on another; the listener reference itself never changes.
     */
    private static final class Registration<T> {

        private final EventListener<T> listener;
        private volatile boolean active = true;

        private Registration(EventListener<T> listener) {
            this.listener = listener;
        }
    }

    /**
     * The handle handed back to a subscriber. It knows both the registration and the
     * list it lives in, so {@link #cancel()} removes exactly this subscription.
     */
    private static final class BusSubscription<T> implements Subscription {

        private final List<Registration<T>> registrations;
        private final Registration<T> registration;

        private BusSubscription(List<Registration<T>> registrations, Registration<T> registration) {
            this.registrations = registrations;
            this.registration = registration;
        }

        @Override
        public void cancel() {
            // Idempotent: flipping the flag twice is harmless, and remove() on an
            // already-removed element is a no-op.
            registration.active = false;
            registrations.remove(registration);
        }

        @Override
        public boolean isActive() {
            return registration.active;
        }
    }
}
