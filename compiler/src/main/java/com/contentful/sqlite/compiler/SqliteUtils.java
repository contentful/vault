package com.contentful.sqlite.compiler;

import java.util.Map;
import org.apache.commons.codec.binary.Base64;

final class SqliteUtils {
  private SqliteUtils() {
    throw new AssertionError();
  }

  static String typeForClass(String className) {
    if (String.class.getName().equals(className)) {
      return "STRING";
    } else if (Boolean.class.getName().equals(className)) {
      return "INT";
    } else if (Integer.class.getName().equals(className)) {
      return "INT";
    } else if (Double.class.getName().equals(className)) {
      return "DOUBLE";
    } else if (Map.class.getName().equals(className)) {
      return "BLOB";
    }
    return null;
  }

  static String hashForId(String id) {
    return Base64.encodeBase64String(id.getBytes()).replaceAll("=", "").toLowerCase();
  }
}
