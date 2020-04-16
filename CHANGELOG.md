# Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

## Version [3.2.3] - [2020-04-16]
- Fix: SyncConfig has been extended by environment field

## Version [3.2.1] - 2018-05-28
- Fix: ignore empty asset fields
- Update: Contentful CDA SDK to version 10.1.0.

## Version [3.2.0] - 2018-04-24
- Change: use https for asset urls.
- Change: Use CDA SDK 10.0.0 and throw if environments are used.
- Change: Locale handling: CDA 10 uses `/locales` and not `/space.locales`, update tests and internal structure.

## Version [3.1.1] - 2018-02-13
- Add: update CDA SDK
- Remove: Synthetic methods

## Version [3.1.0] - 2017-10-18
- Add: Database pre seeding utility
- Update: Contentful CDA SDK to include Android pre 5.0 auto configured  TLS 1.2.

## Version [3.0.2] - 2017-10-18
- Fix: Do not use a properties file for version name storage.
- Fix: Update version of CDA SDK to include non properties file loading.

## Version [3.0.1] - 2017-08-24
- Fix: Upload all artifacts.

## Version [3.0.0] - 2017-08-16
- Fixes
 * Circular dependencies on links
- Changes
 * Remove direct client passing through. Please use a `SyncConfig.Builder()`.
- Updates
 * Contentful 8.0.0 (was 7.2.0)
 * rx Java 2.1.1 (was 1.0.14)
 * okhttp 3.8.1 (was 2.5.0)
 * robolectric 3.3.2 (was 3.0)
 * truth 0.34 (was 0.25)
 * compile-testing 0.9 (was 0.6)
 * guava 22.0 (was 18.0)
 * commons-io 2.5 (was 2.4)
 * junit 4.12 (was 4.10)

## Version [2.1.0]- 2016-11-10
- Updating to newest Contentful Delivery Api SDK, adding limited support for sync in preview.

## Version [2.0.0] - 2016-04-15
- changed: update cda to 7.0.0

## Version [1.0.0] - 2016-02-29
- NOTE: This release introduces backwards incompatible changes to any existing database schemas, when upgrading make sure to bump the `dbVersion` attribute on your `Space`.
- NOTE: Also: Indicate satisfaction with current stability of api
- fixed: Use correct text type for sql statements

## Version [0.9.14] - 2015-11-23
- Fixed: Non-localized arrays of links now resolve properly.

## Version [0.9.13] - 2015-11-21
- Fixed: Incorrect persistence of localized links.

## Version [0.9.12] - 2015-10-28
- Changed: Use contentful.java v5.0.1 (fixes CME for localized link fields pointing to non-existing resources).

## Version [0.9.11] - 2015-09-17
- Fixed: DB upgrade failures.

## Version [0.9.10] - 2015-09-03
- NOTE: This release introduces backwards incompatible changes to any existing database schemas, when upgrading make sure to bump the `dbVersion` attribute on your `Space`.
- New: Simplified extension `Vault.requestSync(CDAClient)`.
- Changed: Asset attrs are now final.
- Changed: Compiler will fail for static `@Field` elements.
- Changed: Compiler will fail for private `@Field` elements.
- Changed: RxJava v1.0.14.
- Changed: OkHttp v2.5.0.
- Changed: contentful.java v4.0.2.
- Changed: Project now uses Java 7 language level.
- Fixed: Preserve order for arrays of links.
- Fixed: Duplicate array links are no longer squashed.

## Version [0.9.9] - 2015-08-13
- NOTE: This release introduces backwards incompatible changes to any existing database schemas, when upgrading make sure to bump the `dbVersion` attribute on your `Space`.
- New: Support multiple locales per space. Locales to persist must be explicitly defined with the `@Space` annotation.
- Changed: Better error handling when initializing vault with an invalid class.
- Changed: Declaring a model with no fields will fail at compile-time.

## Version [0.9.8] - 2015-08-01
- New: RxJava support with `Vault.observe()` and `Vault.observeSyncResults()`.
- New: `SyncResult` class to represent a result of sync operation.
- Changed: `SyncCallback` replaced `onSuccess()` and `onFailure()` with `onResult(SyncResult)`.
- Changed: RxJava v1.0.13.

