package com.contentful.sqlite;

public final class FieldMeta {
  final String id;
  final String name;
  final String sqliteType;
  final boolean link;
  final String className;

  public FieldMeta(String id, String name, String sqliteType, boolean link, String className) {
    this.id = id;
    this.name = name;
    this.sqliteType = sqliteType;
    this.link = link;
    this.className = className;
  }
}
