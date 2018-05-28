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

import com.contentful.java.cda.CDAClient;
import com.contentful.vault.Asset;
import com.contentful.vault.SyncConfig;

import org.junit.Test;

import java.util.List;

import okhttp3.mockwebserver.RecordedRequest;

import static com.contentful.vault.BaseFields.CREATED_AT;
import static com.google.common.truth.Truth.assertThat;

public class SyncTest extends SyncBase {

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
}
