#!/bin/bash
# Release to Maven Central. The GitHub Actions workflow .github/workflows/release.yml runs the same
# steps as a manual release. See RELEASING.md.
#
# Usage: .buildscript/release.sh <step>
#
#   validate        pom.xml has a release version (no -SNAPSHOT) with a CHANGELOG.md entry; the version
#                   is not tagged, is greater than the last tag and is not on Maven Central; the commit
#                   is on origin/master; the working tree is clean
#   deploy          build, sign, upload to the Central Portal, publish, and wait until Central reports
#                   PUBLISHED (skipped if the version is already on Maven Central)
#   tag             tag RELEASE_COMMIT (default: HEAD) and push only that tag
#   github-release  create the GitHub release with the version's CHANGELOG.md section as notes
#   all             validate, deploy, tag, github-release
#
# Environment:
#   DRY_RUN=1             build and sign the bundle without uploading it (skipPublishing), and print the
#                         tag push and the release command instead of running them. Also allows validating
#                         a commit that is not on master.
#   EXPECTED_VERSION      fail unless pom.xml has this version (the workflow's confirmation input)
#   RELEASE_COMMIT        commit to tag (CI passes $GITHUB_SHA); defaults to HEAD
#   MAVEN_GPG_KEY         ASCII-armored private signing key (CI). Unset: your local gpg keyring signs.
#   MAVEN_GPG_PASSPHRASE  passphrase of that key
#   GH_TOKEN              GitHub token for gh (CI); otherwise your gh login is used
#   GIT_REMOTE            remote to fetch from and push to (default: origin)
#
# The Central Portal user token comes from the <server> with id "central" in ~/.m2/settings.xml
# (setup-java writes it on CI).

set -euo pipefail

cd "$(dirname "$0")/.."

# --- Repository configuration -------------------------------------------------------------------
REPO_SLUG="contentful/vault"
GROUP_PATH="com/contentful/vault"   # groupId with slashes
ARTIFACTS="core compiler"          # artifacts that must appear on Maven Central
TAG_PREFIX="vault-parent-"         # tags look like vault-parent-3.2.12
MAVEN_ARGS=(-Prelease)             # signing, sources, javadoc and publishing live in the release profile
# -------------------------------------------------------------------------------------------------

GIT_REMOTE="${GIT_REMOTE:-origin}"
DRY_RUN="${DRY_RUN:-0}"

# The project's own <version>, ignoring the <parent> block.
VERSION="$(sed -n -e '/<parent>/,/<\/parent>/d' -e '/<version>/{s/.*<version>\(.*\)<\/version>.*/\1/p;q;}' pom.xml)"
TAG="$TAG_PREFIX$VERSION"

log()  { printf '\n==> %s\n' "$*"; }
fail() { printf 'ERROR: %s\n' "$*" >&2; exit 1; }

# Runs a command that changes remote state, or prints it when DRY_RUN=1.
publish() {
  if [[ "$DRY_RUN" == "1" ]]; then
    printf '[dry-run] %s\n' "$*"
  else
    "$@"
  fi
}

remote_tag_sha() {
  local refs
  refs="$(git ls-remote "$GIT_REMOTE" "refs/tags/$TAG^{}" "refs/tags/$TAG")" \
    || fail "Could not list the tags on $GIT_REMOTE"
  awk 'NR==1{print $1}' <<<"$refs"
}

