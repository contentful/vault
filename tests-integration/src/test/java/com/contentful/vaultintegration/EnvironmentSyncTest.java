package com.contentful.vaultintegration;

import com.contentful.java.cda.CDAClient;
import com.contentful.vault.SyncCallback;
import com.contentful.vault.SyncConfig;
import com.contentful.vault.SyncException;
import com.contentful.vault.SyncResult;
import com.contentful.vault.Vault;
import com.contentful.vaultintegration.lib.demo.DemoSpace;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.logging.LogManager;

import okhttp3.mockwebserver.MockWebServer;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml", sdk = 23)
public class EnvironmentSyncTest {
  private MockWebServer server;
  private CDAClient client;
  private Vault vault;

  @Before public void setUp() throws Exception {
    LogManager.getLogManager().reset();
    server = new MockWebServer();
    server.start();

    final String url = "http://" + server.getHostName() + ":" + server.getPort();

    client = CDAClient.builder()
        .setSpace("space")
        .setToken("token")
        .setEnvironment("staging")
        .setEndpoint(url)
        .build();

    vault = Vault.with(RuntimeEnvironment.application, DemoSpace.class);
  }

  @After public void tearDown() throws Exception {
    server.shutdown();
    vault.releaseAll();
  }

  @Test(expected = SyncException.class)
  public void throwIfEnvironmentIsSet() throws Throwable {
    final CountDownLatch latch = new CountDownLatch(1);

    Executor executor = new Executor() {
      @Override public void execute(Runnable command) {
        command.run();
      }
    };

    final SyncResult[] result = {null};
    SyncCallback callback = new SyncCallback() {
      @Override public void onResult(SyncResult r) {
        result[0] = r;
        latch.countDown();
      }
    };

    final SyncConfig config = new SyncConfig.Builder().setClient(client).build();
    vault.requestSync(config, callback, executor);
    latch.await();

    final Throwable error = result[0].error();
    assertThat(error).isNotNull();
    final Throwable cause = error.getCause();
    assertThat(cause).isInstanceOf(IllegalStateException.class);
    assertThat(cause.getMessage()).isEqualTo("Cannot call 'sync' on non master environments!");

    throw error;
  }
}
