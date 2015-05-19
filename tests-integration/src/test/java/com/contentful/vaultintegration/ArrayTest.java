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
import com.contentful.vaultintegration.lib.vault.ArraysResource;
import com.contentful.vaultintegration.lib.vault.VaultSpace;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml")
public class ArrayTest extends BaseTest {
  @Override protected void setupVault() {
    vault = Vault.with(RuntimeEnvironment.application, VaultSpace.class);
  }

  @Test public void testArray() throws Exception {
    enqueue("vault_space.json");
    enqueue("vault_initial.json");

    sync();
    ArraysResource resource = vault.fetch(ArraysResource.class).first();
    assertNotNull(resource);

    assertEquals(2, resource.assets.size());
    assertEquals("1yj8f2uFEgGEkuqeoGIQws", resource.assets.get(0).remoteId());
    assertEquals("2cMA1o04G42KGoQioOqqUA", resource.assets.get(1).remoteId());

    assertEquals(3, resource.symbols.size());
    assertEquals("a", resource.symbols.get(0));
    assertEquals("b", resource.symbols.get(1));
    assertEquals("c", resource.symbols.get(2));

    assertEquals(1, resource.blobs.size());
    assertNotNull(resource.blobs.get(0).object);
  }
}
