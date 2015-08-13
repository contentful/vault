package com.contentful.vaultintegration;

import com.contentful.vault.Vault;
import com.contentful.vaultintegration.lib.demo.DemoSpace;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

public class VaultTest extends BaseTest {
  @Override protected void setupVault() {
    vault = Vault.with(RuntimeEnvironment.application, DemoSpace.class);
  }

  @Test public void failsInvalidSpaceClass() throws Exception {
    try {
      Vault.with(RuntimeEnvironment.application, Object.class);
      fail();
    } catch (RuntimeException e) {
      assertThat(e.getMessage()).isEqualTo(
          "Cannot find generated class for space: java.lang.Object");
    }
  }
}
