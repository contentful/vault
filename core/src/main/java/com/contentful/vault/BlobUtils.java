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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

final class BlobUtils {
  // Java serialization stream magic header (0xACED)
  private static final int SERIAL_MAGIC_1 = (byte) 0xAC;
  private static final int SERIAL_MAGIC_2 = (byte) 0xED;
  // Generous upper bound; real blobs are small field maps/lists
  private static final int MAX_BLOB_SIZE = 1024 * 1024; // 1 MB

  // Allowlisted types that may appear in deserialized blobs (top-level or nested within Maps/Lists).
  private static final Set<String> ALLOWED_CLASS_NAMES = new HashSet<>(Arrays.asList(
      java.util.HashMap.class.getName(),
      java.util.LinkedHashMap.class.getName(),
      java.util.ArrayList.class.getName(),
      java.lang.String.class.getName(),
      java.lang.Integer.class.getName(),
      java.lang.Long.class.getName(),
      java.lang.Double.class.getName(),
      java.lang.Float.class.getName(),
      java.lang.Boolean.class.getName(),
      java.lang.Number.class.getName(),
      "[B" // byte[]
  ));

  private static void validateBlobInput(byte[] blob) throws IOException {
    if (blob == null || blob.length == 0) {
      throw new IllegalArgumentException("Blob must not be null or empty");
    }
    if (blob.length > MAX_BLOB_SIZE) {
      throw new IllegalArgumentException("Blob exceeds maximum allowed size");
    }
    if ((blob[0] & 0xFF) != (SERIAL_MAGIC_1 & 0xFF) || (blob[1] & 0xFF) != (SERIAL_MAGIC_2 & 0xFF)) {
      throw new InvalidClassException("Invalid serialized format: missing Java serialization header");
    }
  }

  @SuppressWarnings("unchecked")
  static <T extends Serializable> T fromBlob(final Class<T> clazz, byte[] blob)
      throws IOException, ClassNotFoundException {
    validateBlobInput(blob);
    T result = null;
    ObjectInputStream ois = null;
    try {
      ByteArrayInputStream bos = new ByteArrayInputStream(blob);
      ois = new ObjectInputStream(bos) {
        @Override protected Class<?> resolveClass(ObjectStreamClass desc)
            throws IOException, ClassNotFoundException {
          String name = desc.getName();
          if (!name.equals(clazz.getName()) && !ALLOWED_CLASS_NAMES.contains(name)) {
            throw new InvalidClassException("Unauthorized deserialization attempt", name);
          }
          return super.resolveClass(desc);
        }
      };
      result = (T) ois.readObject();
    } finally {
      if (ois != null) {
        try {
          ois.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return result;
  }

  static byte[] toBlob(Serializable object) throws IOException {
    ObjectOutputStream oos = null;
    ByteArrayOutputStream bos;
    byte[] result = null;
    try {
      bos = new ByteArrayOutputStream();
      oos = new ObjectOutputStream(bos);
      oos.writeObject(object);
      result = bos.toByteArray();
    } finally {
      if (oos != null) {
        try {
          oos.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return result;
  }
}
