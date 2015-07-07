package com.contentful.vaultintegration;

import com.contentful.vault.Vault;
import com.contentful.vaultintegration.lib.links.AssetsContainer;
import com.contentful.vaultintegration.lib.links.LinksSpace;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import static com.google.common.truth.Truth.assertThat;

public class LinksTest extends BaseTest {
  @Override protected void setupVault() {
    vault = Vault.with(RuntimeEnvironment.application, LinksSpace.class);
  }

  @Test public void testLinks() throws Exception {
    final String CONTAINER_ID = "34ljO1zKukgIYmcQ64IgOo";

    // Initial
    enqueue("links/space.json");
    enqueue("links/initial.json");
    sync();

    AssetsContainer container = vault.fetch(AssetsContainer.class)
        .where("remote_id = ?", CONTAINER_ID)
        .first();

    assertThat(container).isNotNull();
    assertThat(container.assets()).isNotNull();
    assertThat(container.assets()).hasSize(1);
    assertThat(container.assets().get(0).remoteId()).isEqualTo("4eHZNAfWq4UaiYIywMiSAy");

    // Update
    enqueue("links/space.json");
    enqueue("links/update.json");
    sync();

    container = vault.fetch(AssetsContainer.class)
        .where("remote_id = ?", CONTAINER_ID)
        .first();

    assertThat(container).isNotNull();
    assertThat(container.assets()).isNotNull();
    assertThat(container.assets()).hasSize(2);
    List<String> ids = Arrays.asList("2lD1fm3UBiwYk0m6CgmiQQ", "65ou5OAxawuQIkOAe2e42c");
    assertThat(ids).contains(container.assets().get(0).remoteId());
    assertThat(ids).contains(container.assets().get(1).remoteId());
  }
}
