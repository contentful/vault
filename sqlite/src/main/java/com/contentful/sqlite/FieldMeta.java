package com.contentful.sqlite;

public final class FieldMeta {
  final String id;
  final String name;
  final String sqliteType;
  final boolean link;

  public FieldMeta(String id, String name, String sqliteType, boolean link) {
    this.id = id;
    this.name = name;
    this.sqliteType = sqliteType;
    this.link = link;
  }
}
