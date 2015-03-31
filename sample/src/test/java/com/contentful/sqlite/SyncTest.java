package com.contentful.sqlite;

import android.content.Context;
import com.contentful.java.cda.CDAClient;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import retrofit.RestAdapter.LogLevel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml")
public class SyncTest {
  MockWebServer server;
  CDAClient client;

  @Space(value = "cfexampleapi", models = Cat.class)
  static class Cfexampleapi {
  }

  @ContentType("cat")
  static class Cat extends Resource {
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

  @Test public void testInitialSync() throws Exception {
    Context context = Robolectric.application;

    // Initial
    enqueue("space.json");
    enqueue("initial.json");
    new SyncRunnable(context, Cfexampleapi.class, client).run();
    DbHelper helper = Persistence.getOrCreateDbHelper(context, Cfexampleapi.class);
    assertInitialAssets(helper);
    assertInitialEntries(helper);

    /*
    // Update
    enqueue("update.json");
    new SyncRunnable(context, Cfexampleapi.class, client).run();
    assertDeltaAssets(helper);
    */
  }

  private void assertInitialAssets(DbHelper helper) {
    List<Asset> assets = Persistence.fetch(helper, Asset.class)
        .order("created_at")
        .all();

    assertEquals(4, assets.size());
    assertEquals("nyancat", assets.get(0).getRemoteId());
    assertEquals("jake", assets.get(1).getRemoteId());
    assertEquals("happycat", assets.get(2).getRemoteId());
    assertEquals("1x0xpXu4pSGS4OukSyWGUK", assets.get(3).getRemoteId());

    for (Asset asset : assets) {
      assertNotNull(asset.getUrl());
      assertNotNull(asset.getMimeType());
      assertNotNull(asset.getCreatedAt());
      assertNotNull(asset.getUpdatedAt());
    }
  }

  private void assertInitialEntries(DbHelper helper) {
    /*
    TODO
    List<Cat> cats = Persistence.fetch(helper, Cat.class)
        .order("created_at")
        .all();

    */
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
