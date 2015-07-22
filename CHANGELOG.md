# Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

## Version [0.9.7][unreleased] - (in development)
- New: Compiler now injects static `$Fields` class to every model, reflecting the model's column names.
- New: Add `BaseFields` class with common resource column names.
- Changed: Use contentful.java v4.0.0.
- Changed: Proguard configuration update to keep generated `$Fields` classes.

## Version [0.9.6] - 2015-07-16
- NOTE: This release introduces backwards incompatible changes to any existing database schemas, when updating make sure to bump the `dbVersion` attribute on your `Space`, in order to apply a migration. 
- Changed: Use contentful.java v3.

## Version [0.9.5] - 2015-07-07
- Fixed: Boolean field values set properly.
- Changed: Missing location/object fields will be set to null instead of empty collections.

## Version [0.9.4] - 2015-07-05
- Fixed: Replace `getClass()` calls with generated code for link resolution.
- Fixed: Escape model field names on inserts.
- Changed: Add `onError` and `onSuccess()` methods to `SyncCallback`.
- Changed: Use contentful.java v2.0.4.

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
 
[unreleased]: https://github.com/contentful/vault/compare/vault-parent-0.9.6...HEAD
[0.9.6]: https://github.com/contentful/vault/compare/vault-parent-0.9.5...vault-parent-0.9.6
[0.9.5]: https://github.com/contentful/vault/compare/vault-parent-0.9.4...vault-parent-0.9.5
[0.9.4]: https://github.com/contentful/vault/compare/vault-parent-0.9.3...vault-parent-0.9.4
[0.9.3]: https://github.com/contentful/vault/compare/v0.9.2...vault-parent-0.9.3
[0.9.2]: https://github.com/contentful/vault/compare/v0.9.1...v0.9.2
[0.9.1]: https://github.com/contentful/vault/compare/v0.9.0...v0.9.1
