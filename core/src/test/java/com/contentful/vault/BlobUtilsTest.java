/*
 * Copyright (C) 2018 Contentful GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.contentful.vault;

import java.io.ByteArrayOutputStream;
import java.io.InvalidClassException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BlobUtilsTest {

  // --- round-trip (allowed types) ---

  @Test public void testHashMapRoundTrip() throws Exception {
    HashMap<String, Object> map = new HashMap<>();
    map.put("key", "value");
    map.put("num", 42.0);

    byte[] blob = BlobUtils.toBlob(map);
    HashMap result = BlobUtils.fromBlob(HashMap.class, blob);

    assertNotNull(result);
    assertEquals("value", result.get("key"));
    assertEquals(42.0, result.get("num"));
  }

  @Test public void testArrayListRoundTrip() throws Exception {
    ArrayList<String> list = new ArrayList<>();
    list.add("a");
    list.add("b");

    byte[] blob = BlobUtils.toBlob(list);
    ArrayList result = BlobUtils.fromBlob(ArrayList.class, blob);

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("a", result.get(0));
  }

  // --- malicious class rejection ---

  @Test(expected = InvalidClassException.class)
  public void testMaliciousClassIsRejected() throws Exception {
    byte[] maliciousBlob = serializeRaw(new MaliciousClass());
    BlobUtils.fromBlob(HashMap.class, maliciousBlob);
  }

  // --- input validation ---

  @Test(expected = IllegalArgumentException.class)
  public void testNullBlobRejected() throws Exception {
    BlobUtils.fromBlob(HashMap.class, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyBlobRejected() throws Exception {
    BlobUtils.fromBlob(HashMap.class, new byte[0]);
  }

  @Test(expected = InvalidClassException.class)
  public void testInvalidHeaderRejected() throws Exception {
    BlobUtils.fromBlob(HashMap.class, new byte[]{0x00, 0x01, 0x02, 0x03});
  }

  @Test(expected = IllegalArgumentException.class)
  public void testOversizedBlobRejected() throws Exception {
    BlobUtils.fromBlob(HashMap.class, new byte[1024 * 1024 + 1]);
  }

  // --- helpers ---

  private static byte[] serializeRaw(Serializable obj) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(obj);
    }
    return baos.toByteArray();
  }

  static class MaliciousClass implements Serializable {
    private static final long serialVersionUID = 1L;
  }
}
