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
import com.contentful.vault.Vault;
import com.contentful.vaultintegration.lib.demo.DemoSpace;

import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

public class SyncPreviewTest extends SyncBase {

  @Override protected void setupVault() {
    vault = Vault.with(RuntimeEnvironment.application, DemoSpace.class);
  }

  @Override protected void setupClient() {
    client = CDAClient.builder()
        .setSpace("space")
        .setToken("token")
        .preview()
        .setEndpoint(getServerUrl())
        .build();
  }

  @Test public void testSyncInPreview() throws Exception {
    enqueue("demo/locales.json");
    enqueue("demo/types.json");
    enqueue("demo/initial.json");

    sync();

    assertSyncInitial();
  }

  @Test
  public void testSyncInPreviewNotInitialDoesInitial() throws Exception {
    enqueue("demo/locales.json");
    enqueue("demo/types.json");
    enqueue("demo/initial.json");

    sync();
    assertSyncInitial();

    enqueue("demo/locales.json");
    enqueue("demo/types.json");
    enqueue("demo/initial.json");

    sync();
    assertSyncInitial();
  }
}
