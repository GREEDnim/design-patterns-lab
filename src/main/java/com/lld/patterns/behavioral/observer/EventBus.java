package com.lld.patterns.behavioral.observer;

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
 * <p>Stubbed for you to implement — replace the bodies, keep the signatures.
 */
public final class EventBus<T> {

    /** Register a listener. Must reject null. */
    public Subscription subscribe(EventListener<T> listener) {
        throw new UnsupportedOperationException("TODO: implement subscribe");
    }

    /** Notify every active subscriber, isolating a failing listener. */
    public void publish(T event) {
        throw new UnsupportedOperationException("TODO: implement publish");
    }

    /** @return the number of active subscriptions. */
    public int subscriberCount() {
        throw new UnsupportedOperationException("TODO: implement subscriberCount");
    }
}
