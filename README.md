# design-patterns-lab

A Java 21 + JUnit 5 lab for the **classic design patterns**, grouped into the three
families: **creational**, **structural**, and **behavioral**.

Each pattern is a problem: a write-up that teaches it, the seam and stubs you
implement, and tests that are the spec. **You write the code; the tests are the
spec.** We build the patterns one at a time, deepest-first, each with a
production lens (concurrency, scaling, failure) calibrated to where it actually
matters.

Companion to `rate-limiter-lab`.

## Problems

### Creational — how objects are created

| # | Pattern | Write-up | Status |
|---|---|---|---|
| 01 | Factory Method | — | planned |
| 02 | Abstract Factory | — | planned |
| 03 | Builder | — | planned |
| 04 | Singleton | — | planned |
| 05 | Prototype | — | planned |
| 06 | Dependency Injection *(backend-flavoured)* | — | planned |

### Structural — how objects are composed

| # | Pattern | Write-up | Status |
|---|---|---|---|
| 01 | Adapter | — | planned |
| 02 | Decorator | — | planned |
| 03 | Facade | — | planned |
| 04 | Proxy | — | planned |
| 05 | Composite | — | planned |
| 06 | Repository *(backend-flavoured)* | — | planned |

### Behavioral — how objects interact and share responsibility

| # | Pattern | Write-up | Status |
|---|---|---|---|
| 01 | Strategy | [docs/patterns/behavioral/01-strategy.md](docs/patterns/behavioral/01-strategy.md) | ✅ implemented |
| 02 | Observer | — | planned |
| 03 | Command | — | planned |
| 04 | State | — | planned |
| 05 | Template Method | — | planned |
| 06 | Chain of Responsibility | — | planned |
| 07 | Iterator | — | planned |
| 08 | Mediator | — | planned |
| 09 | Null Object *(backend-flavoured)* | — | planned |
| 10 | Specification *(backend-flavoured)* | — | planned |

*Read-only (no build here): Bridge, Flyweight, Visitor, Memento — covered as notes
inside the write-ups that neighbour them.*

## How to work a pattern

1. Read its write-up in `docs/patterns/…`.
2. Implement the stubbed classes in `src/main/java/…` — the write-up is the spec.
3. Run its tests until green:
   ```bash
   mvn -Dtest='com.lld.patterns.<family>.<pattern>.*Test' test
   ```
4. Commit the pattern (see `AGENTS.md` for the one-commit-per-pattern rule).

## Run the tests

```bash
mvn test
```

One pattern at a time, for example Strategy:

```bash
mvn -Dtest='com.lld.patterns.behavioral.strategy.*Test' test
```

## Layout

- `docs/patterns/<family>/NN-<pattern>.md` — one teaching write-up per pattern.
- `docs/production-playbook.md` — cross-cutting production topics (concurrency,
  scaling, backpressure, idempotency, caching, observability).
- `src/main/java/com/lld/patterns/<family>/<pattern>/` — the seam, plus stubs to
  implement.
- `src/test/java/com/lld/patterns/<family>/<pattern>/` — the pattern's tests.
- `src/test/java/com/lld/support/` — shared test support, when a pattern needs it.
