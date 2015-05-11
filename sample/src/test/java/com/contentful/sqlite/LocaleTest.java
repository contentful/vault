package com.contentful.sqlite;

import android.content.Context;
import com.contentful.sqlite.lib.Cat;
import java.io.IOException;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertEquals;

public class LocaleTest extends BaseTest {
  @Space(value = "cfexampleapi", models = Cat.class)
  static class DemoSpace {
  }

  @Test public void testLocale() throws Exception {
    Context context = RuntimeEnvironment.application;

    checkDefaultLocale(context);
    checkCustomLocale(context);
  }

  private void checkCustomLocale(Context context) throws IOException {
    // Initial (Klingon)
    enqueue("space.json");
    enqueue("initial.json");

    SyncRunnable.builder()
        .setContext(context)
        .setSpace(DemoSpace.class)
        .setSyncConfig(SyncConfig.builder()
            .setClient(client).setLocale("tlh")
            .build())
        .build()
        .run();

    Cat cat = Persistence.with(context, DemoSpace.class)
        .fetch(Cat.class)
        .first();

    assertEquals("Quch vIghro'", cat.name);
  }

  private void checkDefaultLocale(Context context) throws IOException {
    // Initial (default locale)
    enqueue("space.json");
    enqueue("initial.json");

    SyncRunnable.builder()
        .setContext(context)
        .setSpace(DemoSpace.class)
        .setSyncConfig(SyncConfig.builder().setClient(client).build())
        .build()
        .run();

    Cat cat = Persistence.with(context, DemoSpace.class)
        .fetch(Cat.class)
        .first();

    assertEquals("Happy Cat", cat.name);
  }
}
