package com.contentful.sqlite;

import android.content.Context;
import com.contentful.java.cda.CDAClient;
import java.util.LinkedHashMap;
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
}