## Version [0.9.7] - 2015-07-22
- Changed: Use contentful.java v4.0.1 (fixes NPE for entries with null links).

## Version [0.9.6] - 2015-07-22
- NOTE: This release introduces backwards incompatible changes to any existing database schemas, when upgrading make sure to bump the `dbVersion` attribute on your `Space`.
- New: Support using a pre-existing vault database with the `copyPath` attribute on `@Space`. 
- New: Compiler now injects static `$Fields` class to every model, reflecting the model's column names.
- New: Add `Asset.Fields` class.
- New: Add `BaseFields` class with common resource column names.
- Changed: Use contentful.java v4.0.0.
- Changed: Proguard configuration update to keep generated `$Fields` classes.
- Fixed: Add missing asset metadata: title, description and file map.

## Version [0.9.5] - 2015-07-07
- Changed: Missing location/object fields will be set to null instead of empty collections.
- Fixed: Boolean field values set properly.

## Version [0.9.4] - 2015-07-05
- Changed: Add `onError` and `onSuccess()` methods to `SyncCallback`.
- Changed: Use contentful.java v2.0.4.
- Fixed: Replace `getClass()` calls with generated code for link resolution.
- Fixed: Escape model field names on inserts.

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
 
[unreleased]: https://github.com/contentful/vault/compare/vault-parent-3.2.1...HEAD
[3.2.0]: https://github.com/contentful/vault/compare/vault-parent-3.2.0...vault-parent-3.2.1
[3.2.0]: https://github.com/contentful/vault/compare/vault-parent-3.1.1...vault-parent-3.2.0
[3.1.1]: https://github.com/contentful/vault/compare/vault-parent-3.1.0...vault-parent-3.1.1
[3.1.0]: https://github.com/contentful/vault/compare/vault-parent-3.0.2...vault-parent-3.1.0
[3.0.2]: https://github.com/contentful/vault/compare/vault-parent-3.0.1...vault-parent-3.0.2
[3.0.1]: https://github.com/contentful/vault/compare/vault-parent-3.0.0...vault-parent-3.0.1
[3.0.0]: https://github.com/contentful/vault/compare/vault-parent-2.1.0...vault-parent-3.0.0
[2.1.0]: https://github.com/contentful/vault/compare/vault-parent-2.0.0...vault-parent-2.1.0
[2.0.0]: https://github.com/contentful/vault/compare/vault-parent-1.0.0...vault-parent-2.0.0
[1.0.0]: https://github.com/contentful/vault/compare/vault-parent-0.9.14...vault-parent-1.0.0
[0.9.14]: https://github.com/contentful/vault/compare/vault-parent-0.9.13...vault-parent-0.9.14
[0.9.13]: https://github.com/contentful/vault/compare/vault-parent-0.9.12...vault-parent-0.9.13
[0.9.12]: https://github.com/contentful/vault/compare/vault-parent-0.9.11...vault-parent-0.9.12
[0.9.11]: https://github.com/contentful/vault/compare/vault-parent-0.9.10...vault-parent-0.9.11
[0.9.10]: https://github.com/contentful/vault/compare/vault-parent-0.9.9...vault-parent-0.9.10
[0.9.9]: https://github.com/contentful/vault/compare/vault-parent-0.9.8...vault-parent-0.9.9
[0.9.8]: https://github.com/contentful/vault/compare/vault-parent-0.9.7...vault-parent-0.9.8
[0.9.7]: https://github.com/contentful/vault/compare/vault-parent-0.9.6...vault-parent-0.9.7
[0.9.6]: https://github.com/contentful/vault/compare/vault-parent-0.9.5...vault-parent-0.9.6
[0.9.5]: https://github.com/contentful/vault/compare/vault-parent-0.9.4...vault-parent-0.9.5
[0.9.4]: https://github.com/contentful/vault/compare/vault-parent-0.9.3...vault-parent-0.9.4
[0.9.3]: https://github.com/contentful/vault/compare/v0.9.2...vault-parent-0.9.3
[0.9.2]: https://github.com/contentful/vault/compare/v0.9.1...v0.9.2
[0.9.1]: https://github.com/contentful/vault/compare/v0.9.0...v0.9.1
