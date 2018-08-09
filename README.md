<p align="center">
  <img src="assets/feature_graphic.png" alt="Contentful Java SDK"/><br/>
  <a href="https://www.contentful.com/slack/">
    <img src="https://img.shields.io/badge/-Join%20Community%20Slack-2AB27B.svg?logo=slack&maxAge=31557600" alt="Join Contentful Community Slack">
  </a>
  &nbsp;
  <a href="https://www.contentfulcommunity.com/">
    <img src="https://img.shields.io/badge/-Join%20Community%20Forum-3AB2E6.svg?logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCA1MiA1OSI+CiAgPHBhdGggZmlsbD0iI0Y4RTQxOCIgZD0iTTE4IDQxYTE2IDE2IDAgMCAxIDAtMjMgNiA2IDAgMCAwLTktOSAyOSAyOSAwIDAgMCAwIDQxIDYgNiAwIDEgMCA5LTkiIG1hc2s9InVybCgjYikiLz4KICA8cGF0aCBmaWxsPSIjNTZBRUQyIiBkPSJNMTggMThhMTYgMTYgMCAwIDEgMjMgMCA2IDYgMCAxIDAgOS05QTI5IDI5IDAgMCAwIDkgOWE2IDYgMCAwIDAgOSA5Ii8+CiAgPHBhdGggZmlsbD0iI0UwNTM0RSIgZD0iTTQxIDQxYTE2IDE2IDAgMCAxLTIzIDAgNiA2IDAgMSAwLTkgOSAyOSAyOSAwIDAgMCA0MSAwIDYgNiAwIDAgMC05LTkiLz4KICA8cGF0aCBmaWxsPSIjMUQ3OEE0IiBkPSJNMTggMThhNiA2IDAgMSAxLTktOSA2IDYgMCAwIDEgOSA5Ii8+CiAgPHBhdGggZmlsbD0iI0JFNDMzQiIgZD0iTTE4IDUwYTYgNiAwIDEgMS05LTkgNiA2IDAgMCAxIDkgOSIvPgo8L3N2Zz4K&maxAge=31557600" alt="Join Contentful Community Forum"/>
  </a>
</p>

vault - Contentful Offline Persistence for Android
==================================================

