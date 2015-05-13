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

### Models

Models are defined by declaring a class, the class should be a subclass of the `Resource` class. 
Annotate the class with `@ContentType`, which takes the Content Type's ID as it's value.

Fields are defined by annotating class attributes with the `@Field` annotation. 
```java
@ContentType("cat")
public class Cat extends Resource {
  @Field public String name;
  @Field public Cat bestFriend;
  @Field public Asset image;
}
```

The ID of the field will by inferred by the name of the attribute, unless provided explicitly as the value for the `@Field` annotation:
```java
@Field("field-id-goes-here") 
public String someField; 
```

### Spaces

Spaces can be defined by declaring a class, annotated with the `@Space` annotation. It is also required to provide the Space ID and an array of model classes to be included:

```java
@Space(value = "cfexampleapi", models = { Cat.class })
public class DemoSpace { }
```

### Synchronization

Once a Space is defined, you can tell Vault to sync the local database with the remote state:

```java
Vault.with(context, DemoSpace.class).requestSync();
```

Vault will use the Contentful Sync API to request delta updates of the data and reflect the remote changes to the db.
Once sync is completed, Vault will fire a broadcast with the action `Vault.ACTION_SYNC_COMPLETE`.

Alternatively, you can provide a `SyncCallback` which will be invoked once sync is completed, but make sure to cancel it according to it's host lifecycle events:

```java
class SomeActivity extends Activity {
  SyncCallback callback;
  
  @Override protected void onCreate(Bundle savedInstanceState) {
    // ...
    Vault.with(this, DemoSpace.class).requestSync(callback = new SyncCallback() {
      @Override public void onComplete(boolean success) {
        // Sync completed \o/
      }
    });
  }
  
  @Override protected void onDestroy() {
    // ...
    callback.cancel();
  }
}
```

In addition, it is possible to provide a specific sync configuration, one example would be selecting which locale to persist when saving resources:

```java
// Sync DemoSpace using the "tlh" locale code (Klingon)
Vault.with(context, DemoSpace.class).requestSync(
  SyncConfig.builder().setLocale("tlh").build());
```
