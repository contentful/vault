package com.contentful.sqlite;

import android.database.Cursor;
import java.util.List;

public interface ModelHelper<T extends Resource> {
  String getTableName();

  List<FieldMeta> getFields();

  List<String> getCreateStatements();

  T fromCursor(Cursor cursor);
}
