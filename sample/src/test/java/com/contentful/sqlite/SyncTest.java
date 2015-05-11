package com.contentful.sqlite;

import android.content.Context;
import com.contentful.java.cda.CDAClient;
import com.contentful.sqlite.lib.Cat;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import java.util.List;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class SyncTest extends BaseTest {
  @Space(value = "cfexampleapi", models = Cat.class)
  static class DemoSpace {
  }

  @Test public void testSync() throws Exception {
    Context context = RuntimeEnvironment.application;

    // Initial
    enqueue("space.json");
    enqueue("initial.json");
    sync(context, DemoSpace.class, client);

    // Request
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    assertEquals("/spaces/space/sync?initial=true", request.getPath());

    assertInitialAssets(context);
    assertInitialEntries(context);
    assertSingleLink(context);

    // Update
    enqueue("space.json");
    enqueue("update.json");
    sync(context, DemoSpace.class, client);

    // Request
    server.takeRequest();
    request = server.takeRequest();
    assertEquals("/spaces/space/sync?sync_token=st1", request.getPath());

    // Resources
    assertUpdateAssets(context);
    assertUpdateEntries(context);
  }

  private void sync(Context context, Class<?> space, CDAClient client) {
    SyncRunnable.builder()
        .setContext(context)
        .setSpace(space)
        .setSyncConfig(SyncConfig.builder().setClient(client).build())
        .build()
        .run();
  }

  private void assertSingleLink(Context context) {
    Cat nyanCat = Persistence.with(context, DemoSpace.class).fetch(Cat.class)
        .where("remote_id = ?", "nyancat")
        .first();

    assertNotNull(nyanCat);
    Cat happyCat = nyanCat.bestFriend;
    assertNotNull(happyCat);
    assertSame(nyanCat, happyCat.bestFriend);
  }

  private void assertInitialAssets(Context context) {
    List<Asset> assets = Persistence.with(context, DemoSpace.class).fetch(Asset.class)
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
    List<Asset> assets = Persistence.with(context, DemoSpace.class).fetch(Asset.class)
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
    List<Cat> cats = Persistence.with(context, DemoSpace.class).fetch(Cat.class)
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
    List<Cat> cats = Persistence.with(context, DemoSpace.class).fetch(Cat.class)
        .order("created_at")
        .all();

    Cat happyCat = cats.get(0);
    assertEquals("happycat", happyCat.getRemoteId());
    assertEquals("Happier Cat", happyCat.name);

    assertEquals("garfield", cats.get(1).getRemoteId());
    assertEquals("supercat", cats.get(2).getRemoteId());
  }
}
