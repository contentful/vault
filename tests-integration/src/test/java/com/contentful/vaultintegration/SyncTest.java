/*
 * Copyright (C) 2018 Contentful GmbH
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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.contentful.java.cda.CDAClient;
import com.contentful.vault.Asset;
import com.contentful.vault.SyncConfig;
import com.contentful.vault.SyncException;
import com.contentful.vaultintegration.lib.demo.Cat;
import com.contentful.vaultintegration.lib.demo.DemoSpace$$SpaceHelper;

import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

import static com.contentful.vault.BaseFields.CREATED_AT;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

public class SyncTest extends SyncBase {

  private SQLiteDatabase openDatabase() {
    return RuntimeEnvironment.application.openOrCreateDatabase(
        new DemoSpace$$SpaceHelper().getDatabaseName(), 0, null);
  }

  @Test public void localeModeChangesReplaceAllLocaleTables() throws Exception {
    enqueueInitial();
    sync();
    assertSyncInitial();
    assertThat(vault.fetch(Cat.class).all("tlh")).hasSize(3);

    enqueueInitial();
    sync(SyncConfig.builder().setClient(client).setSingleLocale(true).build());
    assertRequestInitial();
    assertThat(vault.fetch(Cat.class).all("tlh")).isEmpty();
    assertInitialEntries();

    enqueueInitial();
    sync();
    assertSyncInitial();
    assertThat(vault.fetch(Cat.class).all("tlh")).hasSize(3);
  }

  @Test public void legacySyncSchemaResyncsWithoutLosingOfflineDataOnFailure() throws Exception {
    enqueueInitial();
    sync();
    assertSyncInitial();
    vault.release();
    try (SQLiteDatabase database = openDatabase()) {
      database.execSQL("DROP TABLE sync_info");
      database.execSQL("CREATE TABLE sync_info (token TEXT NOT NULL, "
          + "last_sync_ts TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");
      database.execSQL("INSERT INTO sync_info (token) VALUES ('st1')");
    }
    setupVault();

    enqueue("demo/locales.json");
    enqueue("demo/types.json");
    server.enqueue(new MockResponse().setResponseCode(500));
    try {
      sync(SyncConfig.builder().setClient(client).setSingleLocale(true).build());
      fail("Expected failed sync");
    } catch (SyncException expected) {
      assertInitialEntries();
      assertThat(vault.fetch(Cat.class).all("tlh")).hasSize(3);
    }
    assertRequestInitial();

    enqueueInitial();
    sync(SyncConfig.builder().setClient(client).setSingleLocale(true).build());
    assertRequestInitial();
    assertThat(vault.fetch(Cat.class).all("tlh")).isEmpty();
    try (SQLiteDatabase database = openDatabase();
         Cursor cursor = database.rawQuery("SELECT single_locale FROM sync_info", null)) {
      assertThat(cursor.moveToFirst()).isTrue();
      assertThat(cursor.getInt(0)).isEqualTo(1);
    }
  }

  @Test public void failedWriteRollsBackDataTokenAndLocaleMode() throws Exception {
    enqueueInitial();
    sync();
    assertSyncInitial();
    try (SQLiteDatabase database = openDatabase()) {
      database.execSQL("CREATE TRIGGER reject_asset BEFORE INSERT ON `assets$en-US` "
          + "BEGIN SELECT RAISE(FAIL, 'test write failure'); END");
    }
    enqueueInitial();
    try {
      sync(SyncConfig.builder().setClient(client).setSingleLocale(true).build());
      fail("Expected failed write");
    } catch (SyncException expected) {
      assertInitialAssets();
      assertInitialEntries();
      assertThat(vault.fetch(Cat.class).all("tlh")).hasSize(3);
    }
    assertRequestInitial();
    try (SQLiteDatabase database = openDatabase();
         Cursor cursor = database.rawQuery("SELECT token, single_locale FROM sync_info", null)) {
      assertThat(cursor.moveToFirst()).isTrue();
      assertThat(cursor.getString(0)).isEqualTo("st1");
      assertThat(cursor.getInt(1)).isEqualTo(0);
      database.execSQL("DROP TRIGGER reject_asset");
    }
    enqueueUpdate();
    sync();
    assertSyncUpdate();
  }

  @Test public void testAssetFallback() throws Exception {
    enqueue("assets/locales.json");
    enqueue("assets/types.json");
    enqueue("assets/initial.json");
    sync();

    server.takeRequest();
    server.takeRequest();
    RecordedRequest request = server.takeRequest();
    assertThat(request.getPath()).isEqualTo("/spaces/space/environments/master/sync?initial=true");
  }

  @Test public void testAssetsInDraft() throws Exception {
    enqueue("assets/locales.json");
    enqueue("assets/types.json");
    enqueue("assets/empty.json");
    sync();

    server.takeRequest(); // ignore locales request
    server.takeRequest(); // ignore content types request
    RecordedRequest request = server.takeRequest(); // analyse empty asset response
    assertThat(request.getPath()).isEqualTo("/spaces/space/environments/master/sync?initial=true");


    List<Asset> assets = vault.fetch(Asset.class).all();

    assertThat(assets).isNotNull();
    assertThat(assets).hasSize(1);

    Asset asset = assets.get(0);
    assertThat(asset).isNotNull();
    assertThat(asset.file()).isNull();
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

  @Test public void testAssetMetadata() throws Exception {
    enqueueInitial();
    sync();

    List<Asset> assets = vault.fetch(Asset.class)
            .order(CREATED_AT)
            .all();

    assertThat(assets).isNotNull();
    assertThat(assets).hasSize(4);

    assertThat(assets.get(0).title()).isEqualTo("Nyan Cat");
    assertThat(assets.get(0).description()).isNull();
    assertThat(assets.get(0).file()).hasSize(4);

    assertThat(assets.get(1).title()).isEqualTo("Jake");
    assertThat(assets.get(1).description()).isNull();
    assertThat(assets.get(1).file()).hasSize(4);

    assertThat(assets.get(2).title()).isEqualTo("Happy Cat");
    assertThat(assets.get(2).description()).isNull();
    assertThat(assets.get(2).file()).hasSize(4);

    assertThat(assets.get(3).title()).isEqualTo("Doge");
    assertThat(assets.get(3).description()).isEqualTo("nice picture");
    assertThat(assets.get(3).file()).hasSize(4);
  }

  @Test
  public void syncingOnEnvironmentsWorks() throws Throwable {
    enqueue("assets/locales.json");
    enqueue("assets/types.json");
    enqueue("assets/initial.json");

    final CDAClient localClient = CDAClient.builder()
            .setSpace("space")
            .setToken("token")
            .setEnvironment("environment")
            .setEndpoint(getServerUrl()) // only used for testing: leave blank if not white labeling
            .build();

    final SyncConfig config = new SyncConfig.Builder().setClient(localClient).build();

    sync(config);

    RecordedRequest request = server.takeRequest();
    assertThat(request.getPath()).startsWith("/spaces/space/environments/environment/locales");

    request = server.takeRequest();
    assertThat(request.getPath()).startsWith("/spaces/space/environments/environment/content_types");

    request = server.takeRequest();
    assertThat(request.getPath()).startsWith("/spaces/space/environments/environment/sync");
  }

  @Test public void testInvalidateForcesInitialSync() throws Exception {
    enqueueInitial();
    sync();
    assertSyncInitial();

    // Without invalidate this would be a delta sync with the stored token.
    enqueueInitial();
    sync(SyncConfig.builder().setClient(client).setInvalidate(true).build());
    assertSyncInitial();
  }

  @Test public void testFailedInvalidateKeepsExistingData() throws Exception {
    enqueueInitial();
    sync();
    assertSyncInitial();

    // The locales and content types load, then the sync request fails.
    enqueue("demo/locales.json");
    enqueue("demo/types.json");
    server.enqueue(new MockResponse().setResponseCode(500));
    try {
      sync(SyncConfig.builder().setClient(client).setInvalidate(true).build());
      fail("The sync was expected to fail with HTTP 500.");
    } catch (SyncException expected) {
      // expected
    }

    // Nothing may be wiped when the new data could not be fetched.
    assertInitialAssets();
    assertInitialEntries();
  }

  @Test public void testSyncWithLimit() throws Exception {
    // Initial sync: the limit is sent.
    enqueueInitial();
    sync(SyncConfig.builder().setClient(client).setLimit(1000).build());
    assertRequestInitialWithLimit(1000);
    assertInitialAssets();
    assertInitialEntries();
    assertSingleLink();

    // Delta sync: continues from the stored token, the limit is not sent.
    enqueueUpdate();
    sync(SyncConfig.builder().setClient(client).setLimit(1000).build());
    assertSyncUpdate();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSyncWithInvalidLimit() throws Exception {
    SyncConfig.builder()
            .setClient(client)
            .setLimit(0)  // Invalid limit
            .build();
  }

  @Test public void testSyncWithLimitAndInvalidate() throws Exception {
    enqueueInitial();
    sync();
    assertSyncInitial();

    // Invalidate discards the token, so this is an initial sync again, with the limit.
    enqueueInitial();
    sync(SyncConfig.builder()
            .setClient(client)
            .setLimit(1000)
            .setInvalidate(true)
            .build());
    assertRequestInitialWithLimit(1000);
    assertInitialAssets();
    assertInitialEntries();
  }

  private void assertRequestInitialWithLimit(int limit) throws InterruptedException {
    server.takeRequest(); // locales
    server.takeRequest(); // content types
    RecordedRequest request = server.takeRequest();
    assertThat(request.getPath())
        .isEqualTo("/spaces/space/environments/master/sync?initial=true&limit=" + limit);
  }
}
