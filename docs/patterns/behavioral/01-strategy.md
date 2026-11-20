# Behavioral 01 — Strategy

## Goal

Make an **algorithm interchangeable at runtime** behind one small interface. Here
the algorithm is *how a discount is computed*: the caller asks for a final price and
does not know, or care, which pricing rule is currently in force.

The pattern lets you add a new rule by adding a class — without editing the code
that uses it.

## Why this pattern exists

Start with the smell. Pricing written the obvious way grows into a magnet for every
feature request:

```
long discountCents(Order order) {
    if (order.type() == FESTIVE) {
        return order.subtotal() * 20 / 100;
    } else if (order.type() == BULK) {
        return Math.min(order.subtotal(), 5_000);
    } else if (order.type() == LOYAL) {
        ...
    } else if (order.type() == FIRST_ORDER) {
        ...
    }
    return 0;
}
```

Every new promotion edits this one method. It is a single reason for the whole
system to change (violates **SRP**), adding a rule means modifying tested code
(violates **Open–Closed**), the branches cannot be tested in isolation, and the
method has to know about every discount that will ever exist.

The Strategy pattern removes the branching by **making each algorithm its own
object** behind a common interface, and letting the caller hold one.

## How it works

Three roles:

- **Strategy (the seam):** `DiscountStrategy` — one method, `discountCents(subtotal)`.
  This is the only thing callers depend on.
- **Concrete strategies:** `NoDiscount`, `PercentageDiscount`, `FlatDiscount`,
  `TieredDiscount` — one algorithm each.
- **Context:** `PriceCalculator` — holds a strategy and delegates to it. It knows
  *when* to compute a price; the strategy knows *how*.

```
   PriceCalculator ──holds──▶ DiscountStrategy ◀──implements── PercentageDiscount
        │                          (seam)                      FlatDiscount
        │                                                            TieredDiscount
        └─ finalPriceCents(subtotal) delegates to strategy.discountCents(subtotal)
```

The key move is **composition over inheritance**. The alternative — a subclass of
`PriceCalculator` per rule — explodes combinatorially the moment pricing and, say,
tax both vary independently. Holding a strategy keeps the two axes orthogonal: the
context composes with any strategy instead of inheriting from one.

The seam being a **single pure function** is what unlocks the production story:
strategies carry no mutable state, so one instance is safe to share across every
thread and can be swapped at runtime.

**Choosing** which strategy to use is a *separate* concern. Do not bake a `switch`
back into the context — selection belongs to a registry or a factory (see the
Factory Method write-up).

## The spec

The seam is given:

```java
public interface DiscountStrategy {
    long discountCents(long subtotalCents);   // contract: 0 <= result <= subtotalCents
}
```

Implement these. Exact names and signatures matter — the tests use them.

- **`NoDiscount`** — the null strategy. Always returns `0`.
- **`PercentageDiscount(int percent)`** — `floor(subtotal * percent / 100)`.
  Reject `percent` outside `0..100` (inclusive) with `IllegalArgumentException`.
- **`FlatDiscount(long amountCents)`** — subtract a fixed amount, **capped at the
  subtotal** (discount can never exceed `subtotal`, so the price never goes
  negative). Reject a negative amount with `IllegalArgumentException`.
- **`TieredDiscount()`** — fixed tiers on the subtotal:
  - `< 10_000` cents → `0%`
  - `10_000 .. 49_999` → `5%`
  - `>= 50_000` → `10%`

  Percentages floor to whole cents, as above.
- **`PriceCalculator`** — the context:
  - `PriceCalculator(DiscountStrategy strategy)` — `NullPointerException` on null.
  - `void setStrategy(DiscountStrategy strategy)` — swap at runtime; `NullPointerException`
    on null.
  - `long finalPriceCents(long subtotalCents)` — reject a negative subtotal with
    `IllegalArgumentException`; otherwise return `subtotal - discount`, **clamping
    the discount into `[0, subtotal]`** so a strategy that breaks its contract can
    never return a negative price or one above the subtotal.

