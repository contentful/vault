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
