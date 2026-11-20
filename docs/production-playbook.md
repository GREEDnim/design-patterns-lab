# Production playbook

Cross-cutting concerns that show up across many patterns. Pattern write-ups link
here instead of repeating themselves. This grows as we go.

## Concurrency & locking

- **Prefer immutability and statelessness.** A stateless, immutable object is
  thread-safe with no locks at all. Most patterns here are designed to reach that
  state — reach for it first.
- **Lock only mutable shared state**, and only for the smallest critical section.
  A whole-method `synchronized` on a class serving many callers is usually wrong.
- **Lock striping / per-key locks** beat one global lock when independent units
  (per client, per topic, per key) can proceed in parallel.
- **Safe publication matters even without locks.** A field swapped at runtime and
  read by other threads must be `volatile`, an `AtomicReference`, or replaced under
  a lock; otherwise a reader may see a stale or torn value.
- **Forward-only state transitions** under concurrency: when threads read a clock
  or counter at slightly different instants, compare so stale callers cannot rewind
  state (`>` not `!=`).

## Scaling

- **Share one immutable instance** across threads and requests; never allocate a
  fresh strategy/policy per call.
- **Move the bottleneck out of the hot path**: cache, batch, or shard before adding
  threads.
- **Know the admission control** at the edge: rate limiting and backpressure belong
  at boundaries, not scattered through business code.

## Backpressure

- When a producer outpaces a consumer, the buffer grows until something collapses.
  Backpressure is the consumer telling the producer "fewer, please" — pull instead
  of push, shrink batch sizes on failure, or reject early.
- With **push**, backpressure means retry later; with **pull**, it means the
  consumer simply polls slower. Pick the delivery model deliberately.

## Idempotency & retries

- Any delivery that can be retried (at-least-once) must be **idempotent**: a retried
  unit of work must not apply twice. Carry an idempotency key.
- Retries need **bounded attempts + exponential backoff + jitter**, and a terminal
  sink (dead-letter queue) so poison messages leave the loop.

## Caching

- Caches trade staleness for speed. Decide the invalidation policy up front (TTL,
  write-through, write-behind, explicit bust).
- Cache the **immutable** result, keyed on everything that can change it.

## Observability

- Emit a metric tagged by the concrete choice (which strategy, which codec, which
  route). Wrapping a component to add metrics/logging is itself a pattern
  (Decorator / Proxy) — do not sprinkle logging through the core logic.
