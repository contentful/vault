package com.contentful.sqlite;

import com.contentful.java.cda.CDAClient;
import com.contentful.sqlite.lib.ArraysResource;
import com.contentful.sqlite.lib.BlobResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import retrofit.RestAdapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml")
public class ArrayTest extends BaseTest {
  @Space(value = "y005y7p7nrqo", models = { ArraysResource.class, BlobResource.class } )
  static class TestSpace {
  }

  @Override protected void setupClient() {
    client = new CDAClient.Builder()
        .setLogLevel(RestAdapter.LogLevel.FULL)
        .setSpaceKey("y005y7p7nrqo") // TODO use recordings once all entries are formed
        .setAccessToken("4ebade7cb044ffa992861e10bbdf4ef486868bf08e5e708355aa0f85e5651a2a")
        .build();
  }

  @Test public void testArray() throws Exception {
    SyncRunnable.builder()
        .setContext(RuntimeEnvironment.application)
        .setSpace(TestSpace.class)
        .setSyncConfig(SyncConfig.builder().setClient(client).build())
        .build()
        .run();

    ArraysResource resource = Persistence.with(RuntimeEnvironment.application, TestSpace.class)
        .fetch(ArraysResource.class)
        .first();

    assertNotNull(resource);
    assertEquals(2, resource.assets.size());

    assertEquals(3, resource.symbols.size());
    assertEquals("a", resource.symbols.get(0));
    assertEquals("b", resource.symbols.get(1));
    assertEquals("c", resource.symbols.get(2));

    assertEquals(1, resource.blobs.size());
    assertNotNull(resource.blobs.get(0).object);
  }
}
