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

import static com.google.common.truth.Truth.assertThat;

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
    assertThat(request.getPath()).isEqualTo("/spaces/space/sync?sync_token=st1");
  }

  private void assertRequestInitial() throws InterruptedException {
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    assertThat(request.getPath()).isEqualTo("/spaces/space/sync?initial=true");
  }

  private void enqueueInitial() throws IOException {
    enqueue("demo/space.json");
    enqueue("demo/initial.json");
  }

  private void enqueueUpdate() throws IOException {
    enqueue("demo/space.json");
    enqueue("demo/update.json");
  }

  private void assertSingleLink() {
    Cat nyanCat = vault.fetch(Cat.class)
        .where("remote_id = ?", "nyancat")
        .first();

    assertThat(nyanCat).isNotNull();

    Cat happyCat = nyanCat.bestFriend();
    assertThat(happyCat).isNotNull();

    assertThat(nyanCat).isSameAs(happyCat.bestFriend());
  }

  private void assertInitialAssets() {
    List<Asset> assets = vault.fetch(Asset.class)
        .order("created_at")
        .all();

    assertThat(assets).isNotNull();
    assertThat(assets).hasSize(4);

    assertThat(assets.get(0).remoteId()).isEqualTo("nyancat");
    assertThat(assets.get(1).remoteId()).isEqualTo("jake");
    assertThat(assets.get(2).remoteId()).isEqualTo("happycat");
    assertThat(assets.get(2).url()).isEqualTo("http://happycat.jpg");
    assertThat(assets.get(3).remoteId()).isEqualTo("1x0xpXu4pSGS4OukSyWGUK");

    for (Asset asset : assets) {
      assertThat(asset.url()).isNotNull();
      assertThat(asset.mimeType()).isNotNull();
      assertThat(asset.remoteId()).isNotNull();
      assertThat(asset.updatedAt()).isNotNull();
    }
  }

  private void assertUpdateAssets() {
    List<Asset> assets = vault.fetch(Asset.class)
        .order("created_at")
        .all();

    assertThat(assets).isNotNull();
    assertThat(assets).hasSize(3);
    assertThat(assets.get(0).remoteId()).isEqualTo("nyancat");
    assertThat(assets.get(1).remoteId()).isEqualTo("happycat");
    assertThat(assets.get(1).url()).isEqualTo("http://happiercat.jpg");
    assertThat(assets.get(2).remoteId()).isEqualTo("1x0xpXu4pSGS4OukSyWGUK");

    for (Asset asset : assets) {
      assertThat(asset.url()).isNotNull();
      assertThat(asset.mimeType()).isNotNull();
      assertThat(asset.remoteId()).isNotNull();
      assertThat(asset.updatedAt()).isNotNull();
    }
  }

  private void assertInitialEntries() {
    List<Cat> cats = vault.fetch(Cat.class)
        .order("created_at")
        .all();

    assertThat(cats).isNotNull();
    assertThat(cats).hasSize(3);

    Cat nyanCat = cats.get(0);
    assertThat(nyanCat).isNotNull();
    assertThat(nyanCat.remoteId()).isEqualTo("nyancat");
    assertThat(nyanCat.image()).isNotNull();

    Cat happyCat = cats.get(1);
    assertThat(happyCat).isNotNull();
    assertThat(happyCat.remoteId()).isEqualTo("happycat");
    assertThat(happyCat.name()).isEqualTo("Happy Cat");
    assertThat(happyCat.image()).isNotNull();

    Cat garfield = cats.get(2);
    assertThat(garfield).isNotNull();
    assertThat(garfield.remoteId()).isEqualTo("garfield");
    assertThat(garfield.image()).isSameAs(happyCat.image());
    assertThat(garfield.bestFriend()).isNull();

    assertThat(nyanCat.bestFriend()).isEqualTo(happyCat);
    assertThat(happyCat.bestFriend()).isEqualTo(nyanCat);
  }

  private void assertUpdateEntries() {
    List<Cat> cats = vault.fetch(Cat.class)
        .order("created_at")
        .all();

    assertThat(cats).isNotNull();
    assertThat(cats).hasSize(3);
    assertThat(cats.get(0).name()).isEqualTo("Happier Cat");
    assertThat(cats.get(0).remoteId()).isEqualTo("happycat");
    assertThat(cats.get(1).remoteId()).isEqualTo("garfield");
    assertThat(cats.get(2).remoteId()).isEqualTo("supercat");
  }
}
