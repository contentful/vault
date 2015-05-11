package com.contentful.sqlite;

import android.content.Context;
import com.contentful.java.cda.CDAClient;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import retrofit.RestAdapter;

import static org.junit.Assert.assertEquals;

public class LocaleTest extends BaseTest {
  @Space(value = "y005y7p7nrqo", models = Text.class, localeCode = "es-ES")
  static class TestSpace {
  }

  @ContentType("6yhJBtk1nUYq0iumy6gMY4")
  static class Text extends Resource {
    @Field String text;
  }

  @Override protected void setupClient() {
    client = new CDAClient.Builder()
        .setLogLevel(RestAdapter.LogLevel.FULL)
        .setSpaceKey("y005y7p7nrqo") // TODO use recordings once all entries are formed
        .setAccessToken("4ebade7cb044ffa992861e10bbdf4ef486868bf08e5e708355aa0f85e5651a2a")
        .build();
  }

  @Test public void testLocale() throws Exception {
    Context context = RuntimeEnvironment.application;

    SyncRunnable.builder()
        .setContext(RuntimeEnvironment.application)
        .setSpace(TestSpace.class)
        .setClient(client)
        .build()
        .run();

    Text text = Persistence.with(context, TestSpace.class).fetch(Text.class).first();
    assertEquals("español", text.text);
  }
}
