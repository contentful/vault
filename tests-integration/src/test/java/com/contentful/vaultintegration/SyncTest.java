/*
 * Copyright (C) 2015 Contentful GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.contentful.vaultintegration;

import com.contentful.vault.Asset;
import com.contentful.vault.SyncConfig;
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

  @Test public void testSyncInvalidate() throws Exception {
    // Initial
    enqueueInitial();
    sync();
    assertSyncInitial();

    // Initial (invalidate)
    enqueueInitial();
    sync(SyncConfig.builder().setClient(client).setInvalidate(true).build());
    assertSyncInitial();
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
    enqueue("demo_space.json");
    enqueue("demo_initial.json");
  }

  private void enqueueUpdate() throws IOException {
    enqueue("demo_space.json");
    enqueue("demo_update.json");
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
    assertEquals("nyancat", assets.get(0).remoteId());
    assertEquals("jake", assets.get(1).remoteId());
    assertEquals("happycat", assets.get(2).remoteId());
    assertEquals("1x0xpXu4pSGS4OukSyWGUK", assets.get(3).remoteId());
    assertEquals("http://happycat.jpg", assets.get(2).url());

    for (Asset asset : assets) {
      assertNotNull(asset.url());
      assertNotNull(asset.mimeType());
      assertNotNull(asset.remoteId());
      assertNotNull(asset.updatedAt());
    }
  }

  private void assertUpdateAssets() {
    List<Asset> assets = vault.fetch(Asset.class)
        .order("created_at")
        .all();

    assertEquals(3, assets.size());
    assertEquals("nyancat", assets.get(0).remoteId());
    assertEquals("happycat", assets.get(1).remoteId());
    assertEquals("1x0xpXu4pSGS4OukSyWGUK", assets.get(2).remoteId());

    assertEquals("http://happiercat.jpg", assets.get(1).url());

    for (Asset asset : assets) {
      assertNotNull(asset.url());
      assertNotNull(asset.mimeType());
      assertNotNull(asset.createdAt());
      assertNotNull(asset.updatedAt());
    }
  }

  private void assertInitialEntries() {
    List<Cat> cats = vault.fetch(Cat.class)
        .order("created_at")
        .all();

    assertEquals(3, cats.size());

    Cat nyanCat = cats.get(0);
    assertEquals("nyancat", nyanCat.remoteId());
    assertNotNull(nyanCat.image);

    Cat happyCat = cats.get(1);
    assertEquals("happycat", happyCat.remoteId());
    assertEquals("Happy Cat", happyCat.name);
    assertNotNull(happyCat.image);

    Cat garfield = cats.get(2);
    assertEquals("garfield", garfield.remoteId());
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
    assertEquals("happycat", happyCat.remoteId());
    assertEquals("Happier Cat", happyCat.name);

    assertEquals("garfield", cats.get(1).remoteId());
    assertEquals("supercat", cats.get(2).remoteId());
  }
}
