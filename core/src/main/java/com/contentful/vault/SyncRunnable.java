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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import com.contentful.java.cda.CDAAsset;
import com.contentful.java.cda.CDAEntry;
import com.contentful.java.cda.CDAResource;
import com.contentful.java.cda.CDAType;
import com.contentful.java.cda.LocalizedResource;
import com.contentful.java.cda.SynchronizedSpace;
import com.squareup.okhttp.HttpUrl;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;
import static com.contentful.java.cda.CDAType.ASSET;
import static com.contentful.java.cda.CDAType.ENTRY;
import static com.contentful.vault.BaseFields.CREATED_AT;
import static com.contentful.vault.BaseFields.REMOTE_ID;
import static com.contentful.vault.BaseFields.UPDATED_AT;
import static com.contentful.vault.Sql.TABLE_ASSETS;
import static com.contentful.vault.Sql.TABLE_ENTRY_TYPES;
import static com.contentful.vault.Sql.TABLE_LINKS;
import static com.contentful.vault.Sql.TABLE_SYNC_INFO;
import static com.contentful.vault.Sql.escape;
import static com.contentful.vault.Sql.localizeName;

public final class SyncRunnable implements Runnable {
  private final Context context;

  private final SyncConfig config;

  private SqliteHelper sqliteHelper;

  private SpaceHelper spaceHelper;

  private SQLiteDatabase db;

  private String tag;

  private SyncRunnable(Builder builder) {
    this.context = builder.context;
    this.config = builder.config;
    this.tag = builder.tag;
    this.sqliteHelper = builder.sqliteHelper;
    this.spaceHelper = sqliteHelper.getSpaceHelper();
  }

  static Builder builder() {
    return new Builder();
  }

  @Override public void run() {
    SyncException error = null;
    db = sqliteHelper.getWritableDatabase();
    try {
      String token = null;
      if (config.shouldInvalidate()) {
        SqliteHelper.clearRecords(spaceHelper, db);
      } else {
        token = fetchSyncToken();
      }

      SynchronizedSpace syncedSpace;
      if (token == null) {
        syncedSpace = config.client().sync().fetch();
      } else {
        syncedSpace = config.client().sync(token).fetch();
      }

      db.beginTransaction();
      try {
        processDeleted(syncedSpace);
        processResources(syncedSpace);

        saveSyncInfo(HttpUrl.parse(syncedSpace.nextSyncUrl()).queryParameter("sync_token"));
        db.setTransactionSuccessful();
      } finally {
        db.endTransaction();
      }
    } catch (Exception e) {
      error = new SyncException(e);
    } finally {
      // Notify via broadcast
      context.sendBroadcast(new Intent(Vault.ACTION_SYNC_COMPLETE)
          .putExtra(Vault.EXTRA_SUCCESS, error == null));

      SyncResult syncResult = new SyncResult(spaceHelper.getSpaceId(), error);

      // RxJava Subject
      Vault.SYNC_SUBJECT.onNext(syncResult);

      // Callback
      Vault.executeCallback(tag, syncResult);
    }
  }

  private void processResources(SynchronizedSpace syncedSpace) {
    for (CDAAsset asset : syncedSpace.assets().values()) {
      processResource(asset);
    }
    for (CDAEntry entry : syncedSpace.entries().values()) {
      processResource(entry);
    }
  }

  private void processDeleted(SynchronizedSpace syncedSpace) {
    for (String id : syncedSpace.deletedAssets()) {
      deleteAsset(id);
    }
    for (String id : syncedSpace.deletedEntries()) {
      deleteEntry(id);
    }
  }

  private String fetchSyncToken() {
    String token = null;
    Cursor cursor = db.rawQuery("SELECT `token` FROM sync_info", null);
    try {
      if (cursor.moveToFirst()) {
        token = cursor.getString(0);
      }
    } finally {
      cursor.close();
    }
    return token;
  }

  private void saveSyncInfo(String syncToken) {
    AutoEscapeValues values = new AutoEscapeValues();
    values.put("token", syncToken);
    db.delete(TABLE_SYNC_INFO, null, null);
    db.insert(TABLE_SYNC_INFO, null, values.get());
  }

