/*
 * Copyright (C) 2015 Contentful GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.contentful.vault;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

final class SqliteHelper extends SQLiteOpenHelper {
  private final SpaceHelper spaceHelper;

  public SqliteHelper(Context context, SpaceHelper spaceHelper) {
    super(context, spaceHelper.getDatabaseName(), null, spaceHelper.getDatabaseVersion());
    this.spaceHelper = spaceHelper;
  }

  @Override public SQLiteDatabase getReadableDatabase() {
    synchronized (this) {
      copyStaticDatabase();
    }
    return super.getReadableDatabase();
  }

  @Override public SQLiteDatabase getWritableDatabase() {
    synchronized (this) {
      copyStaticDatabase();
    }
    return super.getWritableDatabase();
  }

  @Override public void onCreate(SQLiteDatabase db) {
    db.beginTransaction();
    try {
      execCreate(spaceHelper, db);
      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
    }
  }

  @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    deleteTables(db);
    onCreate(db);
  }

  static void execCreate(SpaceHelper helper, SQLiteDatabase db) {
    for (String sql : SpaceHelper.DEFAULT_CREATE) {
      db.execSQL(sql);
    }
    for (ModelHelper<?> modelHelper : helper.getModels().values()) {
      for (String sql : modelHelper.getCreateStatements()) {
        db.execSQL(sql);
      }
    }
  }

  static void clearRecords(SpaceHelper helper, SQLiteDatabase db) {
    db.beginTransaction();
    try {
      for (String name : SpaceHelper.DEFAULT_TABLES) {
        db.delete(name, null, null);
      }
      for (ModelHelper<?> modelHelper : helper.getModels().values()) {
        db.delete(modelHelper.getTableName(), null, null);
      }
      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
    }
  }

  static void deleteTables(SQLiteDatabase db) {
    String[] columns = new String[] { "name" };
    String selection = "type = ? AND name != ?";
    String[] args = new String[] { "table", "android_metadata" };
    Cursor cursor = db.query("sqlite_master", columns, selection, args, null, null, null);
    List<String> tables = null;
    try {
      if (cursor.moveToFirst()) {
        tables = new ArrayList<String>();
        do {
          tables.add(cursor.getString(0));
        } while (cursor.moveToNext());
      }
    } finally {
      cursor.close();
    }
    if (tables != null) {
      db.beginTransaction();
      try {
        for (String table : tables) {
          db.execSQL("DROP TABLE " + table);
        }
        db.setTransactionSuccessful();
      } finally {
        db.endTransaction();
      }
    }
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
    String url = cursor.getString(SpaceHelper.COLUMN_ASSET_URL);
    String mimeType = cursor.getString(SpaceHelper.COLUMN_ASSET_MIME_TYPE);

    return Asset.builder()
        .setUrl(url)
        .setMimeType(mimeType)
        .build();
  }

  public SpaceHelper getSpaceHelper() {
    return spaceHelper;
  }

  private void copyStaticDatabase() {

  }
}
