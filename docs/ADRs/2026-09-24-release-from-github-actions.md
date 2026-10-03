# Release from a manual GitHub Actions workflow, with the version set in the release PR

- **Date:** 2026-09-24
- **Status:** Accepted

## Context

Releases were cut on a maintainer's laptop with `maven-release-plugin`: `master` carried
`X.Y.Z-SNAPSHOT`, and `./mvnw release:prepare` rewrote the five POMs, pushed two
`[maven-release-plugin]` commits and the `vault-parent-X.Y.Z` tag to `master`, then
`release:perform` deployed the tag. The org rulesets now require every change to `master`
to go through a pull request, with no bypass, so `release:prepare` can't push its commits
(3.2.11 and 3.2.12 were already released from a hand-set version instead). The steps
also depended on one person's GPG keyring and Central token, and GitHub releases stopped
being created after 3.2.6.

## Decision

- The release PR sets the version in all five POMs with `.buildscript/set-version.sh X.Y.Z`
  (`versions:set`). `master` stays on the released version until the next release PR; it
  no longer carries `-SNAPSHOT`.
- A maintainer starts `.github/workflows/release.yml` by hand on `master` and types the
  version. It validates, runs `test.yml`, then `.buildscript/release.sh` deploys with
  `-Prelease -DautoPublish=true -DwaitUntil=PUBLISHED`, tags `vault-parent-X.Y.Z` and creates
  the GitHub release (title `X.Y.Z`, notes from `CHANGELOG.md`).
- Signing uses `maven-gpg-plugin` 3.2's in-process signer (`-Dgpg.signer=bc`) with the key
  in the `maven-central` environment's secrets, so CI needs no gpg keyring.
- contentful.java and contentful-management.java use the same script and workflow.

## Consequences

- No bot or bypass needs write access to `master`; CI only pushes a tag.
- Maven Central is published before the tag, so a tag always means the version is on
  Central. A failure after publishing is recovered by re-running the tag and release steps.
- `maven-release-plugin` stays in the POM but is unused.
- `last-module` still has to stay last: `central-publishing-maven-plugin` also uploads the
  bundle when the last reactor module is built.
