package com.contentful.sqlite;

import java.util.Map;

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
}
