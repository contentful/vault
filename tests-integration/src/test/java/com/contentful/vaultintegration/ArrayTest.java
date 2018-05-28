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

import com.contentful.vault.Vault;
import com.contentful.vaultintegration.lib.vault.ArraysResource;
import com.contentful.vaultintegration.lib.vault.VaultSpace;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import static com.contentful.vault.BaseFields.REMOTE_ID;
import static com.google.common.truth.Truth.assertThat;

public class ArrayTest extends BaseTest {
  @Override protected void setupVault() {
    vault = Vault.with(RuntimeEnvironment.application, VaultSpace.class);
  }

  @Test public void testArray() throws Exception {
    enqueueSync("vault");
    sync();

    ArraysResource resource = vault.fetch(ArraysResource.class)
        .where(REMOTE_ID + " = ?", "u2L1goyi3eA4W0AKcqEou")
        .first();

    assertThat(resource).isNotNull();

    assertThat(resource.assets()).hasSize(2);
    List<String> ids = Arrays.asList("1yj8f2uFEgGEkuqeoGIQws", "2cMA1o04G42KGoQioOqqUA");
    assertThat(ids).contains(resource.assets().get(0).remoteId());
    assertThat(ids).contains(resource.assets().get(1).remoteId());

    assertThat(resource.symbols()).isNotNull();
    assertThat(resource.symbols()).containsExactly("a", "b", "c");

    assertThat(resource.blobs()).isNotNull();
    assertThat(resource.blobs()).hasSize(1);
    assertThat(resource.blobs().get(0)).isNotNull();
  }

  @Test public void testEmptyFields() throws Exception {
    enqueueSync("vault");
    sync();

    ArraysResource resource = vault.fetch(ArraysResource.class)
        .where(REMOTE_ID + " = ?", "FyFV7zVpMQUG6IIEekeI0")
        .first();

    assertThat(resource).isNotNull();
    assertThat(resource.assets()).isNotNull();
    assertThat(resource.assets()).isEmpty();

    assertThat(resource.symbols()).isNotNull();
    assertThat(resource.symbols()).isEmpty();

    assertThat(resource.blobs()).isNotNull();
    assertThat(resource.blobs()).isEmpty();
  }
}
