# Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

## Version [0.9.4][unreleased] - (in development)
- Fixed: Replace `getClass()` calls with generated code for link resolution.

## Version [0.9.3] - 2015-05-26
- New: `Resource` implementation for `equals()` and `hashCode()`

## Version [0.9.2] - 2015-05-21
- Fixed: Link persistence and resolution (reopened from v0.9.1).

## Version [0.9.1] - 2015-05-20
- New: `Asset` is now `Parcelable`.
- New: Optional `SyncConfig` setting that causes invalidation of existing data upon sync.
- New: Provide direct read-only access to database objects.
- Changed: Use empty collections instead of null for empty arrays / maps.
- Changed: Use contentful.java v2.0.3.
- Changed: contentful.java dependency scope changed to compile.
- Fixed: Link resolution for arrays of resources now works correctly.
- Fixed: Removed incorrect calls to `close()` on database objects.

## Version 0.9.0 - 2015-05-18
Initial release.
 
[unreleased]: https://github.com/contentful/vault/compare/vault-parent-0.9.3...HEAD
[0.9.3]: https://github.com/contentful/vault/compare/v0.9.2...vault-parent-0.9.3
[0.9.2]: https://github.com/contentful/vault/compare/v0.9.1...v0.9.2
[0.9.1]: https://github.com/contentful/vault/compare/v0.9.0...v0.9.1
