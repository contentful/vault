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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

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
    assertInitialAssets(context);
    assertInitialEntries(context);
    assertSingleLink(context);

    /*
    // Update
    enqueue("update.json");
    new SyncRunnable(context, Cfexampleapi.class, client).run();
    assertUpdateAssets(helper);
    */
  }

  private void assertSingleLink(Context context) {
    Cat nyanCat = Persistence.fetch(context, Cfexampleapi.class, Cat.class)
        .where("remote_id = ?", "nyancat")
        .first();

    assertNotNull(nyanCat);
    Cat happyCat = nyanCat.bestFriend;
    assertNotNull(happyCat);
    assertSame(nyanCat, happyCat.bestFriend);
  }

  private void assertInitialAssets(Context context) {
    List<Asset> assets = Persistence.fetch(context, Cfexampleapi.class, Asset.class)
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

  private void assertInitialEntries(Context context) {
    List<Cat> cats = Persistence.fetch(context, Cfexampleapi.class, Cat.class)
        .order("created_at")
        .all();

    assertEquals(3, cats.size());

    Cat nyanCat = cats.get(0);
    assertEquals("nyancat", nyanCat.getRemoteId());

    Cat happyCat = cats.get(1);
    assertEquals("happycat", happyCat.getRemoteId());

    Cat garfield = cats.get(2);
    assertEquals("garfield", garfield.getRemoteId());

    assertSame(happyCat, nyanCat.bestFriend);
    assertSame(nyanCat, happyCat.bestFriend);
    assertNull(garfield.bestFriend);
  }

  private String getServerUrl() {
    URL url = server.getUrl("/");
    return url.getHost() + ":" + url.getPort();
  }

  private void enqueue(String fileName) throws IOException {
    URL resource = getClass().getClassLoader().getResource(fileName);
    if (resource == null) {
      throw new IllegalArgumentException("File not found");
    }
    server.enqueue(new MockResponse().setResponseCode(200).setBody(FileUtils.readFileToString(
            new File(resource.getFile()))));
  }
}
