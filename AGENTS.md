# Project instructions

A Java 21 + JUnit 5 lab for the **classic design patterns**, grouped into the three
families: **creational**, **structural**, and **behavioral** (plus a few
backend-flavoured patterns folded into the closest family).

Each pattern ships a **write-up that teaches it** and a **complete, tested reference
implementation**. This is a teaching repo: you read the code, run the tests, and ask
questions — the point is *understanding* the pattern, not re-implementing it.

Companion to `rate-limiter-lab`.

## Layout

- One package per pattern: `com.lld.patterns.<family>.<pattern>` under
  `src/main/java/com/lld/patterns/`. `<family>` is `creational`, `structural`, or
  `behavioral`.
- Write-ups live at `docs/patterns/<family>/NN-<pattern>.md`; keep the problem
  lists in `README.md` in sync.
- Tests mirror the package under `src/test/java/com/lld/patterns/<family>/<pattern>/`.
- Shared test support lives in `src/test/java/com/lld/support/` when needed.
- Cross-cutting production notes live in `docs/production-playbook.md`; reference
  them from write-ups instead of repeating them.

## Teaching through implementations

- Every pattern ships a **complete, working implementation** plus tests. `mvn test`
  is green — the repo is a reference you can read and run, not a set of exercises.
- The **seam** is the small interface the pattern revolves around (e.g.
  `DiscountStrategy`); concrete classes implement it. Keep the seam tiny and stable.
- Implementations are held to production standards: immutability where possible,
  argument validation, deliberate concurrency, and comments that explain *why*.

## Writing a pattern write-up

A write-up is a **guide that teaches the pattern, not a spec sheet**. Answer **how
it works** and **why it exists** — not just what the code says. Roughly this order:

- **Goal** — what the pattern lets you do, in plain language.
- **Why the pattern exists** — the smell it removes, shown concretely.
- **How it works** — the mental model: the roles, how they collaborate, and why the
  arrangement produces the benefit. Derive, don't assert.
- **The spec** — the seam, the classes, and the exact contract.
- **Production lens** — concurrency/locking, scaling, failure, memory,
  observability. **Calibrated per pattern:** go deep where it is real, one line
  where it is thin, and say "N/A — and here is why" where it genuinely does not
  apply. Do not bolt locking onto a pattern that has no shared state.
- **Trade-offs & when NOT to use it** — over-engineering, misuse, and the patterns
  it is easily confused with.
- **How it shows up in real backends** — the framework/library surfaces where you
  will actually meet it.
- **Where it lives**, **How to run the tests**.

Every non-obvious decision in the implementation should be explained in the
write-up (or a code comment): why this data structure, why this lock, why this
trade-off.

## Tests

- Two test classes per pattern when concurrency is real:
  `<Pattern>Test` (functional) and `<Pattern>ConcurrencyTest` (thread safety).
  A pattern with no concurrency story gets only the functional class.
- Give every `@Test` a comment saying what it verifies and the failure mode it
  guards against.
- Tests document the behaviour a reader should expect; never weaken an assertion.

## Build and commands

- JDK 21, Maven, JUnit 5.
- All tests: `mvn test`
- One pattern's tests:
  `mvn -Dtest='com.lld.patterns.<family>.<pattern>.*Test' test`

## Code conventions

- Java: 4-space indent, `com.lld` import grouping, no wildcard imports.
- Validate constructor arguments up front: `IllegalArgumentException` for bad
  values, `NullPointerException` (via `Objects.requireNonNull`) for null objects.
- Prefer immutability. A stateless, immutable object is thread-safe for free —
  that is the point of most of these patterns.
- Inject collaborators; never reach for static/global mutable state.

## Commits

- **One commit per pattern.** A pattern's write-up, reference implementation,
  tests, and its rows in the README go together. Never mix two patterns in one
  commit.
- Repo-wide changes (this file, the README layout, the playbook) may stand alone.
