package com.contentful.sqlite;

import android.database.Cursor;
import java.util.List;

public interface Generator<T extends Resource> {
  T fromCursor(Cursor cursor);

  List<FieldMeta> getFields();
}
