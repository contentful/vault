# AGENTS.md

Instructions for coding agents (and new humans) working in this repository.

## What this repository is

Vault is an **Android library that persists Contentful data locally in SQLite**. It is
not a secrets manager and is unrelated to HashiCorp Vault. Two artifacts are published
under the `com.contentful.vault` group id:

- `core` — the runtime: `Vault.java`, the Sync API driver, and the ORM-like query API.
- `compiler` — a `javax.annotation.processing` annotation processor that reads
  `@Space` / `@ContentType` / `@Field` and generates the SQLite schema plus helper
  classes at compile time.

Read [ARCHITECTURE.md](ARCHITECTURE.md) before changing anything that crosses the
compiler/runtime boundary — the generated-class naming contract is shared between them.

## Maintenance status — read this first

Treat this repository as **maintenance-only**. Evidence:

- The most recent release is `v.3.2.6`, tagged 2023-02-01.
- The most recent commit by a human is `4f6a5ed` (`Update README.md`, 2023-02-01).
  Everything on `master` since then is automated dependency bumps from
  `renovate[bot]`, enabled by `6c0c1d3` (`chore: set up Renovate …`, 2026-07-27).
- The only build/test CI in the repo is `.travis.yml`, pointing at Travis CI. The
  README build badge still points at `travis-ci.org`, which no longer serves this
  project. There is no `.github/workflows/` directory.

Consequence: dependency bumps have been merging to `master` **without a compile or
test run**. Org-level checks (CodeQL, the Wiz scanners, Governance Controls) do run on
pull requests, but none of them compile the project. If you change code here, you must
build and test locally — CI will not catch you.

Do not add documentation, comments, or changelog entries that imply active feature
development.

## Ground rules

- The default branch is **`master`**, not `main`.
- `java.version` in `pom.xml` is `1.7` and `compiler/pom.xml` declares a `system`-scope
  dependency on `sun.jdk:tools` at `${java.home}/../lib/tools.jar`. Both require a
  **JDK 8** to build; `jitpack.yml` and `.travis.yml` both pin JDK 8 for this reason.
  Do not "modernise" the JDK without also removing the `tools.jar` dependency and the
  `com.sun.tools.javac` imports in `compiler/src/main/java/.../Processor.java`.
- Do not hand-edit version numbers in the five `pom.xml` files. `maven-release-plugin`
  owns them; see [CONTRIBUTING.md](CONTRIBUTING.md).
- `last-module/` is intentionally an empty Maven module. Do not delete it or reorder
  `<modules>` in the root `pom.xml`. See
  `docs/ADRs/2026-08-25-empty-last-module-to-group-staged-artifacts.md`.
- `CHANGELOG.md` is maintained by hand, newest version first, and the README's
  dependency snippets carry a hard-coded version string. Both need updating at release
  time; neither is generated.

## Commands

Use the checked-in Maven wrapper (`mvnw` pins Maven 3.9.16):

| Task | Command |
| --- | --- |
| Full build and test | `./mvnw clean install` |
| Runtime tests only | `./mvnw -pl core test` |
| Annotation-processor tests | `./mvnw -pl compiler test` |
| Integration tests | `./mvnw -pl tests-integration test` |

Integration tests use Robolectric plus an OkHttp `MockWebServer` and replay JSON
fixtures from `tests-integration/src/test/resources/` — see
`tests-integration/src/test/java/com/contentful/vaultintegration/BaseTest.java`. **No
Contentful credentials are needed to run the test suite.**

`tests-integration` has `<packaging>apk</packaging>` and builds through
`android-maven-plugin`, so a full `install` needs an Android SDK with platform 30
(`android.platform` in the root `pom.xml`).

## Conventions

- Two-space indentation in Java and in the POMs; opening brace on the same line;
  `@Override` on the same line as the method signature (see `Vault.java`).
- Every Java source file starts with the Apache 2.0 header block.
- `Sql.escape(...)` wraps identifiers in backticks. Field ids are escaped by the
  generator, but caller-supplied `where(...)` clauses are not — see the reserved-keyword
  note in the README.
- Commit subjects on `master` use a loose `type: subject` form (`fix:`, `chore:`,
  `polish:`, `docs:`). There is no semantic-release here; releases are cut manually.
