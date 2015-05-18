/*
 * Copyright (C) 2015 Contentful GmbH
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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

final class BlobUtils {
  @SuppressWarnings("unchecked")
  static <T extends Serializable> T fromBlob(Class<T> clazz, byte[] blob)
      throws IOException, ClassNotFoundException {
    T result = null;
    ObjectInputStream ois = null;
    try {
      ByteArrayInputStream bos = new ByteArrayInputStream(blob);
      ois = new ObjectInputStream(bos);
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
