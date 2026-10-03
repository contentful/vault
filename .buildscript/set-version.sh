#!/bin/bash
# Sets the release version in every pom.xml of the build. Run it in the release PR.
#
# Usage: .buildscript/set-version.sh X.Y.Z

set -euo pipefail

cd "$(dirname "$0")/.."

VERSION="${1:-}"
[[ "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || { echo "Usage: $0 X.Y.Z" >&2; exit 1; }

./mvnw -B -q versions:set -DnewVersion="$VERSION" -DprocessAllModules=true -DgenerateBackupPoms=false
git status --short -- '*pom.xml'
