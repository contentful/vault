# Architecture

Vault persists Contentful content locally on Android over SQLite. The design splits
into a **compile-time code generator** and a **runtime library** that agree on a naming
contract for the generated classes. Everything the runtime needs about a space's shape
is baked in at compile time; there is no reflection over models at runtime.

## Modules

The root `pom.xml` is a `pom`-packaged aggregator (`com.contentful.vault:vault-parent`)
with four modules, declared in this order:

| Module | Artifact | Role |
| --- | --- | --- |
| `compiler` | `com.contentful.vault:compiler` | Annotation processor. Published. |
| `core` | `com.contentful.vault:core` | Runtime library. Published. |
| `tests-integration` | `tests-integration` | End-to-end tests. `apk` packaging, deploy skipped. |
| `last-module` | `last-module` | Empty by design; see the ADR below. |

`compiler` depends on `core` at `provided` scope for the annotation and constant
definitions, so the ordering above is also the build order. `compiler` sets
`-proc:none` on `maven-compiler-plugin` so it does not run itself over its own sources.

## Compile-time path

`compiler/src/main/java/com/contentful/vault/compiler/Processor.java` is the
`AbstractProcessor`, registered through
`compiler/src/main/resources/META-INF/services/javax.annotation.processing.Processor`.
It claims two annotations, `@ContentType` and `@Space`, and emits one class per target
via JavaPoet (`Injection#brewJava`). The suffixes are the shared contract, defined once
in `core/src/main/java/com/contentful/vault/Constants.java`:

| Generator | Suffix | Contains |
| --- | --- | --- |
| `FieldsInjection` | `$Fields` | Column-name constants for each `@Field`, extending `BaseFields`. |
| `ModelInjection` | `$$ModelHelper` | `fromCursor`, `setField`, table name, `CREATE TABLE` statements. |
| `SpaceInjection` | `$$SpaceHelper` | Space id, db name, `dbVersion`, `copyPath`, locales, model map. |

Those same three suffixes are what `proguard-vault.cfg` keeps, which is why consumers
have to apply that file.

## Runtime path

`core/src/main/java/com/contentful/vault/`:

- `Vault.java` is the entry point. `Vault.with(context, SpaceClass)` looks the
  generated `$$SpaceHelper` up, caches one `SqliteHelper` per space class in a static
  map, and exposes `fetch(...)` / `observe(...)`.
- `SyncRunnable.java` drives the Contentful **Sync API** through the `java-sdk`
  `CDAClient` (`client.sync().fetch()` for an initial sync, `client.sync(token)` for a
  delta), then writes assets, entries, links, and deletions into SQLite. It runs on a
  single-threaded executor (`Vault.EXECUTOR_SYNC`); callbacks are posted back to the
  main looper, and results are also published on an RxJava 3 `PublishSubject`
  (`Vault.observeSyncResults()`).
- `Sql.java` owns the schema. Fixed tables are `entry_types` and `sync_info`; `links`
  and `assets` plus every model table are **created once per locale** via
  `Sql.localizeName(...)`, which is how `.all("tlh")` reads a different locale from the
  same database. `Sql.escape(...)` backticks identifiers.
- `SqliteHelper.java` is the `SQLiteOpenHelper`. When a `copyPath` is set it copies the
  pre-seeded database out of assets before opening (`VaultDatabaseExporter` is the tool
  that produces that file). `onUpgrade` drops and recreates tables — a `dbVersion` bump
  discards local content rather than migrating it.
- `AbsQuery` / `FetchQuery` / `ObserveQuery` are the query builders; `LinkResolver` and
  `QueryResolver` rehydrate links between resources.

## Version stamping

`core` has no `src/main/resources`. Instead `core/src/main/templates/com/contentful/
vault/build/GeneratedBuildParameters.java` is a filtered resource: Maven substitutes
`${project.version}` into it, `build-helper-maven-plugin` adds the output directory as a
source root at `generate-sources`, and `SyncConfig` reads the constant to set the
integration header on the `CDAClient`. This replaced a runtime `vault.properties`
lookup in `8cd61d6`.

## Build and CI

- Maven wrapper `mvnw`, pinned to Maven 3.9.16 in `.mvn/wrapper/maven-wrapper.properties`.
- `java.version` is `1.7`, and `compiler` declares `sun.jdk:tools` at
  `${java.home}/../lib/tools.jar`. Both mean the build needs a **JDK 8**; `jitpack.yml`
  and `.travis.yml` pin JDK 8 accordingly.
- `.travis.yml` is the only build/test pipeline in the repository. It runs the Maven
  build and, on `master`, calls `.buildscript/deploy_snapshot.sh` to publish snapshots
  to Sonatype and ping JitPack. This pipeline is **not active** — the badge in the
  README points at `travis-ci.org`, and there is no `.github/workflows/` directory. The
  checks that do run on pull requests are org-level (CodeQL, Wiz scanners, Governance
  Controls); none of them compile the project. See AGENTS.md for the consequences.
- Release publishing goes through `maven-release-plugin` and
  `nexus-staging-maven-plugin` (`stagingProfileId` `6c692a1d2981a9` on
  `oss.sonatype.org`).

## Decision records

- [`docs/ADRs/2026-08-25-empty-last-module-to-group-staged-artifacts.md`](docs/ADRs/2026-08-25-empty-last-module-to-group-staged-artifacts.md)
  — why `last-module/` exists and must stay last.
