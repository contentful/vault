package com.contentful.sqlite;

import android.database.Cursor;

final class ResourceFactory {
  private ResourceFactory() {
    throw new AssertionError();
  }

  @SuppressWarnings("unchecked")
  static <T extends Resource> T fromCursor(SpaceHelper spaceHelper, Class<T> clazz, Cursor cursor) {
    T result;
    if (clazz == Asset.class) {
      result = (T) assetFromCursor(cursor);
    } else {
      result = spaceHelper.fromCursor(clazz, cursor);
    }
    if (result != null) {
      setResourceFields(result, cursor);
    }
    return result;
  }

  private static Asset assetFromCursor(Cursor cursor) {
    String url = cursor.getString(SpaceHelper.COLUMN_ASSET_URL);
    String mimeType = cursor.getString(SpaceHelper.COLUMN_ASSET_MIME_TYPE);

    Asset asset = new Asset();
    asset.setUrl(url);
    asset.setMimeType(mimeType);
    return asset;
  }

  private static void setResourceFields(Resource resource, Cursor cursor) {
    resource.setRemoteId(cursor.getString(SpaceHelper.COLUMN_REMOTE_ID));
    resource.setCreatedAt(cursor.getString(SpaceHelper.COLUMN_CREATED_AT));
    resource.setUpdatedAt(cursor.getString(SpaceHelper.COLUMN_UPDATED_AT));
  }
}
