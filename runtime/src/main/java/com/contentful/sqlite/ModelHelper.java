package com.contentful.sqlite;

import android.database.Cursor;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public abstract class ModelHelper<T extends Resource> {
  public abstract String getTableName();

  public abstract List<FieldMeta> getFields();

  public abstract List<String> getCreateStatements();

  public abstract T fromCursor(Cursor cursor);

  protected abstract boolean setField(T resource, String name, Object value);

  protected final <E extends Serializable> E fieldFromBlob(Class<E> clazz, Cursor cursor,
      int columnIndex) {
    byte[] blob = cursor.getBlob(columnIndex);
    if (blob == null || blob.length == 0) {
      return null;
    }
    E result = null;
    Exception exception = null;
    try {
      result = BlobUtils.fromBlob(clazz, blob);
    } catch (IOException e) {
      exception = e;
    } catch (ClassNotFoundException e) {
      exception = e;
    }
    if (exception != null) {
      throw new RuntimeException(String.format("Failed creating BLOB from column %d of %s.",
          columnIndex, getTableName()), exception);
    }
    return result;
  }
}
