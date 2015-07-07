package com.contentful.vaultintegration;

import com.contentful.vault.Vault;
import com.contentful.vaultintegration.lib.links.AssetsContainer;
import com.contentful.vaultintegration.lib.links.LinksSpace;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LinksTest extends BaseTest {
  @Override protected void setupVault() {
    vault = Vault.with(RuntimeEnvironment.application, LinksSpace.class);
  }

  @Test public void testLinks() throws Exception {
    // Initial
    enqueue("links/links_space.json");
    enqueue("links/links_initial.json");
    sync();

    AssetsContainer container = vault.fetch(AssetsContainer.class).first();
    assertNotNull(container);
    assertEquals(1, container.assets().size());
    assertEquals("4eHZNAfWq4UaiYIywMiSAy", container.assets().get(0).remoteId());

    // Update
    enqueue("links/links_space.json");
    enqueue("links/links_update.json");
    sync();

    container = vault.fetch(AssetsContainer.class).first();
    assertNotNull(container);
    assertEquals(2, container.assets().size());
    assertEquals("2lD1fm3UBiwYk0m6CgmiQQ", container.assets().get(0).remoteId());
    assertEquals("65ou5OAxawuQIkOAe2e42c", container.assets().get(1).remoteId());
  }
}
