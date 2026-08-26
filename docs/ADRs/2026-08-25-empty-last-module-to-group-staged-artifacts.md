# Keep an empty `last-module` Maven module to group staged artifacts

- **Date:** 2026-08-25
- **Status:** Accepted (documenting existing behaviour)

> This record was written on 2026-08-25 from the commit history. It documents a decision
> made on 2017-08-24; it is not a new decision, and the rationale below is reconstructed
> from the commits and `CHANGELOG.md` rather than from a contemporaneous write-up.

## Context

Vault publishes two artifacts, `com.contentful.vault:core` and
`com.contentful.vault:compiler`, from one multi-module Maven build. Publishing goes
through `nexus-staging-maven-plugin`, declared in the root `pom.xml` with
`<extensions>true</extensions>` against `oss.sonatype.org` (`stagingProfileId`
`6c692a1d2981a9`). With extensions enabled, that plugin replaces the normal deploy: each
module's artifacts are staged locally, and the actual upload plus close of the staging
repository is deferred to the **last module in the reactor**.

On 2017-08-24 the release of 3.0.x repeatedly failed to get both artifacts published.
The whole sequence happened inside four hours and is legible in the log:

| Time | Commit | What happened |
| --- | --- | --- |
| 12:28 | `19b76a9` | "Do not publish integration test module" — adds `<skipNexusStagingDeployMojo>true</skipNexusStagingDeployMojo>` and `maven-deploy-plugin` `<skip>true</skip>` to `tests-integration/pom.xml`. |
| 12:53–14:29 | `37a61a2`, `6614800`, `964b504`, `a4a0616`, `2ba9261`, `aa10d60` | Four release attempts for 3.0.2, two rollbacks, one revert, and a server-id change. |
| 15:48 | `eda874c` | "Redefine version 3.0.1 upload" — reworks the deploy configuration and renumbers the release back to 3.0.1, replacing the changelog line "Fix: Use different deploy plugin, fixing previous releases." with "Fix: Upload all artifacts." |
| 16:14 | `5711931` | "Add hidden last module to merge upload" — adds `last-module/pom.xml`, and reorders `<modules>` from `tests-integration, compiler, core` to `compiler, core, tests-integration, last-module`. |
| 16:44 | `c8b8bac` | 3.0.1 is released. |

No commit message states the root cause, so the mechanism below is inferred from the
plugin's deferred-deploy behaviour and the module graph, not quoted from the history.
Maven orders the reactor topologically: `compiler` depends on `core`, and
`tests-integration` depends on both, so `tests-integration` came last regardless of how
`<modules>` was written. `tests-integration` is precisely the module that had just been
configured to skip the staging deploy. With the reactor terminating in a module that
skips it, the deferred upload-and-close never fired and the staged artifacts were never
published — matching the symptom `CHANGELOG.md` records for 3.0.1, "Fix: Upload all
artifacts."

## Decision

Keep an intentionally empty module, `last-module`, and keep it last in the root
`pom.xml`'s `<modules>` list. Its only job is to terminate the reactor with a module that
does *not* skip the staging deploy, so the close-and-release step fires once, after every
publishable artifact has been staged. Because it declares no dependencies, Maven leaves
it in declaration order relative to the other leaf modules — which is why its position in
`<modules>` matters, and why the same commit rewrote that list to match the
dependency-derived order.

`last-module/pom.xml` is deliberately minimal: parent coordinates, `modelVersion`, and
`<artifactId>last-module</artifactId>`. No sources, no plugins. The
`maven-release-plugin` version bumps flow through it like any other module, which is why
it shows up in `git log -- last-module` at every release.

## Consequences

- The `<modules>` order in the root `pom.xml` is functional, not cosmetic. Deleting
  `last-module`, or moving it anywhere but last, can silently stop the staging upload
  again — and that failure shows up only at release time, not at build time.
- An empty module whose name explains nothing is a maintenance trap; anyone tidying the
  build is likely to remove it. That is why this record exists and why `AGENTS.md` and
  `CONTRIBUTING.md` both call it out.
- An empty `last-module` artifact is built and version-bumped on every release. It
  carries no code and is of no use to consumers.
- A cleaner fix would be to configure the staging plugin's deferred deploy directory
  explicitly, or to stage all artifacts in one step from a release workflow. Neither was
  attempted. Neither should be attempted without a real release to test against — the
  build/test pipeline in this repository is currently dormant (see `ARCHITECTURE.md`).

## Evidence

- `5711931b39cb2802cff9a01f1bf1f153cc011d93` — "Add hidden last module to merge upload"
- `19b76a9f353f324a28b75d1253a484a7523ea672` — "Do not publish integration test module"
- `eda874ca10e40461c77285b963790f0d50763fdc` — "Redefine version 3.0.1 upload"
- `last-module/pom.xml`; root `pom.xml` (`<modules>`, `nexus-staging-maven-plugin`);
  `tests-integration/pom.xml` (`skipNexusStagingDeployMojo`)
- `CHANGELOG.md`, entry for version 3.0.1
