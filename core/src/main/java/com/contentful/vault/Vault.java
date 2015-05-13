package com.contentful.vault;

import android.content.Context;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit.android.MainThreadExecutor;

public class Vault {
  public static final String ACTION_SYNC_COMPLETE = "com.contentful.vault.ACTION_SYNC_COMPLETE";
  public static final String EXTRA_SUCCESS = "EXTRA_SUCCESS";

  static final Map<Class<?>, SqliteHelper> SQLITE_HELPERS =
      new LinkedHashMap<Class<?>, SqliteHelper>();

  static final ExecutorService EXECUTOR_SYNC = Executors.newSingleThreadExecutor(
      new CFThreadFactory());

  static final Executor EXECUTOR_CALLBACK = new MainThreadExecutor();

  static final Map<String, CallbackBundle> CALLBACKS = new HashMap<String, CallbackBundle>();

  final Context context;
  final Class<?> space;

  private Vault(Context context, Class<?> space) {
    this.context = context.getApplicationContext();
    this.space = space;
  }

  public static Vault with(Context context, Class<?> space) {
    if (context == null) {
      throw new IllegalArgumentException("Cannot be invoked with null context.");
    }
    if (space == null) {
      throw new IllegalArgumentException("Cannot be invoked with null space.");
    }
    return new Vault(context, space);
  }

  public void requestSync(SyncConfig config) {
    requestSync(config, null);
  }

  public void requestSync(SyncConfig config, SyncCallback callback) {
    requestSync(config, callback, null);
  }

  public void requestSync(SyncConfig config, SyncCallback callback, Executor callbackExecutor) {
    if (config == null) {
      throw new IllegalArgumentException("Cannot be invoked with null configuration.");
    }
    if (config.client == null) {
      throw new IllegalArgumentException("Cannot be invoked with null client.");
    }

    String tag = Long.toString(System.currentTimeMillis());

    if (callback != null) {
      callback.setTag(tag);

      if (callbackExecutor == null) {
        callbackExecutor = EXECUTOR_CALLBACK;
      }

      CallbackBundle bundle = new CallbackBundle(callback, callbackExecutor);
      synchronized (CALLBACKS) {
        CALLBACKS.put(tag, bundle);
      }
    }

    EXECUTOR_SYNC.submit(SyncRunnable.builder()
        .setTag(tag)
        .setContext(context)
        .setSqliteHelper(getOrCreateSqliteHelper(context, space))
        .setSyncConfig(config)
        .setCallbackExecutor(callbackExecutor)
        .build());
  }

  static SqliteHelper getOrCreateSqliteHelper(Context context, Class<?> space) {
    synchronized (SQLITE_HELPERS) {
      SqliteHelper sqliteHelper = SQLITE_HELPERS.get(space);
      if (sqliteHelper == null) {
        SpaceHelper spaceHelper = createSpaceHelper(space);
        sqliteHelper = createSqliteHelper(context, spaceHelper);
        SQLITE_HELPERS.put(space, sqliteHelper);
      }
      return sqliteHelper;
    }
  }

  private static SqliteHelper createSqliteHelper(Context context, SpaceHelper spaceHelper) {
    return new SqliteHelper(context, spaceHelper);
  }

  private static SpaceHelper createSpaceHelper(Class<?> space) {
    try {
      Class<?> clazz = Class.forName(space.getName() + Constants.SUFFIX_SPACE);
      return (SpaceHelper) clazz.newInstance();
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    }
  }

  public <T extends Resource> Query<T> fetch(Class<T> resource) {
    SqliteHelper sqliteHelper = getOrCreateSqliteHelper(context, space);
    SpaceHelper spaceHelper = sqliteHelper.getSpaceHelper();

    String tableName;
    List<FieldMeta> fields = null;
    if (Asset.class.equals(resource)) {
      tableName = SpaceHelper.TABLE_ASSETS;
    } else {
      ModelHelper<?> modelHelper = getModelHelperOrThrow(spaceHelper, resource);
      tableName = modelHelper.getTableName();
      fields = modelHelper.getFields();
    }
    return new Query<T>(this, sqliteHelper, resource, tableName, fields);
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

  static void executeCallback(String tag, final boolean success) {
    final CallbackBundle bundle = clearBundle(tag);
    if (bundle != null) {
      bundle.executor.execute(new Runnable() {
        @Override public void run() {
          bundle.callback.onComplete(success);
        }
      });
    }
  }

  static CallbackBundle clearBundle(String tag) {
    synchronized (CALLBACKS) {
      return CALLBACKS.remove(tag);
    }
  }

  public static void cancel(SyncCallback callback) {
    if (callback == null) {
      throw new IllegalArgumentException("callback argument must not be null.");
    }

    String tag = callback.getTag();
    if (tag != null) {
      clearBundle(tag);
    }
  }

  public void releaseAll() {
    synchronized (SQLITE_HELPERS) {
      for (SqliteHelper spaceHelper : SQLITE_HELPERS.values()) {
        spaceHelper.close();
      }
      SQLITE_HELPERS.clear();
    }
  }

  static class CallbackBundle {
    final SyncCallback callback;
    final Executor executor;

    public CallbackBundle(SyncCallback callback, Executor executor) {
      this.callback = callback;
      this.executor = executor;
    }
  }
}
