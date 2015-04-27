package com.contentful.sqlite;

import android.database.Cursor;
import java.util.List;

public abstract class ModelHelper<T extends Resource> {
  public abstract String getTableName();

  public abstract List<FieldMeta> getFields();

  public abstract List<String> getCreateStatements();

  public abstract T fromCursor(Cursor cursor);
}
