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

import com.contentful.vault.Vault;
import com.contentful.vaultintegration.lib.vault.BlobResource;
import com.contentful.vaultintegration.lib.vault.VaultSpace;
import java.util.Map;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;

public class BlobTest extends BaseTest {
  @Override protected void setupVault() {
    vault = Vault.with(RuntimeEnvironment.application, VaultSpace.class);
  }

  @Test public void testBlob() throws Exception {
    enqueue("vault/space.json");
    enqueue("vault/initial.json");

    sync();
    BlobResource blobResource = vault.fetch(BlobResource.class)
        .where("remote_id = ?", "6tOdhkd6Ewekq8M6MQe4GY")
        .first();

    assertThat(blobResource).isNotNull();
    assertThat(blobResource.object()).isNotNull();
    assertThat(blobResource.object()).containsEntry("fieldString", "hello");
    assertThat(blobResource.object()).containsEntry("fieldInteger", 31337.0);
    assertThat(blobResource.object()).containsEntry("fieldFloat", 3.1337);
    assertThat(blobResource.object()).containsEntry("fieldBoolean", true);

    Object fieldMap = blobResource.object().get("fieldMap");
    assertThat(fieldMap).isNotNull();
    assertThat(fieldMap).isInstanceOf(Map.class);
    assertEquals("value", ((Map) fieldMap).get("key"));
  }
}
