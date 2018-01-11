# Vault

[![Build Status](https://travis-ci.org/contentful/vault.svg)](https://travis-ci.org/contentful/vault/builds#)

Vault is an Android library that simplifies persistence of data from Contentful via SQLite. It lets you define Java representations for your Contentful models. 

[Contentful](https://www.contentful.com) provides a content infrastructure for digital teams to power content in websites, apps, and devices. Unlike a CMS, Contentful was built to integrate with the modern software stack. It offers a central hub for structured content, powerful management and delivery APIs, and a customizable web app that enable developers and content creators to ship digital products faster.

At compile-time Vault will create a corresponding database schema by generating all the required boilerplate code and injecting it to the classpath. It is also bundled with a complementary lightweight runtime which exposes a simple ORM-like API for pulling resources from the generated database.

Setup
=====

Grab via Maven:
```xml
<dependency>
  <groupId>com.contentful.vault</groupId>
  <artifactId>compiler</artifactId>
  <version>3.1.0</version>
</dependency>
<dependency>
  <groupId>com.contentful.vault</groupId>
  <artifactId>core</artifactId>
  <version>3.1.0</version>
</dependency>
```
or Gradle:
```groovy
apt 'com.contentful.vault:compiler:3.1.0'
compile 'com.contentful.vault:core:3.1.0'
```
or Gradle 3.+:
```groovy
annotationProcessor 'com.contentful.vault:compiler:3.1.0'
annotationProcessor 'com.contentful.vault:core:3.1.0'
compile 'com.contentful.vault:core:3.1.0'
```

Note for Gradle users, make sure to use the [android-apt][apt] Gradle plugin, which lets you configure compile-time only dependencies.
Note for Gradle 3.0 users, please use the `annotationProcessor` instead of `apt`.

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap].

### Models & Fields

Models are defined by declaring a subclass of the `Resource` class. Annotate the class with `@ContentType`, which takes the Content Type's ID as its value.

Fields are defined by annotating class attributes with the `@Field` annotation. 
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

Field ids are escaped and so can be used regularly, however when making queries with a WHERE condition it is up to the caller to escape the field name in case it is a reserved keyword. For example, consider the following model:
```java
@ContentType("...")
public class Foo extends Resource {
  @Field public String order;
}
```

Since *order* is a reserved SQLite keyword, in order to make a query which references that field here is how to escape it:
```java
vault.fetch(Foo.class)
    .where("`" + Foo$Fields.ORDER "` = ?", "bar")
    .first();
```

### Spaces

Spaces can be defined by declaring a class annotated with the `@Space` annotation. It is required to specify the Space ID, an array of model classes and an array of locale codes you wish to persist:

```java
@Space(
    value = "cfexampleapi",
    models = { Cat.class },
    locales = { "en-US", "tlh" }
)
public class DemoSpace { }
```

### Synchronization

Once a Space is defined, invoke Vault to sync the local database with the remote state:

```java
// Client
CDAClient client = CDAClient.builder()
    .setSpace("cfexampleapi")
    .setToken("b4c0n73n7fu1")
    .build();

// Sync
Vault.with(context, DemoSpace.class).requestSync(client);
```

Vault will use a worker thread to request incremental updates from the Sync API and reflect the remote changes to its database.
Once sync is completed, Vault will fire a broadcast with the action `Vault.ACTION_SYNC_COMPLETE`.

Alternatively, you can provide a `SyncCallback` which will be invoked once sync is completed, but make sure to cancel it according to its host lifecycle events:

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

Similarly, if you're using RxJava, you can be notified of sync results via an Observable:
```java
Vault.observeSyncResults() // returns Observable<SyncResult>
```

### Queries

Vault provides a wrapper around its generated database which can be easily used to fetch persisted objects, some examples:

```java
Vault vault = Vault.with(context, DemoSpace.class);

// Fetch the first Cat
vault.fetch(Cat.class)
    .first();
    
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

If you're using RxJava, you can create queries with the `observe()` method, for example:
```java
vault.observe(Cat.class)
    .where(Cat$Fields.NAME + " = ?", "Happy Cat")
    .all() // returns Observable<Cat>
```

The above example creates an `Observable` that subscribes and observes on the same thread initiating the query, so make sure to change that according to your requirements. A typical use-case would be:
```java
vault.observe(Cat.class)
    .all()
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
```

### Migrations

Whenever changes are introduced to any of the previously used models, a migration has to be applied. Simply increment the version number for your space to trigger a migration:

```java
@Space(value = "cfexampleapi", models = { Cat.class }, dbVersion = 2)
public class DemoSpace { }
```

Note: this will delete any previously persisted data.

### Database pre-seeding

Depending on the amount of content in a given space, initial synchronization might take some time. For that we've added support to pre-seed the database with static content. 

If you want to create an initial database file, please use the `VaultDatabaseExporter`. This class takes an Android Context and a Vault Space like you would provide Vault with. If you call the exporters `.export(..)` method, it will create a sqlite database in `src/main/assets/initial_seed.db`. Feel free to rename this file to your needs. If you want to tell Vault to use this database, please create a Vault space class like this:

```java
@Space(
    value = "foo", // space id of the space to use
    models = { Bar.class },  // model classes to be used
    copyPath = "initial_seed.db" // name of the just created database file.
)
public class VaultSpace { }
```

If you want to update this database file, you could think about running a [robolectric](robolectric.org) test before releasing. This test would sync Contentful data to the existing database.

A simple test suite would look like this:

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

Now your database content will always be uptodate when you execute those tests. If an error happens this test will fail and inform you about next steps.

Note that in order to add this functionality to an already shipped app, the **dbVersion** value has to be increased, as it causes invalidation of any pre-existing content.

### ProGuard

Grab the [ProGuard configuration file][proguard] and apply to your project. 

### Documentation

Javadoc is available [here][javadoc].

License
-------

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

 [snap]: https://oss.sonatype.org/content/repositories/snapshots/
 [apt]: https://bitbucket.org/hvisser/android-apt
 [proguard]: proguard-vault.cfg
 [javadoc]: https://contentful.github.io/vault
