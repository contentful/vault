<p align="center">
  <img src="assets/feature_graphic.png" alt="Contentful Vault Library">
</p>

<p align="center">
  <a href="https://www.contentful.com/slack/">
    <img src="https://img.shields.io/badge/-Join%20Community%20Slack-2AB27B.svg?logo=slack&maxAge=31557600" alt="Join Contentful Community Slack">
  </a>
  &nbsp;
  <a href="https://www.contentfulcommunity.com/">
    <img src="https://img.shields.io/badge/-Join%20Community%20Forum-3AB2E6.svg?logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCA1MiA1OSI+CiAgPHBhdGggZmlsbD0iI0Y4RTQxOCIgZD0iTTE4IDQxYTE2IDE2IDAgMCAxIDAtMjMgNiA2IDAgMCAwLTktOSAyOSAyOSAwIDAgMCAwIDQxIDYgNiAwIDEgMCA5LTkiIG1hc2s9InVybCgjYikiLz4KICA8cGF0aCBmaWxsPSIjNTZBRUQyIiBkPSJNMTggMThhMTYgMTYgMCAwIDEgMjMgMCA2IDYgMCAxIDAgOS05QTI5IDI5IDAgMCAwIDkgOWE2IDYgMCAwIDAgOSA5Ii8+CiAgPHBhdGggZmlsbD0iI0UwNTM0RSIgZD0iTTQxIDQxYTE2IDE2IDAgMCAxLTIzIDAgNiA2IDAgMSAwLTkgOSAyOSAyOSAwIDAgMCA0MSAwIDYgNiAwIDAgMC05LTkiLz4KICA8cGF0aCBmaWxsPSIjMUQ3OEE0IiBkPSJNMTggMThhNiA2IDAgMSAxLTktOSA2IDYgMCAwIDEgOSA5Ii8+CiAgPHBhdGggZmlsbD0iI0JFNDMzQiIgZD0iTTE4IDUwYTYgNiAwIDEgMS05LTkgNiA2IDAgMCAxIDkgOSIvPgo8L3N2Zz4K&maxAge=31557600" alt="Join Contentful Community Forum">
  </a>
</p>

# vault - Contentful Offline Persistence for Android