## Production lens

This is where Strategy earns its keep in real backends.

- **Statelessness is the whole game.** Because a strategy is a pure function of its
  inputs, it needs **no locks**. Put configuration (the percent, the flat amount) in
  `final` fields and keep the object immutable. A stateless object is thread-safe
  for free.
- **Sharing one instance.** DI containers typically register strategies as
  singletons and inject them into many request threads. That is only safe *because*
  they are stateless. The moment someone adds a mutable field ("cache the last
  discount here") you have introduced a data race. The concurrency test exists to
  catch exactly that.
- **Hot-swap needs safe publication.** Swapping the context's strategy at runtime
  means one thread writes the field while others read it. Make it `volatile` (or an
  `AtomicReference`); otherwise a reader can observe a stale or torn value.
- **No per-request allocation.** Reuse strategy instances; do not `new` one per
  call. They are cheap to share and expensive to keep making in the hot path.
- **When a "strategy" seems to need state:** it does not — pass the varying input as
  a method parameter (that is why the method takes `subtotal`), or build a
  throwaway instance per request. Never smuggle per-request state into a shared
  field.
- **Getting the strategy is not the context's job.** Keep a
  `Map<String, DiscountStrategy>` registry populated at startup by a factory. The
  context stays a two-line delegation.
- **Observability.** Emit a metric tagged with the strategy's identity, or wrap
  strategies with a metrics decorator (that wrapping is the **Decorator** pattern) —
  do not sprinkle logging through the pricing logic.

## Trade-offs & when NOT to use it

- **Over-engineering.** Two fixed rules that will never change are fine as a
  `switch`. An interface plus four classes for two stable cases is ceremony.
- **Indirection cost.** Every strategy adds a type; the call path becomes
  `context → interface → impl`. For trivial logic that is harder to read, not
  easier.
- **A lambda is a strategy.** The seam is a `@FunctionalInterface`, so a throwaway
  rule is `subtotal -> subtotal / 4` with no class at all. Use a named class only
  when the rule is reused or worth naming.
- **Confused with State.** Same shape, different intent: a **State** object changes
  *its own* state (and therefore behaviour) over time as a consequence of use;
  a **Strategy** is chosen *externally* and does not change on its own. If the
  object mutates which algorithm runs as a side effect of a call, you want State.
- **Confused with Template Method.** Template Method varies *steps* through
  inheritance; Strategy varies them through composition. Prefer Strategy — it does
  not break the context's encapsulation.
- **If the variants are only data**, a single parameterised class
  (`PercentageDiscount`) beats N near-identical classes. Do not turn a table into a
  class hierarchy.

## How it shows up in real backends

- `java.util.Comparator` — a sorting strategy passed to `List.sort`.
- `java.util.function.*` — every functional interface is a Strategy seam; every
  lambda you pass *is* one.
- Retry/backoff policies, load-balancing and routing policies, compression codecs,
  payment methods, tax rules, cache-eviction policies.
- Spring: injecting different beans by qualifier/profile is Strategy at the
  container level.

## Where it lives

```
src/main/java/com/lld/patterns/behavioral/strategy/
    DiscountStrategy.java        ← the seam (given)
    NoDiscount.java              ← stubs to implement
    PercentageDiscount.java
    FlatDiscount.java
    TieredDiscount.java
    PriceCalculator.java         ← the context (stub)

src/test/java/com/lld/patterns/behavioral/strategy/
    StrategyTest.java            ← behaviour (the spec)
    StrategyConcurrencyTest.java ← statelessness, sharing, safe swap
```

The stubbed bodies throw `UnsupportedOperationException`; replace them. Keep the
signatures.

## How to run the tests

Run just this pattern's tests from the repo root:

```bash
mvn -Dtest='com.lld.patterns.behavioral.strategy.*Test' test
```

Or one class at a time:

```bash
mvn -Dtest=com.lld.patterns.behavioral.strategy.StrategyTest test
mvn -Dtest=com.lld.patterns.behavioral.strategy.StrategyConcurrencyTest test
```
