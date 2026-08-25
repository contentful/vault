# Contributing

Thanks for looking at Vault. Before you invest time, please read the maintenance note in
[AGENTS.md](AGENTS.md): the last release was `v.3.2.6` (2023-02-01) and the last
non-automated commit on `master` is from the same day. Pull requests are welcome, but
expect review to be slow, and expect to verify your own change locally because the
build/test pipeline in this repository is not running.

## Reporting problems

- Bugs and feature requests: <https://github.com/contentful/vault/issues>
- Questions: the [Contentful community forum](https://support.contentful.com/) or the
  [community Slack](https://www.contentful.com/slack/)
- Anything confidential: file a ticket with
  [Contentful Customer Support](https://www.contentful.com/support/) rather than opening
  a public issue

By participating you agree to the
[Contentful Code of Conduct](https://github.com/contentful-developer-relations/community-code-of-conduct).

## Prerequisites

- **JDK 8.** `java.version` in the root `pom.xml` is `1.7`, and `compiler/pom.xml`
  resolves `sun.jdk:tools` from `${java.home}/../lib/tools.jar`, which only exists on a
  JDK 8. Newer JDKs will fail the build.
- An **Android SDK** with platform 30 (`android.platform` in the root `pom.xml`) if you
  build `tests-integration`, which has `apk` packaging.
- No Maven install needed — use the checked-in wrapper `./mvnw` (Maven 3.9.16).
- No Contentful credentials needed. The integration tests run against an OkHttp
  `MockWebServer` replaying JSON fixtures from
  `tests-integration/src/test/resources/`.

## Working on a change

```bash
./mvnw clean install          # full build, all four modules
./mvnw -pl core test          # runtime tests
./mvnw -pl compiler test      # annotation-processor tests (compile-testing + Truth)
./mvnw -pl tests-integration test   # Robolectric end-to-end tests
```

Branch off `master` (this repository has no `main`). Historic branches use a
`type/short_description` shape — for example `fix/migrate_to_rxjava3`,
`bugfix/add_environment_to_vaultdatabaseexporter`, `feature/sync-config-environments`.
Open the pull request against `master`.

### Things that will bite you

- Do not edit the `<version>` element in any of the five POMs. `maven-release-plugin`
  rewrites them; hand edits create conflicts at release time.
- Do not remove `last-module/` or move it out of last position in the root `<modules>`
  list. It is load-bearing for artifact publishing — see
  `docs/ADRs/2026-08-25-empty-last-module-to-group-staged-artifacts.md`.
- If you add or rename a generated class, the suffix constants in
  `core/src/main/java/com/contentful/vault/Constants.java` and the keep rules in
  `proguard-vault.cfg` both have to change, or consumers will break only after
  minification.
- Match the surrounding style: two-space indent, brace on the same line, `@Override` on
  the signature line, and the Apache 2.0 header at the top of every new Java file.

### Documentation to update alongside code

- `CHANGELOG.md` — hand-maintained, newest version first, one bullet per change prefixed
  `Add:` / `Fix:` / `Change:` / `Update:` / `Remove:`.
- `README.md` — the Maven and Gradle snippets carry a hard-coded version string, so they
  need bumping at release time.
- `ARCHITECTURE.md` — if you change the compiler/runtime contract or the module layout.
- A new record under `docs/ADRs/` if you make a decision a future reader would otherwise
  have to reverse-engineer, plus a line in `docs/ADRs/README.md`.

## Releasing

There is **no semantic-release and no automated release pipeline** here; releases are cut
by hand. The shape of past releases, reconstructed from the history:

1. Update `CHANGELOG.md` and the version strings in `README.md` in a normal commit
   (for example `b23ea4d`, `Update release notes and CHANGELOG.md`).
2. Run `maven-release-plugin`, which produces the two commits you see throughout the
   log — `[maven-release-plugin] prepare release vault-parent-<version>` followed by
   `[maven-release-plugin] prepare for next development iteration` — and the
   `vault-parent-<version>` tag. Publishing goes through
   `nexus-staging-maven-plugin` against `oss.sonatype.org` (server id
   `sonatype-nexus-staging`).
3. Create the GitHub release from the tag.

Note that tag and release names drifted over time: tags are `vault-parent-3.2.6`, while
GitHub release titles range across `v3.2.0`, `v3.2.1`, and `v.3.2.6`. Pick one and say so
if you cut the next release.

Snapshot publishing was wired to Travis via `.buildscript/deploy_snapshot.sh` (Sonatype
snapshots plus a JitPack ping) using the `CI_DEPLOY_USERNAME` / `CI_DEPLOY_PASSWORD`
environment variables from `.buildscript/settings.xml`. That path is dormant along with
the rest of the Travis configuration.
