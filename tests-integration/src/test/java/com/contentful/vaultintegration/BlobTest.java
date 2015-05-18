package com.contentful.vaultintegration;

import com.contentful.vault.Vault;
import com.contentful.vaultintegration.lib.vault.BlobResource;
import com.contentful.vaultintegration.lib.vault.VaultSpace;
import java.util.Map;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BlobTest extends BaseTest {
  @Override protected void setupVault() {
    vault = Vault.with(RuntimeEnvironment.application, VaultSpace.class);
  }

  @Test public void testBlob() throws Exception {
    enqueue("vault_space.json");
    enqueue("vault_initial.json");

    sync();
    BlobResource blobResource = vault.fetch(BlobResource.class).first();
    assertNotNull(blobResource);

    Map map = blobResource.object;
    assertNotNull(map);

    assertEquals("hello", blobResource.object.get("fieldString"));
    assertEquals(31337, ((Double) blobResource.object.get("fieldInteger")).intValue());
    assertEquals(3.1337, blobResource.object.get("fieldFloat"));
    assertTrue((Boolean) blobResource.object.get("fieldBoolean"));

    Object obj = blobResource.object.get("fieldMap");
    assertNotNull(obj);
    assertTrue(obj instanceof Map);
    assertEquals("value", ((Map) obj).get("key"));
  }
}
