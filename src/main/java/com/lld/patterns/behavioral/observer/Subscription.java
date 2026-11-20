package com.lld.patterns.behavioral.observer;

/**
 * A handle to one active subscription, returned by {@code EventBus.subscribe}.
 *
 * <p>This is the idiomatic way to let an observer leave: rather than the bus
 * removing by identity (which is fragile for lambdas), the subscriber holds the
 * handle and cancels it. Cancelling must be safe to call more than once.
 */
public interface Subscription {

    /** Stop future delivery to this subscriber. Idempotent. */
    void cancel();

    /** @return {@code true} until {@link #cancel()} is called. */
    boolean isActive();
}
