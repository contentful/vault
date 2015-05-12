package com.contentful.vault.integration;

import com.contentful.java.cda.CDAClient;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import retrofit.RestAdapter;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml")
public abstract class BaseTest {
  MockWebServer server;
  CDAClient client;

  @Before public void setUp() throws Exception {
    server = new MockWebServer();
    server.start();
    setupClient();
  }

  protected void setupClient() {
    client = new CDAClient.Builder()
        .setLogLevel(RestAdapter.LogLevel.FULL)
        .setSpaceKey("space")
        .setAccessToken("token")
        .setEndpoint(getServerUrl())
        .noSSL()
        .build();
  }

  @After public void tearDown() throws Exception {
    server.shutdown();
  }

  protected String getServerUrl() {
    URL url = server.getUrl("/");
    return url.getHost() + ":" + url.getPort();
  }

  protected void enqueue(String fileName) throws IOException {
    URL resource = getClass().getClassLoader().getResource(fileName);
    if (resource == null) {
      throw new IllegalArgumentException("File not found");
    }
    server.enqueue(new MockResponse().setResponseCode(200).setBody(
        FileUtils.readFileToString(new File(resource.getFile()))));
  }
}
