package com.contentful.vault.integration;

import com.contentful.java.cda.CDAClient;
import com.contentful.vault.Space;
import com.contentful.vault.integration.lib.BlobResource;
import org.junit.Test;
import retrofit.RestAdapter;

public class BlobTest extends BaseTest {
  @Space(value = "y005y7p7nrqo", models = BlobResource.class)
  static class TestSpace {
  }

  @Override protected void setupClient() {
    client = new CDAClient.Builder()
        .setLogLevel(RestAdapter.LogLevel.FULL)
        .setSpaceKey("y005y7p7nrqo") // TODO use recordings once all entries are formed
        .setAccessToken("4ebade7cb044ffa992861e10bbdf4ef486868bf08e5e708355aa0f85e5651a2a")
        .build();
  }

  @Test public void testBlob() throws Exception {
    /*
    TODO
    SyncRunnable.builder()
        .setContext(RuntimeEnvironment.application)
        .setSpace(TestSpace.class)
        .setSyncConfig(SyncConfig.builder().setClient(client).build())
        .build()
        .run();

    BlobResource blobResource = Persistence.with(RuntimeEnvironment.application, TestSpace.class)
        .fetch(BlobResource.class)
        .first();

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
    */
  }
}
