# Behavioral 02 — Observer

## Goal

Let one publisher notify **many** interested parties of an event, without knowing
who they are or what they do — and without them polling. One-to-many, pushed,
decoupled: interested parties register themselves, and the publisher just says
"this happened".

## Why this pattern exists

Start with the smell. The side effects of an event get hard-coded into the method
that raises it:

```
void placeOrder(Order order) {
    emailService.sendConfirmation(order);
    inventoryService.reserve(order);
    analyticsService.track("order_placed", order);
    loyaltyService.awardPoints(order);
    recommendationService.refresh(order.userId());
}
```

Every new side effect edits `placeOrder`. The order code now depends on every
downstream service (violates **Dependency Inversion**), adding or removing behaviour
at runtime is impossible, and testing one path means stubbing all of them.

Observer **inverts the dependency**: the downstream services depend on the event,
not the other way around. They subscribe; the publisher publishes and stays ignorant
of them.

## How it works

Three roles:

- **Subject** (`EventBus`) — holds the set of observers, and on `publish` notifies
  each one.
- **Observer** (`EventListener`) — one method, `onEvent`. The subject depends only
  on this interface.
- **Subscription handle** — what a subscriber gets back, so it can leave.

```
     subscribe(listener) ──▶ EventBus ──publish(event)──▶ for each active listener: listener.onEvent(event)
                                 ▲
   listener          ◀──────────┘ (the bus holds only EventListener references;
                                   it never sees EmailService, InventoryService, …)
```

The publisher becomes **open for extension, closed for modification**: a new
side effect is a new subscriber, not an edit to `placeOrder`.

**Push vs pull.** Observer is *push*: the subject decides when to notify. The
alternative — consumers poll ("anything new?") — trades immediacy for coupling and
wasted work. Push is the natural fit when the event is infrequent and latency
matters; pull returns when the consumer's rate is unknown (see backpressure below).

## The spec

Seams given:

```java
@FunctionalInterface
public interface EventListener<T> { void onEvent(T event); }

public interface Subscription { void cancel(); boolean isActive(); }
```

Implement **`EventBus<T>`**:

- `Subscription subscribe(EventListener<T> listener)` — reject null
  (`NullPointerException`). Subscribing the same listener twice creates **two**
  independent subscriptions.
- `void publish(T event)` — notify every **currently active** subscriber, in
  subscription order, each **exactly once**. If a listener throws, the others must
  still be notified and the exception must **not** propagate out of `publish`.
- `int subscriberCount()` — number of active subscriptions.
- All three must be safe to call **concurrently**. A subscriber added or cancelled
  during a `publish` may or may not see the in-flight event, but must never cause an
  exception.

`cancel()` is idempotent; after it, `isActive()` is `false` and no future event is
delivered to that subscriber.

## Production lens

Observer is the first pattern here where concurrency is *the* story.

- **Never iterate a mutating subscriber list.** A plain `ArrayList` under
  concurrent subscribe/cancel throws `ConcurrentModificationException` mid-publish.
  Iterate a **snapshot** (copy-on-write, or copy before iterating). This is a
  locking-vs-copying decision and the heart of the pattern's thread-safety; see the
  playbook's *Concurrency & locking*.
- **Fault isolation.** One flaky listener must not take down delivery for everyone.
  Catch per-listener. The design choice is what to do with the caught failure:
  swallow-and-log (chosen here), forward to an error handler, or collect and throw an
  aggregate after notifying all. Swallowing silently is the pitfall; in production
  you would forward to metrics/a dead-letter sink.
- **Ordering.** Subscription order is honoured here, but do not build correctness on
  cross-observer ordering — observers are independent, and once delivery is
  concurrent (below) any global order is lost.
- **Async delivery and backpressure.** Real event buses do not run observers on the
  publishing thread; they hand off to a thread pool. That immediately raises the
  questions this synchronous bus dodges: a **bounded** queue (unbounded ⇒ memory
  blow-up under load), **backpressure** when the consumer lags, **at-least-once vs
  at-most-once** delivery, retries with backoff, and a dead-letter queue. See the
  playbook's *Backpressure* and *Idempotency & retries*.
- **Memory leaks — the classic Observer bug.** A long-lived subject holds **strong**
  references to its observers. Forget to `cancel()` and the observer is never
  collected (leak) and keeps receiving events after its owner is gone (ghost
  callbacks). Mitigations: cancel in `finally` / `close()`, scope the subscription's
  lifetime to the observer's, or use weak references when appropriate.
- **Idempotent cancel** matters because cancellation often happens on error paths
  that can run twice; a second `cancel()` must be harmless.

## Trade-offs & when NOT to use it

- **Hidden control flow.** After a `publish`, arbitrarily many unknown listeners run.
  Debugging "why did this side effect happen?" gets harder; the call graph is no
  longer local. This is the price of decoupling.
- **Non-determinism at the edges.** Order and timing of observers is not a contract;
  never encode business rules that depend on observer A running before observer B.
- **Latency on the hot path.** Synchronous observers add their cost to the
  publisher's request. Move heavy work off the request thread.
- **Not a substitute for durable messaging.** If the side effect must survive a
  crash, be retried, or be delivered exactly once, you want an outbox/queue, not an
  in-process observer.
- **Confused with Mediator.** Mediator **centralises** many-to-many interaction
  (objects talk *through* it); Observer is a one-to-many **broadcast** with no
  central logic.
- **Confused with Pub/Sub messaging.** The distributed descendant (topics, brokers,
  multiple processes) is a different beast. Observer is its in-process core: one
  subject, many listeners, same address space.

## How it shows up in real backends

- Java: `java.beans.PropertyChangeListener`, Swing/AWT listeners, and most
  relevantly `java.util.concurrent.Flow` (the Reactive Streams interfaces — Observer
  *with* backpressure).
- **Spring**: `ApplicationEventPublisher` / `@EventListener` — publish a domain
  event, let `@EventListener` beans react.
- Reactive libraries (Reactor/RxJava) are Observer generalised with backpressure and
  operators.
- Real message brokers (Kafka, SQS, RabbitMQ) are the distributed form.

## Where it lives

```
src/main/java/com/lld/patterns/behavioral/observer/
    EventListener.java   ← seam (given)
    Subscription.java    ← seam (given)
    EventBus.java        ← stub to implement (the Subject)

src/test/java/com/lld/patterns/behavioral/observer/
    ObserverTest.java            ← behaviour (the spec)
    ObserverConcurrencyTest.java ← fan-out, concurrent publish, subscribe/cancel churn
```

## How to run the tests

```bash
mvn -Dtest='com.lld.patterns.behavioral.observer.*Test' test
```

Or one class at a time:

```bash
mvn -Dtest=com.lld.patterns.behavioral.observer.ObserverTest test
mvn -Dtest=com.lld.patterns.behavioral.observer.ObserverConcurrencyTest test
```
