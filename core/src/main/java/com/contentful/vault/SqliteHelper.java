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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.io.FileUtils;

import static com.contentful.vault.BaseFields.CREATED_AT;
import static com.contentful.vault.BaseFields.REMOTE_ID;
import static com.contentful.vault.BaseFields.UPDATED_AT;
import static com.contentful.vault.Sql.CREATE_ENTRY_TYPES;
import static com.contentful.vault.Sql.CREATE_SYNC_INFO;
import static com.contentful.vault.Sql.TABLE_ASSETS;
import static com.contentful.vault.Sql.TABLE_ENTRY_TYPES;
import static com.contentful.vault.Sql.TABLE_LINKS;
import static com.contentful.vault.Sql.TABLE_SYNC_INFO;
import static com.contentful.vault.Sql.assetColumnIndex;
import static com.contentful.vault.Sql.createAssets;
import static com.contentful.vault.Sql.createLinks;
import static com.contentful.vault.Sql.escape;
import static com.contentful.vault.Sql.localizeName;
import static com.contentful.vault.Sql.resourceColumnIndex;

final class SqliteHelper extends SQLiteOpenHelper {
  private final Context context;

  private final SpaceHelper spaceHelper;

  public SqliteHelper(Context context, SpaceHelper spaceHelper) {
    super(context, spaceHelper.getDatabaseName(), null, spaceHelper.getDatabaseVersion());
    this.context = context.getApplicationContext();
    this.spaceHelper = spaceHelper;
    copyDatabase();
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
    if (spaceHelper.getCopyPath() == null) {
      deleteTables(db);
      onCreate(db);
    }
  }

  static void execCreate(SpaceHelper helper, SQLiteDatabase db) {
    db.execSQL(CREATE_ENTRY_TYPES);
    db.execSQL(CREATE_SYNC_INFO);
    for (String code : helper.getLocales()) {
      db.execSQL(createAssets(code));
      db.execSQL(createLinks(code));
    }
    for (ModelHelper<?> modelHelper : helper.getModels().values()) {
      for (String sql : modelHelper.getCreateStatements(helper)) {
        db.execSQL(sql);
      }
    }
  }

  static void clearRecords(SpaceHelper helper, SQLiteDatabase db) {
    db.beginTransaction();
    try {
      db.delete(escape(TABLE_ENTRY_TYPES), null, null);
      db.delete(escape(TABLE_SYNC_INFO), null, null);
      for (String code : helper.getLocales()) {
        db.delete(escape(localizeName(TABLE_ASSETS, code)), null, null);
        db.delete(escape(localizeName(TABLE_LINKS, code)), null, null);

        for (ModelHelper<?> modelHelper : helper.getModels().values()) {
          db.delete(escape(localizeName(modelHelper.getTableName(), code)), null, null);
        }
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
      resource.setRemoteId(cursor.getString(resourceColumnIndex(REMOTE_ID)));
      resource.setCreatedAt(cursor.getString(resourceColumnIndex(CREATED_AT)));
      resource.setUpdatedAt(cursor.getString(resourceColumnIndex(UPDATED_AT)));
    }
    return resource;
  }

  @SuppressWarnings("unchecked")
  private static Asset assetFromCursor(Cursor cursor) {
    String remoteId = cursor.getString(resourceColumnIndex(REMOTE_ID));
    String url = cursor.getString(assetColumnIndex(Asset.Fields.URL));
    String mimeType = cursor.getString(assetColumnIndex(Asset.Fields.MIME_TYPE));
    String title = cursor.getString(assetColumnIndex(Asset.Fields.TITLE));
    String description = cursor.getString(assetColumnIndex(Asset.Fields.DESCRIPTION));
    HashMap<String, Object> fileMap = null;
    byte[] fileBlob = cursor.getBlob(assetColumnIndex(Asset.Fields.FILE));

    if (fileBlob != null && fileBlob.length > 0) {
      try {
        fileMap = BlobUtils.fromBlob(HashMap.class, fileBlob);
      } catch (IOException e) {
        throw new RuntimeException("Failed while deserializing file map for asset '" +
            remoteId + "'.");
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }

    return Asset.builder()
        .setUrl(url)
        .setMimeType(mimeType)
        .setTitle(title)
        .setDescription(description)
        .setFile(fileMap)
        .build();
  }

  public SpaceHelper getSpaceHelper() {
    return spaceHelper;
  }

  private void copyDatabase() {
    String copyPath = spaceHelper.getCopyPath();
    if (copyPath == null) {
      return;
    }

    final File dbPath = context.getDatabasePath(spaceHelper.getDatabaseName());
    if (!isPendingCopy(dbPath)) {
      return;
    }

    if (dbPath.exists()) {
      dbPath.delete();
    } else {
      dbPath.getParentFile().mkdirs();
    }

    try {
      FileUtils.copyInputStreamToFile(context.getAssets().open(copyPath), dbPath);
    } catch (IOException e) {
      throw new RuntimeException("Failure while attempting to copy '" + copyPath + "'.", e);
    }
  }

  private boolean isPendingCopy(File dbPath) {
    boolean result = false;
    if (dbPath.exists()) {
      SQLiteDatabase db =
          context.openOrCreateDatabase(spaceHelper.getDatabaseName(), Context.MODE_PRIVATE, null);
      try {
        if (spaceHelper.getDatabaseVersion() > db.getVersion()) {
          result = true;
        }
      } finally {
        db.close();
      }
    } else {
      result = true;
    }
    return result;
  }
}
