# Vault

[![Build Status](https://magnum.travis-ci.com/contentful/vault.svg?token=J8uWM5wmFQZTgYu2HNmp&branch=master)](https://magnum.travis-ci.com/contentful/vault)

Easy persistence of Contentful data for Android over SQLite.

Setup
=====

Grab via Maven:
```xml
<dependency>
  <groupId>com.contentful.vault</groupId>
  <artifactId>compiler</artifactId>
  <version>1.0.0</version>
</dependency>
<dependency>
  <groupId>com.contentful.vault</groupId>
  <artifactId>core</artifactId>
  <version>1.0.0</version>
</dependency>
```
or Gradle:
```groovy
compile 'com.contentful.vault:compiler:1.0.0'
compile 'com.contentful.vault:core:1.0.0'
```

### Models & Fields

Models are defined by declaring a subclass of the `Resource` class. Annotate the class with `@ContentType`, which takes the Content Type's ID as it's value.

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

### Spaces

Spaces can be defined by declaring a class annotated with the `@Space` annotation. It is also required to provide the Space ID and an array of model classes to include:

```java
@Space(value = "cfexampleapi", models = { Cat.class })
public class DemoSpace { }
```

### Synchronization

Once a Space is defined, invoke Vault to sync the local database with the remote state:

```java
// Create a CDA client
CDAClient client = new CDAClient.Builder()
    .setSpaceKey("cfexampleapi");
    .setAccessToken("b4c0n73n7fu1");
    .build();

// Create a `SyncConfig` object
SyncConfig config = SyncConfig.builder()
      .setClient(client)
      .build();

// Request sync
Vault.with(context, DemoSpace.class, config).requestSync();
```

Vault will use a worker thread to request incremental updates from the Sync API and reflect the remote changes to it's database.
Once sync is completed, Vault will fire a broadcast with the action `Vault.ACTION_SYNC_COMPLETE`.

Alternatively, you can provide a `SyncCallback` which will be invoked once sync is completed, but make sure to cancel it according to it's host lifecycle events:

```java
class SomeActivity extends Activity {
  SyncCallback callback;
  
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    Vault.with(this, DemoSpace.class).requestSync(config, callback = new SyncCallback() {
      @Override public void onComplete(boolean success) {
        // Sync completed \o/
      }
    });
  }
  
  @Override protected void onDestroy() {
    callback.cancel();
    super.onDestroy();
  }
}
```

### Queries

Vault provides an wrapper around it's generated database which can be easily used to fetch persisted objects, some examples:

```java
Vault vault = Vault.with(conext, DemoSpace.class);

// Fetch the first Cat
vault.fetch(Cat.class)
    .first();
    
// Fetch the most recently updated Cat
vault.fetch(Cat.class)
    .order("updated_at DESC")
    .first();

// Fetch a Cat with a specific name
vault.fetch(Cat.class)
    .where("name = ?", "Nyan Cat")
    .first();
    
// Fetch all Cats, oredered by creation date:
vault.fetch(Cat.class)
    .order("created_at")
    .all();
```