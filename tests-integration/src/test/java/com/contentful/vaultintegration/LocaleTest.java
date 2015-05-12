package com.contentful.vaultintegration;

import android.content.Context;
import com.contentful.vault.Space;
import com.contentful.vaultintegration.lib.Cat;
import java.io.IOException;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

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
    /*
    TODO
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
    */
  }

  private void checkDefaultLocale(Context context) throws IOException {
    /*
    TODO
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
    */
  }
}
