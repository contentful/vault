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

    assertEquals(3, resource.symbols.size());
    assertEquals("a", resource.symbols.get(0));
    assertEquals("b", resource.symbols.get(1));
    assertEquals("c", resource.symbols.get(2));

    assertEquals(1, resource.blobs.size());
    assertNotNull(resource.blobs.get(0).object);
  }
}
