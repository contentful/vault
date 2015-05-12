package com.contentful.vaultintegration;

import com.contentful.vault.Asset;
import com.contentful.vault.Vault;
import com.contentful.vaultintegration.lib.demo.Cat;
import com.contentful.vaultintegration.lib.demo.DemoSpace;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import java.io.IOException;
import java.util.List;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class SyncTest extends BaseTest {
  @Override protected void setupVault() {
    vault = Vault.with(RuntimeEnvironment.application, DemoSpace.class);
  }

  @Test public void testSync() throws Exception {
    // Initial
    enqueueInitial();
    sync();
    assertSyncInitial();

    // Update
    enqueueUpdate();
    sync();
    assertSyncUpdate();
  }

  private void assertSyncUpdate() throws InterruptedException {
    assertRequestUpdate();
    assertUpdateAssets();
    assertUpdateEntries();
  }

  private void assertSyncInitial() throws InterruptedException {
    assertRequestInitial();
    assertInitialAssets();
    assertInitialEntries();
    assertSingleLink();
  }

  private void assertRequestUpdate() throws InterruptedException {
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    assertEquals("/spaces/space/sync?sync_token=st1", request.getPath());
  }

  private void assertRequestInitial() throws InterruptedException {
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    assertEquals("/spaces/space/sync?initial=true", request.getPath());
  }

  private void enqueueInitial() throws IOException {
    enqueue("space.json");
    enqueue("initial.json");
  }

  private void enqueueUpdate() throws IOException {
    enqueue("space.json");
    enqueue("update.json");
  }

  private void assertSingleLink() {
    Cat nyanCat = vault.fetch(Cat.class)
        .where("remote_id = ?", "nyancat")
        .first();

    assertNotNull(nyanCat);
    Cat happyCat = nyanCat.bestFriend;
    assertNotNull(happyCat);
    assertSame(nyanCat, happyCat.bestFriend);
  }

  private void assertInitialAssets() {
    List<Asset> assets = vault.fetch(Asset.class)
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

  private void assertUpdateAssets() {
    List<Asset> assets = vault.fetch(Asset.class)
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

  private void assertInitialEntries() {
    List<Cat> cats = vault.fetch(Cat.class)
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

  private void assertUpdateEntries() {
    List<Cat> cats = vault.fetch(Cat.class)
        .order("created_at")
        .all();

    Cat happyCat = cats.get(0);
    assertEquals("happycat", happyCat.getRemoteId());
    assertEquals("Happier Cat", happyCat.name);

    assertEquals("garfield", cats.get(1).getRemoteId());
    assertEquals("supercat", cats.get(2).getRemoteId());
  }
}
