package com.lld.patterns.behavioral.observer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.Test;

/**
 * Functional behaviour (the spec) for the Observer pattern.
 *
 * <p>Covers delivery, ordering, cancel, fault isolation and the edges. A bus that
 * delivers to the wrong listeners, loses or duplicates events, or lets one listener
 * break the rest fails here.
 */
class ObserverTest {

    /** A subscriber receives every published event, in order. */
    @Test
    void a_subscriber_receives_published_events_in_order() {
        EventBus<String> bus = new EventBus<>();
        List<String> received = new CopyOnWriteArrayList<>();
        bus.subscribe(received::add);

        bus.publish("a");
        bus.publish("b");
        bus.publish("c");

        assertEquals(List.of("a", "b", "c"), received);
    }

    /** All subscribers are notified, in subscription order, for a single event. */
    @Test
    void notifies_every_subscriber_in_subscription_order() {
        EventBus<String> bus = new EventBus<>();
        List<String> order = new CopyOnWriteArrayList<>();
        bus.subscribe(e -> order.add("first:" + e));
        bus.subscribe(e -> order.add("second:" + e));
        bus.subscribe(e -> order.add("third:" + e));

        bus.publish("x");

        assertEquals(List.of("first:x", "second:x", "third:x"), order);
    }

    /** Cancelling a subscription stops future delivery but does not affect the past. */
    @Test
    void cancelling_a_subscription_stops_delivery() {
        EventBus<String> bus = new EventBus<>();
        List<String> received = new CopyOnWriteArrayList<>();
        Subscription subscription = bus.subscribe(received::add);

        bus.publish("a");
        subscription.cancel();
        bus.publish("b");

        assertEquals(List.of("a"), received, "only the pre-cancel event is delivered");
    }

    /** Cancelling never removes a *different* subscriber (no accidental over-removal). */
    @Test
    void cancelling_one_subscription_leaves_the_others_active() {
        EventBus<String> bus = new EventBus<>();
        List<String> kept = new CopyOnWriteArrayList<>();
        List<String> cancelled = new CopyOnWriteArrayList<>();
        Subscription subscription = bus.subscribe(cancelled::add);
        bus.subscribe(kept::add);

        subscription.cancel();
        bus.publish("x");

        assertEquals(List.of("x"), kept);
        assertTrue(cancelled.isEmpty());
    }

    /** cancel is idempotent and isActive tracks the state. */
    @Test
    void cancel_is_idempotent_and_isActive_reflects_state() {
        EventBus<String> bus = new EventBus<>();
        Subscription subscription = bus.subscribe(e -> { });

        assertTrue(subscription.isActive());
        subscription.cancel();
        assertFalse(subscription.isActive());
        assertDoesNotThrow(subscription::cancel, "a second cancel must be a no-op");
    }

    /** subscriberCount reflects active subscriptions as they come and go. */
    @Test
    void subscriber_count_reflects_active_subscriptions() {
        EventBus<String> bus = new EventBus<>();
        assertEquals(0, bus.subscriberCount());

        Subscription a = bus.subscribe(e -> { });
        Subscription b = bus.subscribe(e -> { });
        assertEquals(2, bus.subscriberCount());

        a.cancel();
        assertEquals(1, bus.subscriberCount());

        b.cancel();
        assertEquals(0, bus.subscriberCount());
    }

    /** Subscribing the same listener twice yields two deliveries, independently cancellable. */
    @Test
    void the_same_listener_can_subscribe_more_than_once() {
        EventBus<String> bus = new EventBus<>();
        List<String> received = new CopyOnWriteArrayList<>();
        EventListener<String> listener = received::add;

        Subscription first = bus.subscribe(listener);
        bus.subscribe(listener);
        bus.publish("x");
        assertEquals(List.of("x", "x"), received);

        first.cancel();
        bus.publish("y");
        assertEquals(List.of("x", "x", "y"), received, "one of the two subscriptions remains");
    }

    /**
     * Fault isolation: a listener that throws must not stop the others, and must not
     * let its exception escape publish. This is the difference between a bus and a
     * naive for-loop over the callbacks.
     */
    @Test
    void a_failing_listener_does_not_stop_the_others() {
        EventBus<String> bus = new EventBus<>();
        List<String> received = new CopyOnWriteArrayList<>();
        bus.subscribe(e -> { throw new RuntimeException("boom"); });
        bus.subscribe(received::add);
        bus.subscribe(e -> { throw new IllegalStateException("bang"); });

        assertDoesNotThrow(() -> bus.publish("x"), "listener failures must not escape publish");
        assertEquals(List.of("x"), received, "the healthy listener still got the event");
    }

    /** Publishing with no subscribers is a no-op, not an error. */
    @Test
    void publishing_with_no_subscribers_is_a_no_op() {
        EventBus<String> bus = new EventBus<>();
        assertDoesNotThrow(() -> bus.publish("x"));
    }

    /** A null listener is rejected up front. */
    @Test
    void rejects_a_null_listener() {
        EventBus<String> bus = new EventBus<>();
        assertThrows(NullPointerException.class, () -> bus.subscribe(null));
    }
}
