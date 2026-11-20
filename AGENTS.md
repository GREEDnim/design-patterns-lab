# Project instructions

A Java 21 + JUnit 5 lab for the **classic design patterns**, grouped into the three
families: **creational**, **structural**, and **behavioral** (plus a few
backend-flavoured patterns folded into the closest family). Each pattern is a
problem: a write-up that teaches it, the seam and stubs you implement, and tests
that are the spec. The learner writes the implementations; the tests are the spec.

Companion to `rate-limiter-lab`. Same tools, same habits.

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

## The role of the seam and stubs

- The **seam** is the small interface the pattern revolves around (e.g.
  `DiscountStrategy`). It is given; do not change it per concrete implementation.
- **Stubs** for the classes the learner must write are provided and their bodies
  throw `UnsupportedOperationException`. Replace the bodies; keep the signatures.
  Do not weaken the seam to make a stub fit.
- Write-ups must **not** contain solution code. A pseudocode snippet that states a
  rule is fine, but it does not replace the explanation.

## Writing a pattern write-up

A write-up is a **guide that teaches the pattern, not a spec sheet**. Answer **how
it works** and **why it exists** — not just what to type. Roughly this order:

- **Goal** — what the pattern lets you do, in plain language.
- **Why the pattern exists** — the smell it removes, shown concretely.
- **How it works** — the mental model: the roles, how they collaborate, and why the
  arrangement produces the benefit. Derive, don't assert.
- **The spec** — the seam, the classes to write, and the exact contract.
- **Production lens** — concurrency/locking, scaling, failure, memory,
  observability. **Calibrated per pattern:** go deep where it is real, one line
  where it is thin, and say "N/A — and here is why" where it genuinely does not
  apply. Do not bolt locking onto a pattern that has no shared state.
- **Trade-offs & when NOT to use it** — over-engineering, misuse, and the patterns
  it is easily confused with.
- **How it shows up in real backends** — the framework/library surfaces where you
  will actually meet it.
- **Where it lives**, **How to run the tests**.

## Tests

- Two test classes per pattern when concurrency is real:
  `<Pattern>Test` (functional) and `<Pattern>ConcurrencyTest` (thread safety).
  A pattern with no concurrency story gets only the functional class.
- Give every `@Test` a comment saying what it verifies and the failure mode it
  guards against.
- The tests are the spec. Never weaken, delete, or skip an assertion to make an
  implementation pass; if a test looks wrong, raise it instead of papering over it.

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

- **One commit per pattern.** A pattern's write-up, stubs, tests, and its rows in
  the README go together. Never mix two patterns in one commit.
- Scaffolding a pattern (write-up + stubs + red tests) and implementing it may be
  two commits, as in `rate-limiter-lab`; keep them to the one pattern.
- Repo-wide changes (this file, the README layout, the playbook) may stand alone.

## Skills

- `code` — implement exactly what the user asks and nothing else, without touching
  or running tests. Load it with `@code`.
