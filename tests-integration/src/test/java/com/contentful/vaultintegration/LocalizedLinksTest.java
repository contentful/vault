package com.contentful.vaultintegration;

import com.contentful.vault.Vault;
import com.contentful.vaultintegration.lib.localizedlinks.Container;
import com.contentful.vaultintegration.lib.localizedlinks.Container$Fields;
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

    final String whereClause = Container$Fields.REMOTE_ID + " = ?";
    final String whereArgs = "2gwYYOMj8YMeAKsKYkY8qE";

    final Container english =
        vault.fetch(Container.class).where(whereClause, whereArgs).first();

    final Container hebrew =
        vault.fetch(Container.class).where(whereClause, whereArgs).first("he-IL");

    assertThat(english.image().title()).isEqualTo("hello");
    assertThat(hebrew.image().title()).isEqualTo("shalom");
  }
}