# Prints the artifacts of this version that are already on Maven Central.
on_central() {
  local artifact
  for artifact in $ARTIFACTS; do
    if [[ "$(curl -s -o /dev/null -w '%{http_code}' \
          "https://repo1.maven.org/maven2/$GROUP_PATH/$artifact/$VERSION/$artifact-$VERSION.pom")" == "200" ]]; then
      echo "$artifact"
    fi
  done
}

# The CHANGELOG.md section of VERSION, without its heading.
changelog_section() {
  awk -v heading="## Version [$VERSION]" '
    index($0, heading) == 1 { found = 1; next }
    found && /^## /         { exit }
    found                   { print }
  ' CHANGELOG.md | sed -e '/./,$!d'
}

step_validate() {
  log "Validating release $VERSION"

  [[ "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] \
    || fail "pom.xml has version '$VERSION'. A release needs X.Y.Z without -SNAPSHOT. Run .buildscript/set-version.sh X.Y.Z in a PR"
  if [[ -n "${EXPECTED_VERSION:-}" && "$EXPECTED_VERSION" != "$VERSION" ]]; then
    fail "You asked to release $EXPECTED_VERSION but pom.xml has $VERSION"
  fi

  [[ -n "$(changelog_section)" ]] \
    || fail "CHANGELOG.md has no '## Version [$VERSION]' section (or it is empty)"

  [[ -z "$(git status --porcelain --untracked-files=no)" ]] \
    || fail "Working tree has uncommitted changes to tracked files"

  git fetch --quiet --tags --force "$GIT_REMOTE"
  [[ -z "$(remote_tag_sha)" ]] || fail "Tag $TAG already exists on $GIT_REMOTE"

  local latest
  latest="$(git tag -l "$TAG_PREFIX[0-9]*.[0-9]*.[0-9]*" | sed "s/^$TAG_PREFIX//" | grep -E '^[0-9]+\.[0-9]+\.[0-9]+$' | sort -V | tail -1 || true)"
  if [[ -n "$latest" ]]; then
    [[ "$(printf '%s\n%s\n' "$latest" "$VERSION" | sort -V | tail -1)" == "$VERSION" && "$latest" != "$VERSION" ]] \
      || fail "$VERSION is not greater than the latest tag $TAG_PREFIX$latest"
  fi

  local published
  published="$(on_central)"
  [[ -z "$published" ]] || fail "$VERSION is already on Maven Central ($(echo $published)). Bump the version"

  local commit
  commit="$(git rev-parse "${RELEASE_COMMIT:-HEAD}^{commit}")"
  git fetch --quiet "$GIT_REMOTE" master
  if ! git merge-base --is-ancestor "$commit" "$GIT_REMOTE/master"; then
    [[ "$DRY_RUN" == "1" ]] || fail "Commit $commit is not on $GIT_REMOTE/master. Releases are made from master only"
    echo "WARNING: $commit is not on $GIT_REMOTE/master (allowed for a dry run only)"
  fi

  echo "OK: $VERSION (previous: ${latest:-none}) from $commit, tag $TAG"
}

step_deploy() {
  log "Deploying $VERSION to Maven Central"

  local published
  published="$(on_central)"
  if [[ -n "$published" && "$(echo $published)" == "$(echo $ARTIFACTS)" ]]; then
    echo "$VERSION is already on Maven Central, nothing to do"
    return
  fi

  local args=(-B clean deploy -DskipTests "${MAVEN_ARGS[@]+"${MAVEN_ARGS[@]}"}"
    "-DdeploymentName=${ARTIFACTS// /, } $VERSION"
    -DautoPublish=true -DwaitUntil=PUBLISHED -DwaitMaxTime=3600)

  if [[ -n "${MAVEN_GPG_KEY:-}" ]]; then
    # Sign in-process with the key from the environment; no gpg keyring or agent needed.
    args+=(-Dgpg.signer=bc)
  elif [[ "$DRY_RUN" == "1" ]] && ! gpg --list-secret-keys 2>/dev/null | grep -q '^sec'; then
    echo "WARNING: no signing key available, the dry run skips signing"
    args+=(-Dgpg.skip=true)
  fi

  if [[ "$DRY_RUN" == "1" ]]; then
    args+=(-DskipPublishing=true)
    echo "[dry-run] the bundle is built and signed but not uploaded"
    # The publishing plugin needs a "central" server entry even when it skips the upload.
    if ! grep -qs '<id>central</id>' "$HOME/.m2/settings.xml"; then
      local settings
      settings="$(mktemp)"
      echo '<settings><servers><server><id>central</id><username>dry-run</username><password>dry-run</password></server></servers></settings>' > "$settings"
      args+=(-s "$settings")
    fi
  fi

  ./mvnw "${args[@]}"

  if [[ "$DRY_RUN" == "1" ]]; then
    echo "Signatures of the files that would be uploaded:"
    find . -path '*/target/*' -name '*.asc' | sed 's/^/  /'
  fi
}

step_tag() {
  local commit existing
  commit="$(git rev-parse "${RELEASE_COMMIT:-HEAD}^{commit}")"
  log "Tagging $commit as $TAG"

  existing="$(remote_tag_sha)"
  if [[ -n "$existing" ]]; then
    [[ "$existing" == "$commit" ]] || fail "Tag $TAG already exists on a different commit ($existing)"
    echo "Tag $TAG already points at $commit, nothing to do"
    return
  fi

  if git rev-parse -q --verify "refs/tags/$TAG" >/dev/null; then
    [[ "$(git rev-parse "refs/tags/$TAG^{commit}")" == "$commit" ]] \
      || fail "A local tag $TAG exists on a different commit. Delete it with: git tag -d $TAG"
  elif [[ "$DRY_RUN" == "1" ]]; then
    printf '[dry-run] git tag %s %s\n' "$TAG" "$commit"
  else
    git tag "$TAG" "$commit"
  fi

  # Push only this tag, never --tags.
  publish git push "$GIT_REMOTE" "refs/tags/$TAG"
}

step_github_release() {
  log "Publishing GitHub release $TAG"
  command -v gh >/dev/null 2>&1 || fail "'gh' is required. Install it with: brew install gh"

  if gh release view "$TAG" --repo "$REPO_SLUG" >/dev/null 2>&1; then
    echo "Release $TAG already exists, nothing to do"
    return
  fi

  local notes
  notes="$(mktemp)"
  changelog_section > "$notes"
  if [[ "$DRY_RUN" == "1" ]]; then
    echo "[dry-run] release notes:"
    sed 's/^/  /' "$notes"
  fi

  publish gh release create "$TAG" \
    --repo "$REPO_SLUG" \
    --title "$VERSION" \
    --notes-file "$notes" \
    --latest \
    --verify-tag
  rm -f "$notes"
}

case "${1:-}" in
  validate)       step_validate ;;
  deploy)         step_deploy ;;
  tag)            step_tag ;;
  github-release) step_github_release ;;
  all)
    step_validate
    step_deploy
    step_tag
    step_github_release
    if [[ "$DRY_RUN" == "1" ]]; then
      log "Dry run of $VERSION finished. Nothing was uploaded or pushed"
    else
      log "$REPO_SLUG $VERSION released"
    fi
    ;;
  *)
    sed -n '2,29p' "$0" | sed 's/^# \{0,1\}//'
    exit 1
    ;;
esac
