package com.contentful.vault;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// TODO package-local
public final class SqliteHelper extends SQLiteOpenHelper {
  private final SpaceHelper spaceHelper;

  public SqliteHelper(Context context, SpaceHelper spaceHelper) {
    super(context, spaceHelper.getDatabaseName(), null, spaceHelper.getDatabaseVersion());
    this.spaceHelper = spaceHelper;
  }

  @Override public void onCreate(SQLiteDatabase db) {
    db.beginTransaction();
    try {
      for (String sql : SpaceHelper.DEFAULT_CREATE) {
        db.execSQL(sql);
      }
      for (ModelHelper<?> modelHelper : spaceHelper.getModels().values()) {
        for (String sql : modelHelper.getCreateStatements()) {
          db.execSQL(sql);
        }
      }
      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
    }
  }

  @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // TODO clear db
  }

  @SuppressWarnings("unchecked")
  public final <T extends Resource> T fromCursor(Class<T> clazz, Cursor cursor) {
    T resource = null;
    if (Asset.class.equals(clazz)) {
      resource = (T) assetFromCursor(cursor);
    } else {
      ModelHelper<?> modelHelper = spaceHelper.getModels().get(clazz);
      if (modelHelper != null) {
        resource = (T) modelHelper.fromCursor(cursor);
      }
    }
    if (resource != null) {
      resource.setRemoteId(cursor.getString(SpaceHelper.COLUMN_REMOTE_ID));
      resource.setCreatedAt(cursor.getString(SpaceHelper.COLUMN_CREATED_AT));
      resource.setUpdatedAt(cursor.getString(SpaceHelper.COLUMN_UPDATED_AT));
    }

    return resource;
  }

  private static Asset assetFromCursor(Cursor cursor) {
    Asset asset = new Asset();
    asset.setUrl(cursor.getString(SpaceHelper.COLUMN_ASSET_URL));
    asset.setMimeType(cursor.getString(SpaceHelper.COLUMN_ASSET_MIME_TYPE));
    return asset;
  }
}
