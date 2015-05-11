package com.contentful.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public abstract class SpaceHelper extends SQLiteOpenHelper {
  public SpaceHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
      int version) {
    super(context, name, factory, version);
  }

  public static final String[] RESOURCE_COLUMNS = new String[] {
      "`remote_id` STRING NOT NULL UNIQUE",
      "`created_at` STRING NOT NULL",
      "`updated_at` STRING"
  };

  static final String TABLE_ASSETS = "assets";
  static final String TABLE_ENTRY_TYPES = "entry_types";
  static final String TABLE_LINKS = "links";

  // Static resources column indexes
  static final int COLUMN_REMOTE_ID = 0;
  static final int COLUMN_CREATED_AT = 1;
  static final int COLUMN_UPDATED_AT = 2;

  // Static assets column indexes
  static final int COLUMN_ASSET_URL = RESOURCE_COLUMNS.length;
  static final int COLUMN_ASSET_MIME_TYPE = RESOURCE_COLUMNS.length + 1;

  static final String CREATE_ASSETS = "CREATE TABLE `"
      + TABLE_ASSETS
      + "` ("
      + StringUtils.join(RESOURCE_COLUMNS, ", ") + ","
      + "`url` STRING NOT NULL,"
      + "`mime_type` STRING NOT NULL"
      + ");";

  static final String CREATE_ENTRY_TYPES = "CREATE TABLE `"
      + TABLE_ENTRY_TYPES
      + "` ("
      + "`remote_id` STRING NOT NULL,"
      + "`type_id` STRING NOT NULL,"
      + "UNIQUE(`remote_id`)"
      + ");";

  static final String CREATE_LINKS = "CREATE TABLE `"
      + TABLE_LINKS
      + "` ("
      + "`parent` STRING NOT NULL,"
      + "`child` STRING NOT NULL,"
      + "`field` STRING NOT NULL,"
      + "`child_content_type` STRING,"
      + "UNIQUE (`parent`, `child`, `field`)"
      + ");";

  public static final List<String> DEFAULT_CREATE =
      Arrays.asList(CREATE_ASSETS, CREATE_ENTRY_TYPES, CREATE_LINKS);

  public abstract Map<Class<?>, ModelHelper<?>> getModels();

  public abstract Map<String, Class<?>> getTypes();

  @Override public void onCreate(SQLiteDatabase db) {
    db.beginTransaction();
    try {
      for (String sql : DEFAULT_CREATE) {
        db.execSQL(sql);
      }
      for (ModelHelper<?> modelHelper : getModels().values()) {
        for (String sql : modelHelper.getCreateStatements()) {
          db.execSQL(sql);
        }
      }
      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
    }
  }

  @Override public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
  }

  @SuppressWarnings("unchecked")
  public final <T extends Resource> T fromCursor(Class<T> clazz, Cursor cursor) {
    T resource = null;
    if (Asset.class.equals(clazz)) {
      resource = (T) assetFromCursor(cursor);
    } else {
      ModelHelper<?> modelHelper = getModels().get(clazz);
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
