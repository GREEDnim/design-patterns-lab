# design-patterns-lab

A Java 21 + JUnit 5 lab for the **classic design patterns**, grouped into the three
families: **creational**, **structural**, and **behavioral**.

Each pattern ships a **write-up that teaches it** and a **complete, tested reference
implementation**. This is a **teaching repo**: read the code, run the tests, ask
questions — the goal is understanding each pattern and its production lens
(concurrency, scaling, failure), not re-implementing it.

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
| 01 | Strategy | [docs/patterns/behavioral/01-strategy.md](docs/patterns/behavioral/01-strategy.md) | ✅ done |
| 02 | Observer | [docs/patterns/behavioral/02-observer.md](docs/patterns/behavioral/02-observer.md) | ✅ done |
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

## How to read a pattern

1. Read the write-up in `docs/patterns/…` — it teaches the pattern and its
   production lens.
2. Read the reference implementation in `src/main/java/…`.
3. Run its tests and watch the behaviour:
   ```bash
   mvn -Dtest='com.lld.patterns.<family>.<pattern>.*Test' test
   ```

## Run the tests

```bash
mvn test
```

One pattern at a time, for example Observer:

```bash
mvn -Dtest='com.lld.patterns.behavioral.observer.*Test' test
```

## Layout

- `docs/patterns/<family>/NN-<pattern>.md` — one teaching write-up per pattern.
- `docs/production-playbook.md` — cross-cutting production topics (concurrency,
  scaling, backpressure, idempotency, caching, observability).
- `src/main/java/com/lld/patterns/<family>/<pattern>/` — the seam and the reference
  implementation.
- `src/test/java/com/lld/patterns/<family>/<pattern>/` — the pattern's tests.
- `src/test/java/com/lld/support/` — shared test support, when a pattern needs it.
