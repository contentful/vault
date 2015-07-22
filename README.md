# Vault

[![Build Status](https://travis-ci.org/contentful/vault.svg)](https://travis-ci.org/contentful/vault/builds#)

Vault is an Android library that simplifies persistence of data from Contentful via SQLite. It lets you define Java representations for your Contentful models. Then, at compile-time it will create a corresponding database schema by generating all the required boilerplate code and injecting it to the classpath. It is also bundled with a complementary lightweight runtime which exposes a simple ORM-like API for pulling resources from the generated database.

Setup
=====

Grab via Maven:
```xml
<dependency>
  <groupId>com.contentful.vault</groupId>
  <artifactId>compiler</artifactId>
  <version>0.9.7</version>
</dependency>
<dependency>
  <groupId>com.contentful.vault</groupId>
  <artifactId>core</artifactId>
  <version>0.9.7</version>
</dependency>
```
or Gradle:
```groovy
apt 'com.contentful.vault:compiler:0.9.7'
compile 'com.contentful.vault:core:0.9.7'
```

Note for Gradle users, make sure to use the [android-apt][apt] Gradle plugin, which lets you configure compile-time only dependencies.

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

Spaces can be defined by declaring a class annotated with the `@Space` annotation. It is also required to provide the Space ID and an array of model classes to include:

```java
@Space(value = "cfexampleapi", models = { Cat.class })
public class DemoSpace { }
```

### Synchronization

Once a Space is defined, invoke Vault to sync the local database with the remote state:

```java
// Client
CDAClient client = CDAClient.builder()
    .setSpace("cfexampleapi");
    .setToken("b4c0n73n7fu1");
    .build();

// Configuration
SyncConfig config = SyncConfig.builder()
      .setClient(client)
      .build();

// Sync
Vault.with(context, DemoSpace.class).requestSync(config);
```

Vault will use a worker thread to request incremental updates from the Sync API and reflect the remote changes to its database.
Once sync is completed, Vault will fire a broadcast with the action `Vault.ACTION_SYNC_COMPLETE`.

Alternatively, you can provide a `SyncCallback` which will be invoked once sync is completed, but make sure to cancel it according to its host lifecycle events:

```java
class SomeActivity extends Activity {
  SyncCallback callback;
  
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    Vault.with(this, DemoSpace.class).requestSync(config, callback = new SyncCallback() {
      @Override public void onSuccess() {
        // Sync completed \o/
      }
      
      @Override public void onError(Throwable cause) {
        // Handle error
      }
    });
  }
  
  @Override protected void onDestroy() {
    Vault.cancel(callback);
    super.onDestroy();
  }
}
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
    
// Fetch all Cats, oredered by creation date:
vault.fetch(Cat.class)
    .order(Cat$Fields.CREATED_AT)
    .all();
```


### Migrations

Whenever changes are introduced to any of the previously used models, a migration has to be applied. Simply increment the version number for your space to trigger a migration:

```java
@Space(value = "cfexampleapi", models = { Cat.class }, dbVersion = 2)
public class DemoSpace { }
```

Note: this will delete any previously persisted data.

### ProGuard

Grab the [ProGuard configuration file][proguard] and apply to your project. 

### Documentation

Javadoc is available [here][javadoc].

License
-------

    Copyright 2015 Contentful, GmbH.

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
