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
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
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
    @Field String name;
    @Field Cat bestFriend;
    @Field Asset image;
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

  @Test public void testSync() throws Exception {
    Context context = RuntimeEnvironment.application;

    // Initial
    enqueue("space.json");
    enqueue("initial.json");
    sync(context, Cfexampleapi.class, client);

    assertInitialAssets(context);
    assertInitialEntries(context);
    assertSingleLink(context);

    // Update
    enqueue("space.json");
    enqueue("update.json");
    sync(context, Cfexampleapi.class, client);
    assertUpdateAssets(context);
    assertUpdateEntries(context);
  }

  private void sync(Context context, Class<?> space, CDAClient client) {
    SyncRunnable.builder()
        .setContext(context)
        .setSpace(space)
        .setClient(client)
        .build()
        .run();
  }

  private void assertSingleLink(Context context) {
    Cat nyanCat = Persistence.with(context, Cfexampleapi.class).fetch(Cat.class)
        .where("remote_id = ?", "nyancat")
        .first();

    assertNotNull(nyanCat);
    Cat happyCat = nyanCat.bestFriend;
    assertNotNull(happyCat);
    assertSame(nyanCat, happyCat.bestFriend);
  }

  private void assertInitialAssets(Context context) {
    List<Asset> assets = Persistence.with(context, Cfexampleapi.class).fetch(Asset.class)
        .order("created_at")
        .all();

    assertEquals(4, assets.size());
    assertEquals("nyancat", assets.get(0).getRemoteId());
    assertEquals("jake", assets.get(1).getRemoteId());
    assertEquals("happycat", assets.get(2).getRemoteId());
    assertEquals("1x0xpXu4pSGS4OukSyWGUK", assets.get(3).getRemoteId());
    assertEquals("http://happycat.jpg", assets.get(2).getUrl());

    for (Asset asset : assets) {
      assertNotNull(asset.getUrl());
      assertNotNull(asset.getMimeType());
      assertNotNull(asset.getCreatedAt());
      assertNotNull(asset.getUpdatedAt());
    }
  }

  private void assertUpdateAssets(Context context) {
    List<Asset> assets = Persistence.with(context, Cfexampleapi.class).fetch(Asset.class)
        .order("created_at")
        .all();

    assertEquals(3, assets.size());
    assertEquals("nyancat", assets.get(0).getRemoteId());
    assertEquals("happycat", assets.get(1).getRemoteId());
    assertEquals("1x0xpXu4pSGS4OukSyWGUK", assets.get(2).getRemoteId());

    assertEquals("http://happiercat.jpg", assets.get(1).getUrl());

    for (Asset asset : assets) {
      assertNotNull(asset.getUrl());
      assertNotNull(asset.getMimeType());
      assertNotNull(asset.getCreatedAt());
      assertNotNull(asset.getUpdatedAt());
    }
  }

  private void assertInitialEntries(Context context) {
    List<Cat> cats = Persistence.with(context, Cfexampleapi.class).fetch(Cat.class)
        .order("created_at")
        .all();

    assertEquals(3, cats.size());

    Cat nyanCat = cats.get(0);
    assertEquals("nyancat", nyanCat.getRemoteId());
    assertNotNull(nyanCat.image);

    Cat happyCat = cats.get(1);
    assertEquals("happycat", happyCat.getRemoteId());
    assertEquals("Happy Cat", happyCat.name);
    assertNotNull(happyCat.image);

    Cat garfield = cats.get(2);
    assertEquals("garfield", garfield.getRemoteId());
    assertNotNull(garfield.image);
    assertSame(happyCat.image, garfield.image);

    assertSame(happyCat, nyanCat.bestFriend);
    assertSame(nyanCat, happyCat.bestFriend);
    assertNull(garfield.bestFriend);
  }

  private void assertUpdateEntries(Context context) {
    List<Cat> cats = Persistence.with(context, Cfexampleapi.class).fetch(Cat.class)
        .order("created_at")
        .all();

    Cat happyCat = cats.get(0);
    assertEquals("happycat", happyCat.getRemoteId());
    assertEquals("Happier Cat", happyCat.name);

    assertEquals("garfield", cats.get(1).getRemoteId());
    assertEquals("supercat", cats.get(2).getRemoteId());
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
