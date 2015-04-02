package com.contentful.sqlite;

import android.content.Context;
import com.contentful.java.cda.CDAClient;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Persistence {
  private Persistence() {
    throw new AssertionError();
  }

  static final Map<Class<?>, DbHelper> INJECTORS = new LinkedHashMap<Class<?>, DbHelper>();

  private static final ExecutorService executor = Executors.newSingleThreadExecutor(
      new CFThreadFactory());

  public static void requestSync(Context context, Class<?> space, CDAClient client) {
    if (context == null) {
      throw new IllegalArgumentException("Cannot be invoked with null context.");
    }
    if (space == null) {
      throw new IllegalArgumentException("Cannot be invoked with null space.");
    }
    if (client == null) {
      throw new IllegalArgumentException("Cannot be invoked with null client.");
    }

    executor.submit(new SyncRunnable(context, space, client));
  }

  static DbHelper getOrCreateDbHelper(Context context, Class<?> space) {
    synchronized (Persistence.INJECTORS) {
      DbHelper helper = Persistence.INJECTORS.get(space);
      if (helper == null) {
          helper = Persistence.createDbHelper(context, space);
          Persistence.INJECTORS.put(space, helper);
      }
      return helper;
    }
  }

  static DbHelper createDbHelper(Context context, Class<?> space) {
    try {
      Class<?> clazz = Class.forName(space.getName() + Constants.SUFFIX_SPACE);
      Method get = clazz.getMethod("get", Context.class);
      DbHelper helper = (DbHelper) get.invoke(null, context);
      if (helper == null) {
        throw new IllegalArgumentException(
            "Space injector returned empty helper for class \"" + space.getName());
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

  public static <T extends Resource> FutureQuery<T> fetch(Context context, Class<?> space,
      Class<T> resource) {
    DbHelper helper = getOrCreateDbHelper(context, space);
    return fetch(helper, resource);
  }

  public static <T extends Resource> FutureQuery<T> fetch(DbHelper helper, Class<T> resource) {
    String tableName;
    if (Asset.class.equals(resource)) {
      tableName = DbHelper.TABLE_ASSETS;
    } else {
      tableName = helper.getTablesMap().get(resource);
      if (tableName == null) {
        throw new IllegalArgumentException(
            "Unable to find table mapping for class \"" + resource);
      }
    }
    List<FieldMeta> fields = helper.getFieldsMap().get(resource);
    return new FutureQuery<T>(helper, resource, tableName,
        fields);
  }
}