  private void processResource(CDAResource resource) {
    CDAType type = resource.type();
    LocalizedResource localized = (LocalizedResource) resource;
    if (type == ASSET) {
      saveAsset((CDAAsset) resource);
    } else if (type == ENTRY) {
      Class<?> modelClass = spaceHelper.getTypes().get(((CDAEntry) localized).contentType().id());
      if (modelClass == null) {
        return;
      }
      ModelHelper<?> modelHelper = spaceHelper.getModels().get(modelClass);
      saveEntry((CDAEntry) resource, modelHelper.getTableName(), modelHelper.getFields());
    }
  }

  private void deleteAsset(String id) {
    deleteResource(id, TABLE_ASSETS);
  }

  private void deleteEntry(String id) {
    String contentTypeId = LinkResolver.fetchEntryType(db, id);
    if (contentTypeId != null) {
      Class<?> clazz = spaceHelper.getTypes().get(contentTypeId);
      if (clazz != null) {
        deleteResource(id, spaceHelper.getModels().get(clazz).getTableName());
        deleteEntryType(id);
      }
    }
  }

  private void deleteEntryType(String remoteId) {
    String whereClause = REMOTE_ID + " = ?";
    String[] whereArgs = new String[]{ remoteId };
    db.delete(TABLE_ENTRY_TYPES, whereClause, whereArgs);
  }

  private void deleteResource(String remoteId, String tableName) {
    // resource
    String resWhere = REMOTE_ID + " = ?";
    String resArgs[] = new String[]{ remoteId };

    // links
    String linksWhere = "`parent` = ? OR `child` = ?";
    String linkArgs[] = new String[]{
        remoteId,
        remoteId
    };

    for (String code : spaceHelper.getLocales()) {
      db.delete(escape(localizeName(tableName, code)), resWhere, resArgs);
      db.delete(escape(localizeName(TABLE_LINKS, code)), linksWhere, linkArgs);
    }
  }

  @TargetApi(Build.VERSION_CODES.FROYO)
  private void saveAsset(CDAAsset asset) {
    AutoEscapeValues values = new AutoEscapeValues();
    for (String code : spaceHelper.getLocales()) {
      asset.setLocale(code);
      putResourceFields(asset, values);
      values.put(Asset.Fields.URL, "http:" + asset.url());
      values.put(Asset.Fields.MIME_TYPE, asset.mimeType());
      values.put(Asset.Fields.TITLE, asset.title());
      values.put(Asset.Fields.DESCRIPTION, asset.<String>getField("description"));

      byte[] value = null;
      Serializable fileMap = asset.getField("file");
      if (fileMap != null) {
        try {
          value = BlobUtils.toBlob(fileMap);
        } catch (IOException e) {
          throw new RuntimeException(
              String.format("Failed converting field map for asset with id '%s'.", asset.id()));
        }
      }
      values.put(Asset.Fields.FILE, value);

      db.insertWithOnConflict(escape(localizeName(TABLE_ASSETS, code)), null, values.get(),
          CONFLICT_REPLACE);

      values.clear();
    }
  }

