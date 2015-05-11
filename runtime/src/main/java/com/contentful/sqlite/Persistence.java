package com.contentful.sqlite;

import android.content.Context;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit.android.MainThreadExecutor;

public class Persistence {
  public static final String ACTION_SYNC_COMPLETE = "com.contentful.sqlite.ACTION_SYNC_COMPLETE";

  static final Map<Class<?>, SpaceHelper> SPACE_HELPERS =
      new LinkedHashMap<Class<?>, SpaceHelper>();

  static final ExecutorService syncExecutor = Executors.newSingleThreadExecutor(
      new CFThreadFactory());

  static final Executor callbackExecutor = new MainThreadExecutor();

  final Context context;
  final Class<?> space;

  private Persistence(Context context, Class<?> space) {
    this.context = context.getApplicationContext();
    this.space = space;
  }

  public static Persistence with(Context context, Class<?> space) {
    if (context == null) {
      throw new IllegalArgumentException("Cannot be invoked with null context.");
    }
    if (space == null) {
      throw new IllegalArgumentException("Cannot be invoked with null space.");
    }
    return new Persistence(context, space);
  }

  public void requestSync(SyncConfig config) {
    requestSync(config, null);
  }

  public void requestSync(SyncConfig config, SyncCallback callback) {
    if (config == null) {
      throw new IllegalArgumentException("Cannot be invoked with null configuration.");
    }
    if (config.client == null) {
      throw new IllegalArgumentException("Cannot be invoked with null client.");
    }

    syncExecutor.submit(SyncRunnable.builder()
        .setContext(context)
        .setSpace(space)
        .setSyncConfig(config)
        .setCallback(callback)
        .setCallbackExecutor(callbackExecutor)
        .build());
  }

  static SpaceHelper getOrCreateHelper(Context context, Class<?> space) {
    synchronized (SPACE_HELPERS) {
      SpaceHelper helper = SPACE_HELPERS.get(space);
      if (helper == null) {
          helper = Persistence.createHelper(context, space);
          SPACE_HELPERS.put(space, helper);
      }
      return helper;
    }
  }

  static SpaceHelper createHelper(Context context, Class<?> space) {
    try {
      Class<?> clazz = Class.forName(space.getName() + Constants.SUFFIX_SPACE);
      Method get = clazz.getMethod("get", Context.class);
      SpaceHelper helper = (SpaceHelper) get.invoke(null, context);
      if (helper == null) {
        throw new IllegalArgumentException(
            "Space injector has no helper for class \"" + space.getName());
      }
      return helper;
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  public <T extends Resource> Query<T> fetch(Class<T> resource) {
    SpaceHelper spaceHelper = getOrCreateHelper(context, space);
    return fetch(spaceHelper, resource);
  }

  public <T extends Resource> Query<T> fetch(SpaceHelper spaceHelper,
      Class<T> resource) {
    String tableName;
    List<FieldMeta> fields = null;
    if (Asset.class.equals(resource)) {
      tableName = SpaceHelper.TABLE_ASSETS;
    } else {
      ModelHelper<?> modelHelper = getModelHelperOrThrow(spaceHelper, resource);
      tableName = modelHelper.getTableName();
      fields = modelHelper.getFields();
    }
    return new Query<T>(this, spaceHelper, resource, tableName, fields);
  }

  private <T extends Resource> ModelHelper<?> getModelHelperOrThrow(SpaceHelper spaceHelper,
      Class<T> clazz) {
    ModelHelper<?> modelHelper = spaceHelper.getModels().get(clazz);
    if (modelHelper == null) {
      throw new IllegalArgumentException(
          "Unable to find table mapping for class \"" + clazz.getName() + "\".");
    }
    return modelHelper;
  }
}
