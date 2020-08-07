package com.contentful.vaultintegration;

import com.contentful.java.cda.CDAClient;
import com.contentful.vault.SyncConfig;
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

  @Test(expected = IllegalStateException.class)
  public void failsIfRequestingToSyncAndNothingIsSet() {
    SyncConfig
        .builder()
        .build();
  }

  @Test(expected = IllegalStateException.class)
  public void failsIfRequestingToSyncAndOnlyAccessTokenSet() {
    SyncConfig
        .builder()
        .setAccessToken("foo")
        .build();
  }

  @Test(expected = IllegalStateException.class)
  public void failsIfRequestingToSyncAndOnlySpaceIdSet() {
    SyncConfig
        .builder()
        .setSpaceId("foo")
        .build();
  }

  @Test(expected = IllegalStateException.class)
  public void failsIfRequestingToSyncAndAccessTokenAndClientSet() {
    SyncConfig
        .builder()
        .setAccessToken("foo")
        .setClient(null)
        .build();
  }

  @Test(expected = IllegalStateException.class)
  public void failsIfRequestingToSyncAndSpaceIdAndClientSet() {
    SyncConfig
        .builder()
        .setSpaceId("foo")
        .setClient(null)
        .build();
  }

  @Test
  public void createsACustomCDAClient() {
    final SyncConfig syncConfig = SyncConfig
        .builder()
        .setAccessToken("foo")
        .setSpaceId("bar")
        .build();

    final CDAClient client = syncConfig.client();
    assertThat(client).isNotNull();
  }

}
