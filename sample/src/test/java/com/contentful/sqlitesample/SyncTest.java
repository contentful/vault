package com.contentful.sqlitesample;

import android.content.Context;
import com.contentful.java.cda.CDAClient;
import com.contentful.sqlite.ContentType;
import com.contentful.sqlite.Field;
import com.contentful.sqlite.Space;
import com.contentful.sqlite.SyncRunnable;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import retrofit.RestAdapter.LogLevel;

@RunWith(RobolectricTestRunner.class) //
@Config(manifest = "./src/main/AndroidManifest.xml")
public class SyncTest {
  MockWebServer server;
  CDAClient client;

  @Space(value = "cfexampleapi", models = Cat.class)
  static class Cfexampleapi {
  }

  @ContentType("63k4qdEi9aI8IQUGaYGg4O")
  class Cat {
    @Field("name") String nameField;
    @Field(value = "bestFriend", link = true) Cat bestFriend;
  }

  @Before public void setUp() throws Exception {
    server = new MockWebServer();
    server.start();

    client = new CDAClient.Builder()
        .setSpaceKey("space")
        .setAccessToken("token")
        .setEndpoint(getServerUrl())
        .setLogLevel(LogLevel.FULL)
        .noSSL()
        .build();
  }

  @After public void tearDown() throws Exception {
    server.shutdown();
  }

  @Test public void testTom() throws Exception {
    Context context = Robolectric.application;

    enqueue("space.json");
    enqueue("initial.json");

    new SyncRunnable(context, Cfexampleapi.class, client).run();
  }

  private String getServerUrl() {
    URL url = server.getUrl("/");
    return url.getHost() + ":" + url.getPort();
  }

  private void enqueue(String fileName) throws IOException {
    server.enqueue(new MockResponse().setResponseCode(200).setBody(FileUtils.readFileToString(
            new File(getClass().getClassLoader().getResource(fileName).getFile()))));
  }
}
