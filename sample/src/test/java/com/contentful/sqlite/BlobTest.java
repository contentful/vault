package com.contentful.sqlite;

import com.contentful.java.cda.CDAClient;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import retrofit.RestAdapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml")
public class BlobTest {
  @Space(value = "y005y7p7nrqo", models = Blob.class)
  static class TestSpace {
  }

  @ContentType("1HRG7uai2g8YMswwqoAaC8")
  static class Blob extends Resource {
    @Field Map object;
  }

  @Test public void testBlob() throws Exception {
    SyncRunnable.builder()
        .setContext(RuntimeEnvironment.application)
        .setSpace(TestSpace.class)
        .setClient(new CDAClient.Builder()
            .setSpaceKey("y005y7p7nrqo") // TODO use recordings once all entries are formed
            .setAccessToken("4ebade7cb044ffa992861e10bbdf4ef486868bf08e5e708355aa0f85e5651a2a")
            .setLogLevel(RestAdapter.LogLevel.FULL)
            //.noSSL()
            .build())
        .build()
        .run();

    Blob blobResource =
        Persistence.with(RuntimeEnvironment.application, TestSpace.class).fetch(Blob.class).first();

    assertNotNull(blobResource);
    assertEquals("hello", blobResource.object.get("fieldString"));
    assertEquals(31337, ((Double) blobResource.object.get("fieldInteger")).intValue());
    assertEquals(3.1337, blobResource.object.get("fieldFloat"));
    assertTrue((Boolean) blobResource.object.get("fieldBoolean"));

    Object obj = blobResource.object.get("fieldMap");
    assertNotNull(obj);
    assertTrue(obj instanceof Map);
    assertEquals("value", ((Map) obj).get("key"));
  }
}
