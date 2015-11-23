package com.contentful.vaultintegration;

import com.contentful.vault.Vault;
import com.contentful.vaultintegration.lib.localizedlinks.Container;
import com.contentful.vaultintegration.lib.localizedlinks.LocalizedLinksSpace;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import static com.google.common.truth.Truth.assertThat;

public final class LocalizedLinksTest extends BaseTest {
  @Override protected void setupVault() {
    vault = Vault.with(RuntimeEnvironment.application, LocalizedLinksSpace.class);
  }

  @Test public void localizedLink() throws Exception {
    enqueueSync("localizedlinks");
    sync();

    final Container english = vault.fetch(Container.class).first();
    final Container hebrew = vault.fetch(Container.class).first("he-IL");

    assertThat(english.one().title()).isEqualTo("hello");
    assertThat(hebrew.one().title()).isEqualTo("shalom");
  }

  @Test public void testNonLocalizedArray() throws Exception {
    enqueueSync("localizedlinks");
    sync();

    final Container english = vault.fetch(Container.class).first();
    final Container hebrew = vault.fetch(Container.class).first("he-IL");

    assertThat(english.assets()).hasSize(2);
    assertThat(hebrew.assets()).hasSize(2);
  }
}
