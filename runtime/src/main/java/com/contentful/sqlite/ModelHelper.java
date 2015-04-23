package com.contentful.sqlite;

import java.util.List;

public interface ModelHelper<T extends Resource> {
  /*
  T fromCursor(Cursor cursor);

  String getTableName();

  String getContentType();

  */
  String getTableName();

  List<FieldMeta> getFields();
}