> Vault is an Android library that simplifies persisting data from [Contentful](https://www.contentful.com/) to SQLite. It defines a Java representation of your Contentful models, and at compile-time generates the corresponding database schema plus all the required boilerplate. It ships with a complementary lightweight runtime that exposes a simple ORM-like API for pulling resources back out of the generated database.

<p align="center">
  <img src="https://img.shields.io/badge/Status-Maintained-green.svg" alt="This repository is actively maintained" />
  &nbsp;
  <a href="LICENSE">
    <img src="https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg" alt="Apache 2.0 License" />
  </a>
  &nbsp;
  <a href="https://github.com/contentful/vault/actions/workflows/test.yml">
    <img src="https://github.com/contentful/vault/actions/workflows/test.yml/badge.svg" alt="Build Status">
  </a>
</p>

**What is Contentful?**

[Contentful](https://www.contentful.com/) provides content infrastructure for digital teams to power websites, apps, and devices. Unlike a CMS, Contentful was built to integrate with the modern software stack. It offers a central hub for structured content, powerful management and delivery APIs, and a customizable web app that enable developers and content creators to ship their products faster.

<details open>
<summary>Table of contents</summary>
<!-- TOC -->

- [vault - Contentful Offline Persistence for Android](#vault---contentful-offline-persistence-for-android)
  - [Core Features](#core-features)
  - [Getting started](#getting-started)
    - [Requirements](#requirements)
    - [Installation](#installation)
    - [Your first sync](#your-first-sync)
  - [Using the SDK](#using-the-sdk)
    - [Models and fields](#models-and-fields)
    - [Spaces](#spaces)
    - [Synchronization](#synchronization)
    - [Queries](#queries)
    - [Migrations](#migrations)
    - [Preseeding](#preseeding)
  - [Advanced configuration](#advanced-configuration)
    - [Proguard](#proguard)
  - [Documentation & References](#documentation--references)
  - [Reach out to us](#reach-out-to-us)
    - [Have questions about how to use this library?](#have-questions-about-how-to-use-this-library)
    - [You found a bug or want to propose a feature?](#you-found-a-bug-or-want-to-propose-a-feature)
    - [You need to share confidential information or have other questions?](#you-need-to-share-confidential-information-or-have-other-questions)
  - [Get involved](#get-involved)
  - [License](#license)
  - [Code of Conduct](#code-of-conduct)

<!-- /TOC -->

</details>

## Core Features

- Defines a Java representation of your Contentful content model as plain classes annotated with `@ContentType` and `@Field`.
- Generates the corresponding SQLite schema and all persistence boilerplate at compile time via an annotation processor — no manual `SQLiteOpenHelper` code required.
- Keeps your local database in sync with Contentful using the [Sync API](https://www.contentful.com/developers/docs/references/content-delivery-api/#/reference/synchronization), fetching only what changed since the last call.
- A simple, ORM-like query API (`.fetch(Type.class).where(…).first()` / `.all()`) for reading persisted resources back out.
- RxJava support: `.observe(Type.class)` returns an `Observable` for reactive queries, and `Vault.observeSyncResults()` reports sync completion reactively.
- Multi-locale support: declare which locales to persist per `@Space`, and query resources by locale.
- Schema migrations via a simple `dbVersion` bump on your `@Space` annotation.
- Preseeding support: ship a pre-built SQLite database in your APK to avoid the cost of an initial sync on first launch.

## Getting started

- [Requirements](#requirements)
- [Installation](#installation)
- [Your first sync](#your-first-sync)

### Requirements

| Requirement | Version |
| --- | --- |
| Java | 8 or higher |
| Android | Support depends on the [`contentful.java`](https://github.com/contentful/contentful.java) SDK version in use |

Vault depends on the [contentful.java](https://github.com/contentful/contentful.java) SDK for all network access to the Content Delivery API.

### Installation

* _Maven_

```xml
<dependency>
  <groupId>com.contentful.vault</groupId>
  <artifactId>compiler</artifactId>
  <version>3.2.12</version>
</dependency>
<dependency>
  <groupId>com.contentful.vault</groupId>
  <artifactId>core</artifactId>
  <version>3.2.12</version>
</dependency>
```

* _Gradle_

```groovy
annotationProcessor 'com.contentful.vault:compiler:3.2.12'
implementation 'com.contentful.vault:core:3.2.12'
```

> Note: With **3.2.12 and older**, also add `annotationProcessor 'com.contentful.vault:core:<version>'` if the build fails with `NoClassDefFoundError: com/contentful/vault/ContentType`.

> Note: Avoid **3.2.11**. It was published from an older branch and is missing `SyncConfig.Builder#setLimit` and `#setSingleLocale` (added in 3.2.9). Use 3.2.12 or newer.

### Your first sync

Define a model, group it into a `@Space`, then request a sync:

```java
@ContentType("cat")
public class Cat extends Resource {
  @Field public String name;
}

@Space(
    value = "cfexampleapi",
    models = { Cat.class },
    locales = { "en-US" }
)
public class DemoSpace { }
```

```java
CDAClient client = CDAClient.builder()
    .setSpace("cfexampleapi")
    .setToken("b4c0n73n7fu1")
    .build();

Vault.with(context, DemoSpace.class).requestSync(SyncConfig.builder().setClient(client).build());
```

Vault runs the sync on a worker thread and reflects the changes in its local database. Once complete, it broadcasts `Vault.ACTION_SYNC_COMPLETE`.

## Using the SDK

### Models and fields

Models are defined by declaring a subclass of `Resource`. Annotate the class with `@ContentType`, passing the Content Type's ID as its value.

Fields are defined by annotating class attributes with `@Field`:

```java
@ContentType("cat")
public class Cat extends Resource {
  @Field public String name;
  @Field public Cat bestFriend;
  @Field public Asset image;
}
```

By default, the attribute name is used as the field's id, but it can also be specified explicitly:

```java
@Field("field-id-goes-here")
public String someField;
```

Field ids are escaped automatically, but when writing a `WHERE` condition it's up to the caller to escape field names that collide with reserved SQL keywords:

```java
@ContentType("...")
public class Foo extends Resource {
  @Field public String order;
}
```

Since `order` is a reserved SQLite keyword, a query referencing that field looks like this:

```java
vault.fetch(Foo.class)
    .where("`" + Foo$Fields.ORDER + "` = ?", "bar")
    .first();
```

### Spaces

Spaces are classes annotated with `@Space`. Specify the Space ID, an array of model classes, and an array of locale codes to persist:

```java
@Space(
    value = "cfexampleapi",
    models = { Cat.class },
    locales = { "en-US", "tlh" }
)
public class DemoSpace { }
```

### Synchronization

Once a Space is defined, invoke Vault to synchronize the local database with Contentful:

```java
// Client.
CDAClient client = CDAClient.builder()
    .setSpace("cfexampleapi")
    .setToken("b4c0n73n7fu1")
    .build();

// Sync.
Vault.with(context, DemoSpace.class).requestSync(SyncConfig.builder().setClient(client).build());
```

Vault uses a worker thread to request updates from the Sync API and reflect the changes in its database. Once sync completes, Vault broadcasts `Vault.ACTION_SYNC_COMPLETE`.

Providing a `SyncCallback` invokes it once sync completes:

```java
class SomeActivity extends Activity {
  SyncCallback callback;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Vault.with(this, DemoSpace.class).requestSync(SyncConfig.builder().setClient(client).build(), callback = new SyncCallback() {
      @Override public void onResult(SyncResult result) {
        if (result.isSuccessful()) {
          // Success \o/
        } else {
          // Failure
        }
      }
    });
  }

  @Override protected void onDestroy() {
    Vault.cancel(callback);
    super.onDestroy();
  }
}
```

> Note: Extra care needs to be taken for the lifecycle — cancel the callback on lifecycle events.

#### Sync options

`SyncConfig` takes either a `CDAClient`, or an access token and a space ID:

```java
SyncConfig config = SyncConfig.builder()
    .setAccessToken("b4c0n73n7fu1")
    .setSpaceId("cfexampleapi")
    .setEnvironment("master")   // optional, defaults to "master"
    .setLimit(500)              // optional: page size of the initial sync, 1-1000
    .setSingleLocale(true)      // optional: store only the space's default locale
    .setInvalidate(false)       // optional: true wipes local data and syncs from scratch
    .build();

Vault.with(context, DemoSpace.class).requestSync(config);
```

- `setLimit` only applies to the first page of an *initial* sync. Later pages and delta syncs use the API default. Values outside 1-1000 throw `IllegalArgumentException`.
- `setSingleLocale(true)` stores only the default locale, and queries for any other locale return no results. Changing this setting, or opening an older database whose locale mode is unknown, makes the next sync replace all data with a fresh initial sync. Existing offline data remains available if fetching or saving the replacement fails. The locale mode and sync token are stored in the same database transaction.
- `setInvalidate(true)` replaces local data with a fresh initial sync. The old data is only removed once the new data has been downloaded, so a failed sync keeps it.

To close a space's database connection, call `vault.release()`. It only affects that space; `Vault.releaseAll()` is deprecated because it closes every space. Afterwards, get a new instance with `Vault.with(...)`.

RxJava users can subscribe to sync results via an `Observable`:

```java
Vault.observeSyncResults() // Returns Observable<SyncResult>.
```

### Queries

Vault wraps its generated database with a query API for fetching persisted objects:

```java
Vault vault = Vault.with(context, DemoSpace.class);

// Fetch the first Cat.
vault.fetch(Cat.class).first();

// Fetch the most recently updated Cat.
vault.fetch(Cat.class)
    .order(Cat$Fields.UPDATED_AT + " DESC")
    .first();

// Fetch a Cat with a specific name.
vault.fetch(Cat.class)
    .where(Cat$Fields.NAME + " = ?", "Nyan Cat")
    .first();

// Fetch a Cat with a specific name pattern.
vault.fetch(Cat.class)
    .where(Cat$Fields.NAME + " LIKE ?", "%Nyan%")
    .first();

// Fetch a Cat with a specific boolean field.
// SQLite stores booleans as 0/1.
vault.fetch(Cat.class)
    .where(Cat$Fields.IS_GRUMPY + " = ?", "1")
    .first();

// Fetch all Cats, ordered by creation date.
vault.fetch(Cat.class)
    .order(Cat$Fields.CREATED_AT)
    .all();

// Fetch all Cats, using the Klingon locale.
vault.fetch(Cat.class)
    .all("tlh");
```

RxJava queries are created via `.observe()`:

```java
vault.observe(Cat.class)
    .where(Cat$Fields.NAME + " = ?", "Happy Cat")
    .all() // Returns Observable<Cat>.
```

The example above creates an `Observable` that subscribes and observes on the thread that initiated the query. Chain `.subscribeOn(…)`/`.observeOn(…)` if you need a different threading model:

```java
vault.observe(Cat.class)
    .all()
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
```

### Migrations

Whenever a previously used model changes, apply a migration by incrementing the version number on your `@Space`:

```java
@Space(value = "cfexampleapi", models = { Cat.class }, dbVersion = 2)
public class DemoSpace { }
```

> Note: this deletes any previously persisted data and re-acquires it on the next sync.

### Preseeding

Depending on the amount of content in a given space, initial synchronization can take some time. To avoid that cost, pre-seed the database with static content instead.

Use `VaultDatabaseExporter` to create the initial database file. It takes an Android `Context` and a Vault Space; calling `.export(…)` creates a SQLite database at `src/main/assets/initial_seed.db`. Point Vault at it as follows:

```java
@Space(
    value = "{spaceid}",        // Space id of the space to use.
    models = { Cat.class },     // Model classes to be used.
    copyPath = "initial_seed.db" // Name of the just-created database file.
)
public class VaultSpace { }
```

Keep the bundled database up to date by running a [Robolectric](http://robolectric.org) test before each release, which syncs live Contentful data into the existing database file:

```java
@RunWith(RobolectricTestRunner.class)
public class TestSeedDB {
  @Test
  public void testSyncDBtoSqlite() throws Exception {
    final Activity activity = Robolectric.setupActivity(Activity.class);

    assertTrue(new VaultDatabaseExporter().export(activity, VaultSpace.class, VaultSpace.TOKEN));
  }
}
```

If this test fails, that indicates the bundled database content is out of date; follow the guidance in the failure message.

> Note: To add preseeding to an already-shipped app, increment `dbVersion` — this invalidates any pre-existing content on device so the new bundled database takes effect.

## Advanced configuration

### Proguard

Grab the [ProGuard configuration file](proguard-vault.cfg) and apply it to your project.

## Documentation & References

Browse the [Javadoc](https://contentful.github.io/vault) for the full API reference of this library. Every released change is recorded in the [CHANGELOG.md](CHANGELOG.md).

Vault is built on top of the [contentful.java](https://github.com/contentful/contentful.java) SDK; consult its README for details on `CDAClient` and the underlying Content Delivery API.

## Reach out to us

### Have questions about how to use this library?

* Reach out to our community forum: [![Contentful Community Forum](https://img.shields.io/badge/-Join%20Community%20Forum-3AB2E6.svg?logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCA1MiA1OSI+CiAgPHBhdGggZmlsbD0iI0Y4RTQxOCIgZD0iTTE4IDQxYTE2IDE2IDAgMCAxIDAtMjMgNiA2IDAgMCAwLTktOSAyOSAyOSAwIDAgMCAwIDQxIDYgNiAwIDEgMCA5LTkiIG1hc2s9InVybCgjYikiLz4KICA8cGF0aCBmaWxsPSIjNTZBRUQyIiBkPSJNMTggMThhMTYgMTYgMCAwIDEgMjMgMCA2IDYgMCAxIDAgOS05QTI5IDI5IDAgMCAwIDkgOWE2IDYgMCAwIDAgOSA5Ii8+CiAgPHBhdGggZmlsbD0iI0UwNTM0RSIgZD0iTTQxIDQxYTE2IDE2IDAgMCAxLTIzIDAgNiA2IDAgMSAwLTkgOSAyOSAyOSAwIDAgMCA0MSAwIDYgNiAwIDAgMC05LTkiLz4KICA8cGF0aCBmaWxsPSIjMUQ3OEE0IiBkPSJNMTggMThhNiA2IDAgMSAxLTktOSA2IDYgMCAwIDEgOSA5Ii8+CiAgPHBhdGggZmlsbD0iI0JFNDMzQiIgZD0iTTE4IDUwYTYgNiAwIDEgMS05LTkgNiA2IDAgMCAxIDkgOSIvPgo8L3N2Zz4K&maxAge=31557600)](https://support.contentful.com/)
* Jump into our community slack channel: [![Contentful Community Slack](https://img.shields.io/badge/-Join%20Community%20Slack-2AB27B.svg?logo=slack&maxAge=31557600)](https://www.contentful.com/slack/)

### You found a bug or want to propose a feature?

* File an issue here on GitHub: [![File an issue](https://img.shields.io/badge/-Create%20Issue-6cc644.svg?logo=github&maxAge=31557600)](https://github.com/contentful/vault/issues/new). Make sure to remove any credential from your code before sharing it.

### You need to share confidential information or have other questions?

* File a support ticket at our Contentful Customer Support: [![File support ticket](https://img.shields.io/badge/-Submit%20Support%20Ticket-3AB2E6.svg?logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCA1MiA1OSI+CiAgPHBhdGggZmlsbD0iI0Y4RTQxOCIgZD0iTTE4IDQxYTE2IDE2IDAgMCAxIDAtMjMgNiA2IDAgMCAwLTktOSAyOSAyOSAwIDAgMCAwIDQxIDYgNiAwIDEgMCA5LTkiIG1hc2s9InVybCgjYikiLz4KICA8cGF0aCBmaWxsPSIjNTZBRUQyIiBkPSJNMTggMThhMTYgMTYgMCAwIDEgMjMgMCA2IDYgMCAxIDAgOS05QTI5IDI5IDAgMCAwIDkgOWE2IDYgMCAwIDAgOSA5Ii8+CiAgPHBhdGggZmlsbD0iI0UwNTM0RSIgZD0iTTQxIDQxYTE2IDE2IDAgMCAxLTIzIDAgNiA2IDAgMSAwLTkgOSAyOSAyOSAwIDAgMCA0MSAwIDYgNiAwIDAgMC05LTkiLz4KICA8cGF0aCBmaWxsPSIjMUQ3OEE0IiBkPSJNMTggMThhNiA2IDAgMSAxLTktOSA2IDYgMCAwIDEgOSA5Ii8+CiAgPHBhdGggZmlsbD0iI0JFNDMzQiIgZD0iTTE4IDUwYTYgNiAwIDEgMS05LTkgNiA2IDAgMCAxIDkgOSIvPgo8L3N2Zz4K&maxAge=31557600)](https://www.contentful.com/support/)

## Get involved

[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?maxAge=31557600)](http://makeapullrequest.com)

We appreciate any help on our repositories. Feel free to open a pull request or an issue.

## License

```
Copyright 2017 Contentful, GmbH.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Code of Conduct

We want to provide a safe, inclusive, welcoming, and harassment-free space and experience for all participants, regardless of gender identity and expression, sexual orientation, disability, physical appearance, socioeconomic status, body size, ethnicity, nationality, level of experience, age, religion (or lack thereof), or other identity markers.

[Read our full Code of Conduct](https://github.com/contentful-developer-relations/community-code-of-conduct).
