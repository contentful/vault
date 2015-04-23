package com.contentful.sqlite;

import android.database.Cursor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

final class ResourceFactory {
  private ResourceFactory() {
    throw new AssertionError();
  }

  @SuppressWarnings("unchecked")
  static <T extends Resource> T fromCursor(Cursor cursor, Class<T> clazz, List<FieldMeta> fields) {
    if (clazz == Asset.class) {
      return (T) assetFromCursor(cursor);
    } else if (fields == null) {
      throw new IllegalArgumentException("Cannot create resource \"" + clazz.getName() +
          "\" with empty fields list");
    }
    try {
      return entryFromCursor(cursor, clazz, fields);
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  private static <T extends Resource> T entryFromCursor(Cursor cursor,
      Class<T> clazz, List<FieldMeta> fields)
      throws IllegalAccessException, InstantiationException, NoSuchFieldException,
      NoSuchMethodException, InvocationTargetException {
    T result = clazz.newInstance();
    setResourceFields(result, cursor);
    for (FieldMeta field : fields) {
      // TODO perform via generated code
      Field f = result.getClass().getDeclaredField(field.name);
      if (field.isLink()) {
        continue;
      }

      int columnIndex = fields.indexOf(field) + PersistenceHelper.RESOURCE_COLUMNS.length;
      Object value = valueForField(cursor, field, columnIndex);
      if (value != null) {
        f.setAccessible(true);
        f.set(result, value);
      }
    }
    return result;
  }

  private static Object valueForField(Cursor cursor, FieldMeta field, int columnIndex) {
    if (String.class.getName().equals(field.className)) {
      return cursor.getString(columnIndex);
    } else if (Integer.class.getName().equals(field.className) ||
        Double.class.getName().equals(field.className)) {
      return cursor.getInt(columnIndex);
    } else if (Map.class.getName().equals(field.className)) {
      return null; // TODO read blob
    }
    return null;
  }

  private static Asset assetFromCursor(Cursor cursor) {
    String url = cursor.getString(PersistenceHelper.COLUMN_ASSET_URL);
    String mimeType = cursor.getString(PersistenceHelper.COLUMN_ASSET_MIME_TYPE);

    Asset asset = new Asset();
    setResourceFields(asset, cursor);
    asset.setUrl(url);
    asset.setMimeType(mimeType);
    return asset;
  }

  private static void setResourceFields(Resource resource, Cursor cursor) {
    resource.setRemoteId(cursor.getString(PersistenceHelper.COLUMN_REMOTE_ID));
    resource.setCreatedAt(cursor.getString(PersistenceHelper.COLUMN_CREATED_AT));
    resource.setUpdatedAt(cursor.getString(PersistenceHelper.COLUMN_UPDATED_AT));
  }
}
