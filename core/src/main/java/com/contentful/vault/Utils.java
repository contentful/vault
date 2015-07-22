package com.contentful.vault;

final class Utils {
  private Utils() {
    throw new AssertionError();
  }

  static String escape(String name) {
    return String.format("`%s`", name);
  }

  static String declareField(String name, String type, boolean nullable, String suffix) {
    StringBuilder builder = new StringBuilder();
    builder.append(escape(name))
        .append(" ")
        .append(type);
    if (!nullable) {
      builder.append(" ")
          .append("NOT NULL");
    }
    if (suffix != null) {
      builder.append(suffix);
    }
    return builder.toString();
  }
}