  @SuppressWarnings("unchecked")
  private <T> T extractRawFieldValue(CDAEntry entry, String fieldId) {
    Map<?, ?> value = (Map<?, ?>) entry.rawFields().get(fieldId);
    if (value != null) {
      T result = (T) value.get(entry.locale());
      if (result == null) {
        result = (T) value.get(spaceHelper.getDefaultLocale());
      }
      return result;
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private void saveEntry(CDAEntry entry, String tableName, List<FieldMeta> fields) {
    AutoEscapeValues values = new AutoEscapeValues();
    for (String code : spaceHelper.getLocales()) {
      entry.setLocale(code);
      putResourceFields(entry, values);
      for (FieldMeta field : fields) {
        Object value = extractRawFieldValue(entry, field.id());
        if (field.isLink()) {
          processLink(entry, field.id(), (Map<?, ?>) value, 0);
        } else if (field.isArray()) {
          processArray(entry, values, field);
        } else if ("BLOB".equals(field.sqliteType())) {
          saveBlob(entry, values, field, (Serializable) value);
        } else if ("BOOL".equals(field.sqliteType())) {
          saveBoolean(values, field, (Boolean) value);
        } else {
          String stringValue = null;
          if (value != null) {
            stringValue = value.toString();
          }
          values.put(field.name(), stringValue);
        }
      }

      db.insertWithOnConflict(escape(localizeName(tableName, code)), null, values.get(),
          CONFLICT_REPLACE);

      values.clear();
    }

    values.put(REMOTE_ID, entry.id());
    values.put("type_id", entry.contentType().id());
    db.insertWithOnConflict(escape(TABLE_ENTRY_TYPES), null, values.get(), CONFLICT_REPLACE);
  }

  private void saveBoolean(AutoEscapeValues values, FieldMeta field, Boolean value) {
    String write = "0";
    if (value != null && value) {
      write = "1";
    }
    values.put(field.name(), write);
  }

  private void processArray(CDAEntry entry, AutoEscapeValues values, FieldMeta field) {
    if (field.isArrayOfSymbols()) {
      List<?> list = entry.getField(field.id());
      if (list == null) {
        list = Collections.emptyList();
      }
      saveBlob(entry, values, field, (Serializable) list);
    } else {
      // Array of resources
      deleteResourceLinks(entry.id(), field.id());

      List<?> links = extractRawFieldValue(entry, field.id());
      if (links != null) {
        for (int i = 0; i < links.size(); i++) {
          processLink(entry, field.id(), (Map<?, ?>) links.get(i), i);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void processLink(CDAEntry entry, String fieldId, Map<?, ?> value, int position) {
    String parentId = entry.id();
    if (value != null) {
      Map<String, ?> linkInfo = (Map<String, ?>) value.get("sys");
      if (linkInfo != null) {
        String linkType = (String) linkInfo.get("linkType");
        String targetId = (String) linkInfo.get("id");

        if (linkType != null && targetId != null) {
          saveLink(parentId, fieldId, linkType, targetId, position);
        }
      }
    } else {
      deleteResourceLinks(parentId, fieldId);
    }
  }

  private void saveBlob(CDAEntry entry, AutoEscapeValues values, FieldMeta field,
      Serializable value) {
    try {
      values.put(field.name(), BlobUtils.toBlob(value));
    } catch (IOException e) {
      throw new RuntimeException(
          String.format("Failed converting value to BLOB for entry id %s field %s.", entry.id(),
              field.name()));
    }
  }

  private void saveLink(String parentId, String fieldId, String linkType, String targetId,
      int position) {
    AutoEscapeValues values = new AutoEscapeValues();

    for (String code : spaceHelper.getLocales()) {
      values.put("parent", parentId);
      values.put("field", fieldId);
      values.put("child", targetId);
      values.put("position", position);
      values.put("is_asset", CDAType.valueOf(linkType.toUpperCase(Vault.LOCALE)) == ASSET);

      db.insertWithOnConflict(escape(localizeName(TABLE_LINKS, code)), null, values.get(),
          CONFLICT_REPLACE);

      values.clear();
    }
  }

  private void deleteResourceLinks(String parentId, String field) {
    String where = "parent = ? AND field = ?";
    String[] args = new String[]{ parentId, field };

    for (String code : spaceHelper.getLocales()) {
      db.delete(escape(localizeName(TABLE_LINKS, code)), where, args);
    }
  }

  private static void putResourceFields(CDAResource resource, AutoEscapeValues values) {
    values.put(REMOTE_ID, resource.id());
    values.put(CREATED_AT, (String) resource.getAttribute("createdAt"));
    values.put(UPDATED_AT, (String) resource.getAttribute("updatedAt"));
  }

  static class Builder {
    private Context context;
    private SqliteHelper sqliteHelper;
    private SyncConfig config;
    private String tag;

    private Builder() {
    }

    Builder setContext(Context context) {
      this.context = context.getApplicationContext();
      return this;
    }

    Builder setSqliteHelper(SqliteHelper sqliteHelper) {
      this.sqliteHelper = sqliteHelper;
      return this;
    }

    Builder setSyncConfig(SyncConfig config) {
      this.config = config;
      return this;
    }

    Builder setTag(String tag) {
      this.tag = tag;
      return this;
    }

    public SyncRunnable build() {
      return new SyncRunnable(this);
    }
  }
}
