package com.contentful.vaultintegration;

import com.contentful.vault.Asset;
import com.contentful.vault.Vault;
import com.contentful.vaultintegration.lib.demo.Cat;
import com.contentful.vaultintegration.lib.demo.DemoSpace;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.robolectric.RuntimeEnvironment;

import java.io.IOException;
import java.util.List;

import static com.contentful.vault.BaseFields.CREATED_AT;
import static com.contentful.vault.BaseFields.REMOTE_ID;
import static com.google.common.truth.Truth.assertThat;

public class SyncBase extends BaseTest {

  @Override protected void setupVault() {
    vault = Vault.with(RuntimeEnvironment.application, DemoSpace.class);
  }

  protected void assertSyncUpdate() throws InterruptedException {
    assertRequestUpdate();
    assertUpdateAssets();
    assertUpdateEntries();
  }

  protected void assertSyncInitial() throws InterruptedException {
    assertRequestInitial();
    assertInitialAssets();
    assertInitialEntries();
    assertSingleLink();
  }

  protected void assertRequestUpdate() throws InterruptedException {
    server.takeRequest();
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    assertThat(request.getPath()).isEqualTo("/spaces/space/sync?sync_token=st1");
  }

  protected void assertRequestInitial() throws InterruptedException {
    server.takeRequest();
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    assertThat(request.getPath()).isEqualTo("/spaces/space/sync?initial=true");
  }

  protected void enqueueInitial() throws IOException {
    enqueue("demo/space.json");
    enqueue("demo/types.json");
    enqueue("demo/initial.json");
  }

  protected void enqueueUpdate() throws IOException {
    enqueue("demo/space.json");
    enqueue("demo/types.json");
    enqueue("demo/update.json");
  }

  protected void assertSingleLink() {
    Cat nyanCat = vault.fetch(Cat.class)
        .where(REMOTE_ID + " = ?", "nyancat")
        .first();

    assertThat(nyanCat).isNotNull();

    Cat happyCat = nyanCat.bestFriend();
    assertThat(happyCat).isNotNull();

    assertThat(nyanCat).isSameAs(happyCat.bestFriend());
  }

  protected void assertInitialAssets() {
    List<Asset> assets = vault.fetch(Asset.class)
        .order(CREATED_AT)
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

  protected void assertUpdateAssets() {
    List<Asset> assets = vault.fetch(Asset.class)
        .order(CREATED_AT)
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

  protected void assertInitialEntries() {
    List<Cat> cats = vault.fetch(Cat.class)
        .order(CREATED_AT)
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

  protected void assertUpdateEntries() {
    List<Cat> cats = vault.fetch(Cat.class)
        .order(CREATED_AT)
        .all();

    assertThat(cats).isNotNull();
    assertThat(cats).hasSize(3);
    assertThat(cats.get(0).name()).isEqualTo("Happier Cat");
    assertThat(cats.get(0).remoteId()).isEqualTo("happycat");
    assertThat(cats.get(1).remoteId()).isEqualTo("garfield");
    assertThat(cats.get(2).remoteId()).isEqualTo("supercat");
  }
}
