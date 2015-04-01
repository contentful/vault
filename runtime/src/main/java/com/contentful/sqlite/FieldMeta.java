package com.contentful.sqlite;

public final class FieldMeta {
  final String id;
  final String name;
  final String sqliteType;
  final String linkType;
  final String className;

  public FieldMeta(String id, String name, String sqliteType, String linkType, String className) {
    this.id = id;
    this.name = name;
    this.sqliteType = sqliteType;
    this.linkType = linkType;
    this.className = className;
  }

  public boolean isLink() {
    return linkType != null;
  }
}
