package com.lld.patterns.behavioral.observer;

/**
 * The Observer in the Observer pattern: something that wants to be told when an
 * event happens.
 *
 * <p>Listeners are called by an {@link EventBus} on a {@code publish}. They should
 * be fast and must not throw for control flow — a listener that throws is isolated
 * so it cannot stop the others, but the bus cannot undo a slow one.
 *
 * <p>A functional interface on purpose: a listener is usually a lambda,
 * e.g. {@code emailService::sendConfirmation}.
 */
@FunctionalInterface
public interface EventListener<T> {

    /** React to one event. */
    void onEvent(T event);
}
