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
import com.contentful.vaultintegration.lib.links.AssetsContainer;
import com.contentful.vaultintegration.lib.links.LinksSpace;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import static com.contentful.vault.BaseFields.REMOTE_ID;
import static com.google.common.truth.Truth.assertThat;

public class LinksTest extends BaseTest {
  @Override protected void setupVault() {
    vault = Vault.with(RuntimeEnvironment.application, LinksSpace.class);
  }

  @Test public void testLinks() throws Exception {
    final String CONTAINER_ID = "34ljO1zKukgIYmcQ64IgOo";

    // Initial
    enqueueSync("links");
    sync();

    AssetsContainer container = vault.fetch(AssetsContainer.class)
        .where(REMOTE_ID + " = ?", CONTAINER_ID)
        .first();

    assertThat(container).isNotNull();
    assertThat(container.assets()).isNotNull();
    assertThat(container.assets()).hasSize(1);
    assertThat(container.assets().get(0).remoteId()).isEqualTo("4eHZNAfWq4UaiYIywMiSAy");

    // Update
    enqueueSync("links", true);
    sync();

    container = vault.fetch(AssetsContainer.class)
        .where(REMOTE_ID + " = ?", CONTAINER_ID)
        .first();

    assertThat(container).isNotNull();
    assertThat(container.assets()).isNotNull();
    assertThat(container.assets()).hasSize(2);
    List<String> ids = Arrays.asList("2lD1fm3UBiwYk0m6CgmiQQ", "65ou5OAxawuQIkOAe2e42c");
    assertThat(ids).contains(container.assets().get(0).remoteId());
    assertThat(ids).contains(container.assets().get(1).remoteId());
  }

  @Test public void testLinkArrayOrder() throws Exception {
    final String CONTAINER_ID = "ordered";

    // Initial
    enqueueSync("links");
    sync();

    AssetsContainer container = vault.fetch(AssetsContainer.class)
        .where(REMOTE_ID + " = ?", CONTAINER_ID)
        .first();

    assertThat(container).isNotNull();
    assertThat(container.assets()).hasSize(4);
    assertThat(container.assets().get(0).remoteId()).isEqualTo("4eHZNAfWq4UaiYIywMiSAy");
    assertThat(container.assets().get(1).remoteId()).isEqualTo("1g07qGA9BMkucwiUysi8qQ");
    assertThat(container.assets().get(2).remoteId()).isEqualTo("2lD1fm3UBiwYk0m6CgmiQQ");
    assertThat(container.assets().get(3).remoteId()).isEqualTo("65ou5OAxawuQIkOAe2e42c");
  }
}
