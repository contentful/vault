/*
 * Copyright (C) 2015 Contentful GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.contentful.vault;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit.android.MainThreadExecutor;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public class Vault {
  public static final String ACTION_SYNC_COMPLETE = "com.contentful.vault.ACTION_SYNC_COMPLETE";

  public static final String EXTRA_SUCCESS = "EXTRA_SUCCESS";

  static final Locale LOCALE = Locale.US;

  static final Map<Class<?>, SqliteHelper> SQLITE_HELPERS = new LinkedHashMap<>();

  static final ExecutorService EXECUTOR_SYNC = Executors.newSingleThreadExecutor(
      new VaultThreadFactory());

  static final Executor EXECUTOR_CALLBACK = new MainThreadExecutor();

  static final Map<String, CallbackBundle> CALLBACKS = new HashMap<>();

  static final Subject<SyncResult, SyncResult> SYNC_SUBJECT =
      new SerializedSubject<>(PublishSubject.<SyncResult>create());

  private final Context context;

  private final Class<?> space;

  private final SqliteHelper sqliteHelper;

  private Vault(Context context, Class<?> space, SqliteHelper sqliteHelper) {
    this.context = context.getApplicationContext();
    this.space = space;
    this.sqliteHelper = sqliteHelper;
  }

  public static Vault with(Context context, Class<?> space) {
    if (context == null) {
      throw new IllegalArgumentException("Cannot be invoked with null context.");
    }
    if (space == null) {
      throw new IllegalArgumentException("Cannot be invoked with null space.");
    }
    return new Vault(context, space, getOrCreateSqliteHelper(context, space));
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
    if (config.client() == null) {
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
        .setSqliteHelper(sqliteHelper)
        .setSyncConfig(config)
        .build());
  }

  public <T extends Resource> FetchQuery<T> fetch(Class<T> type) {
    return new FetchQuery<>(type, this);
  }

  public <T extends Resource> ObserveQuery<T> observe(Class<T> type) {
    return new ObserveQuery<>(type, this);
  }

  public SQLiteDatabase getReadableDatabase() {
    return sqliteHelper.getReadableDatabase();
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

  public static Observable<SyncResult> observeSyncResults() {
    return SYNC_SUBJECT.asObservable();
  }

  private static SqliteHelper createSqliteHelper(Context context, SpaceHelper spaceHelper) {
    return new SqliteHelper(context, spaceHelper);
  }

  private static SpaceHelper createSpaceHelper(Class<?> space) {
    try {
      Class<?> clazz = Class.forName(space.getName() + Constants.SUFFIX_SPACE);
      return (SpaceHelper) clazz.newInstance();
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Cannot find generated class for space: " + space.getName(), e);
    } catch (IllegalAccessException | InstantiationException e) {
      throw new RuntimeException(e);
    }
  }

  SqliteHelper getSqliteHelper() {
    return sqliteHelper;
  }

  private static SqliteHelper getOrCreateSqliteHelper(Context context, Class<?> space) {
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

  static void executeCallback(String tag, final SyncResult result) {
    final CallbackBundle bundle = clearBundle(tag);
    if (bundle != null) {
      bundle.executor.execute(new Runnable() {
        @Override public void run() {
          bundle.callback.onResult(result);
        }
      });
    }
  }

  static CallbackBundle clearBundle(String tag) {
    synchronized (CALLBACKS) {
      return CALLBACKS.remove(tag);
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