[![Build Status](https://travis-ci.org/contentful/vault.svg)](https://travis-ci.org/contentful/vault/builds#)

> Vault is an Android library that simplifies persistence of Resources from Contentful via SQLite. It defines a Java representation of Contentful models. At compile-time Vault will create a corresponding database schema by generating all the required boilerplate code and injecting it into the classpath. It is also bundled with a complementary lightweight runtime which exposes a simple ORM-like API for pulling resources from the generated database.

What is Contentful?
-------------------

[Contentful](https://www.contentful.com) provides a content infrastructure for digital teams to power content in websites, apps, and devices. Unlike a CMS, Contentful was built to integrate with the modern software stack. It offers a central hub for structured content, powerful management and delivery APIs, and a customizable web app that enable developers and content creators to ship digital products faster.


<details open>
  <summary>Table of contents</summary>
  <!-- TOC -->

- [Setup](#setup)
  - [Snapshots](#snapshots)
- [Usage](#usage)
  - [Models and Fields](#models-and-fields)
  - [Spaces](#spaces)
  - [Synchronization](#synchronization)
  - [Queries](#queries)
  - [Migrations](#migrations)
  - [Preseeding](#preseeding)
- [Documentation](#documentation)
- [Licence](#licence)
- [Reaching Contentful](#reaching-contentful)
  - [Bugs and Feature Requests](#bugs-and-feature-requests)
  - [Sharing Confidential Information](#sharing-confidential-information)
  - [Getting involved](#getting-involved)
- [Code of Conduct](#code-of-conduct)
  <!-- /TOC -->
</details>


Setup
=====

Install the dependency by 

* _Maven_
```xml
<dependency>
  <groupId>com.contentful.vault</groupId>
  <artifactId>compiler</artifactId>
  <version>3.2.1</version>
</dependency>
<dependency>
  <groupId>com.contentful.vault</groupId>
  <artifactId>core</artifactId>
  <version>3.2.1</version>
</dependency>
```

* _Gradle_

```groovy
apt 'com.contentful.vault:compiler:3.2.1'
compile 'com.contentful.vault:core:3.2.1'
```
* _Gradle 3.+_
```groovy
annotationProcessor 'com.contentful.vault:compiler:3.2.1'
annotationProcessor 'com.contentful.vault:core:3.2.1'
compile 'com.contentful.vault:core:3.2.1'
```

> Note for Gradle: Use the [android-apt][apt] Gradle plugin, which lets you configure compile-time only dependencies.
> Note for Gradle 3.0 and newer: Use the `annotationProcessor` instead of `apt`.

Snapshots
---------

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap].

Usage
=====

Models and Fields
-----------------

Models are defined by declaring a subclass of the `Resource` class. Annotate the class with `@ContentType`, which takes the Content Type's ID as its value.

Fields are defined by annotating class attributes with the `@Field` annotation:

```java
@ContentType("cat")
public class Cat extends Resource {
  @Field public String name;
  @Field public Cat bestFriend;
  @Field public Asset image;
}
```

By default, the name of the attribute will be used as the field's ID, but can also be specified explicitly:

```java
@Field("field-id-goes-here") 
public String someField; 
```

Field ids are escaped, however when making queries with a `WHERE` condition it is up to the caller to escape the field name in case it is a reserved keyword. For example:

```java
@ContentType("...")
public class Foo extends Resource {
  @Field public String order;
}
```

Since `order` is a reserved SQLite keyword, making a query which references that field will be done like so:

```java
vault.fetch(Foo.class)
    .where("`" + Foo$Fields.ORDER "` = ?", "bar")
    .first();
```

Spaces
------

Spaces are classes annotated with the `@Space` annotation. It is required to specify the Space ID, an array of _Model_ classes and an array of locale codes wanted to be persisted:

```java
@Space(
    value = "cfexampleapi",
    models = { Cat.class },
    locales = { "en-US", "tlh" }
)
public class DemoSpace { }
```

Synchronization
---------------

Once a Space is defined, Vault is invoked to synchronize the local database with Contentful:

```java
// Client
CDAClient client = CDAClient.builder()
    .setSpace("cfexampleapi")
    .setToken("b4c0n73n7fu1")
    .build();

// Sync
Vault.with(context, DemoSpace.class).requestSync(client);
```

Vault uses a worker thread to request updates from the Sync API and reflect the changes in its database.
Once sync is completed, Vault will fire a broadcast with the action `Vault.ACTION_SYNC_COMPLETE`.

Providing a `SyncCallback` results in it beeing invoked once sync is completed:

```java
class SomeActivity extends Activity {
  SyncCallback callback;
  
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    Vault.with(this, DemoSpace.class).requestSync(client, callback = new SyncCallback() {
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

> Note: Extra care needs to be taken for the lifecycle: Cancelling the callback on lifecycle events is important.

Similarly using _RxJava_ notifies of sync results via an `Observable`:

```java
Vault.observeSyncResults() // returns Observable<SyncResult>
```

Queries
-------

Vault provides a wrapper around its generated database which fetches persisted objects:

```java
Vault vault = Vault.with(context, DemoSpace.class);

// Fetch the first Cat
vault.fetch(Cat.class).first();
    
// Fetch the most recently updated Cat
vault.fetch(Cat.class)
    .order(Cat$Fields.UPDATED_AT + " DESC")
    .first();

// Fetch a Cat with a specific name
vault.fetch(Cat.class)
    .where(Cat$Fields.NAME + " = ?", "Nyan Cat")
    .first();

// Fetch a Cat with a specific name pattern
vault.fetch(Cat.class)
    .where(Cat$Fields.NAME + " LIKE ?", "%Nyan%")
    .first();

// Fetch a Cat with a specific boolean field
// SQLite is storing booleans as 0/1 
vault.fetch(Cat.class)
    .where(Cat$Fields.IS_GRUMPY + " = ?", "1")
    .first();
    
// Fetch all Cats, ordered by creation date:
vault.fetch(Cat.class)
    .order(Cat$Fields.CREATED_AT)
    .all();

// Fetch all Cats, using the Klingon locale:
vault.fetch(Cat.class)
    .all("tlh");
```

Using RxJava queries are created by the `observe()` method, for example:

```java
vault.observe(Cat.class)
    .where(Cat$Fields.NAME + " = ?", "Happy Cat")
    .all() // returns Observable<Cat>
```

The above example creates an `Observable` that subscribes and observes on the same thread initiating the query. That changes if this typical use-case is used:

```java
vault.observe(Cat.class)
    .all()
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
```

Migrations
----------

Whenever changes are introduced to any of the previously used Models, a migration has to be applied. Increment the version number to trigger a migration:

```java
@Space(value = "cfexampleapi", models = { Cat.class }, dbVersion = 2)
public class DemoSpace { }
```

> Note: this will delete any previously persisted data and reaquires them.

Preseeding
----------

Depending on the amount of content in a given space, initial synchronization might take some time. For that support to pre-seed the database with static content got added. 

For creating an initial database file, use the `VaultDatabaseExporter`. This class takes an Android Context and a Vault Space. Calling the `.export(..)` method, it  creates a sqlite database in `src/main/assets/initial_seed.db`. Instrucing Vault to use this is done like so:

```java
@Space(
    value = "{spaceid}", // space id of the space to use
    models = { Cat.class },  // model classes to be used
    copyPath = "initial_seed.db" // name of the just created database file.
)
public class VaultSpace { }
```

Udating this database file is done by leveraging a [robolectric](robolectric.org) test before releasing. This test would sync Contentful data to the existing database.

A simple test suite looks like this:

```java
@RunWith(RobolectricTestRunner.class)
public class TestSeedDB {
 @literal @Test
  public void testSyncDBtoSqlite() throws Exception {
    final Activity activity = Robolectric.setupActivity(Activity.class);

    assertTrue(new VaultDatabaseExporter().export(activity, VaultSpace.class, VaultSpace.TOKEN));
  }
}
```

The database content will always be uptodate when those tests get executed. If an error happens this test will fail and information about next steps will be given.

> Note: In order to add this functionality to an already shipped app, the **dbVersion** value has to be increased, as it causes invalidation of any pre-existing content.

ProGuard
--------

Grab the [ProGuard configuration file][proguard] and apply to your project. 

Documentation
=============

Javadoc is available [here][javadoc].

License
=======

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

Reaching Contentful
===================

Questions
---------

* Use the community forum: [![Contentful Community Forum](https://img.shields.io/badge/-Join%20Community%20Forum-3AB2E6.svg?logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCA1MiA1OSI+CiAgPHBhdGggZmlsbD0iI0Y4RTQxOCIgZD0iTTE4IDQxYTE2IDE2IDAgMCAxIDAtMjMgNiA2IDAgMCAwLTktOSAyOSAyOSAwIDAgMCAwIDQxIDYgNiAwIDEgMCA5LTkiIG1hc2s9InVybCgjYikiLz4KICA8cGF0aCBmaWxsPSIjNTZBRUQyIiBkPSJNMTggMThhMTYgMTYgMCAwIDEgMjMgMCA2IDYgMCAxIDAgOS05QTI5IDI5IDAgMCAwIDkgOWE2IDYgMCAwIDAgOSA5Ii8+CiAgPHBhdGggZmlsbD0iI0UwNTM0RSIgZD0iTTQxIDQxYTE2IDE2IDAgMCAxLTIzIDAgNiA2IDAgMSAwLTkgOSAyOSAyOSAwIDAgMCA0MSAwIDYgNiAwIDAgMC05LTkiLz4KICA8cGF0aCBmaWxsPSIjMUQ3OEE0IiBkPSJNMTggMThhNiA2IDAgMSAxLTktOSA2IDYgMCAwIDEgOSA5Ii8+CiAgPHBhdGggZmlsbD0iI0JFNDMzQiIgZD0iTTE4IDUwYTYgNiAwIDEgMS05LTkgNiA2IDAgMCAxIDkgOSIvPgo8L3N2Zz4K&maxAge=31557600)](https://support.contentful.com/)
* Use the community slack channel: [![Contentful Community Slack](https://img.shields.io/badge/-Join%20Community%20Slack-2AB27B.svg?logo=slack&maxAge=31557600)](https://www.contentful.com/slack/)

Bugs and Feature Requests
-------------------------

* File an issue here [![File an issue](https://img.shields.io/badge/-Create%20Issue-6cc644.svg?logo=github&maxAge=31557600)](https://github.com/contentful/vault/issues/new).

Sharing Confidential Information
--------------------------------

* File a support ticket at our Contentful Customer Support: [![File support ticket](https://img.shields.io/badge/-Submit%20Support%20Ticket-3AB2E6.svg?logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCA1MiA1OSI+CiAgPHBhdGggZmlsbD0iI0Y4RTQxOCIgZD0iTTE4IDQxYTE2IDE2IDAgMCAxIDAtMjMgNiA2IDAgMCAwLTktOSAyOSAyOSAwIDAgMCAwIDQxIDYgNiAwIDEgMCA5LTkiIG1hc2s9InVybCgjYikiLz4KICA8cGF0aCBmaWxsPSIjNTZBRUQyIiBkPSJNMTggMThhMTYgMTYgMCAwIDEgMjMgMCA2IDYgMCAxIDAgOS05QTI5IDI5IDAgMCAwIDkgOWE2IDYgMCAwIDAgOSA5Ii8+CiAgPHBhdGggZmlsbD0iI0UwNTM0RSIgZD0iTTQxIDQxYTE2IDE2IDAgMCAxLTIzIDAgNiA2IDAgMSAwLTkgOSAyOSAyOSAwIDAgMCA0MSAwIDYgNiAwIDAgMC05LTkiLz4KICA8cGF0aCBmaWxsPSIjMUQ3OEE0IiBkPSJNMTggMThhNiA2IDAgMSAxLTktOSA2IDYgMCAwIDEgOSA5Ii8+CiAgPHBhdGggZmlsbD0iI0JFNDMzQiIgZD0iTTE4IDUwYTYgNiAwIDEgMS05LTkgNiA2IDAgMCAxIDkgOSIvPgo8L3N2Zz4K&maxAge=31557600)](https://www.contentful.com/support/)

Getting involved
----------------

[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?maxAge=31557600)](http://makeapullrequest.com)

Code of Conduct
===============

Contentful wants to provide a safe, inclusive, welcoming, and harassment-free space and experience for all participants, regardless of gender identity and expression, sexual orientation, disability, physical appearance, socioeconomic status, body size, ethnicity, nationality, level of experience, age, religion (or lack thereof), or other identity markers.

[Full Code of Conduct](https://github.com/contentful-developer-relations/community-code-of-conduct).


 [snap]: https://oss.sonatype.org/content/repositories/snapshots/
 [apt]: https://bitbucket.org/hvisser/android-apt
 [proguard]: proguard-vault.cfg
 [javadoc]: https://contentful.github.io/vault
