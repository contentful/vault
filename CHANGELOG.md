# Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

## Version [0.9.1][unreleased] - (in development)
- New: `Asset` is now `Parcelable`.
- New: Optional `SyncConfig` setting that causes invalidation of existing data upon sync.
- New: Provide direct read-only access to database objects.
- Changed: Use empty collections instead of null for empty arrays / maps.
- Changed: Use contentful.java v2.0.3.
- Fixed: Link resolution for arrays of resources now works correctly.
- Fixed: Removed incorrect calls to `close()` on database objects.

## Version 0.9.0 - 2015-05-18
Initial release.
 
[unreleased]: https://github.com/contentful/vault/compare/v0.9.0...HEAD
<!--
[0.9.1]: https://github.com/contentful/vault/compare/v0.9.0...v0.9.1
-->