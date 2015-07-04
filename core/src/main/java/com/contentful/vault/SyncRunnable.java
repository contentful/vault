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
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import com.contentful.java.cda.Constants.CDAResourceType;
import com.contentful.java.cda.model.CDAAsset;
import com.contentful.java.cda.model.CDAEntry;
import com.contentful.java.cda.model.CDAResource;
import com.contentful.java.cda.model.CDASyncedSpace;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;
import static com.contentful.java.cda.Constants.CDAResourceType.Asset;
import static com.contentful.java.cda.Constants.CDAResourceType.DeletedAsset;
import static com.contentful.java.cda.Constants.CDAResourceType.DeletedEntry;
import static com.contentful.java.cda.Constants.CDAResourceType.Entry;
import static com.contentful.vault.CDAUtils.extractContentTypeId;
import static com.contentful.vault.CDAUtils.extractResourceId;
import static com.contentful.vault.CDAUtils.extractResourceType;
import static com.contentful.vault.CDAUtils.isOfType;
import static com.contentful.vault.CDAUtils.wasDeleted;

public final class SyncRunnable implements Runnable {
  private final Context context;

  private final SyncConfig config;

  private SqliteHelper sqliteHelper;

  private SpaceHelper spaceHelper;

  private SQLiteDatabase db;

  private String tag;

  private final ResourceHandler HANDLER_DELETE = new ResourceHandler() {
    @Override void asset(CDAResource resource, Object... objects) {
      deleteAsset(resource);
    }

    @Override void entry(CDAResource resource, Object... objects) {
      deleteEntry(resource);
    }
  };

  private final ResourceHandler HANDLER_SAVE = new ResourceHandler() {
    @Override void asset(CDAResource resource, Object... objects) {
      saveAsset((CDAAsset) resource);
    }

    @Override void entry(CDAResource resource, Object... objects) {
      saveEntry((CDAEntry) resource, objects);
    }
  };

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

      CDASyncedSpace syncedSpace;
      if (token == null) {
        syncedSpace = config.client().synchronization().performInitial();
      } else {
        checkLocale();
        syncedSpace = config.client().synchronization().performWithToken(token);
      }

      db.beginTransaction();
      try {
        for (CDAResource resource : syncedSpace.getItems()) {
          processResource(resource);
        }

        saveSyncInfo(syncedSpace.getSyncToken());
        db.setTransactionSuccessful();
      } finally {
        db.endTransaction();
      }
    } catch (Exception e) {
      error = new SyncException(e);
    } finally {
      boolean success = error == null;

      context.sendBroadcast(new Intent(Vault.ACTION_SYNC_COMPLETE)
          .putExtra(Vault.EXTRA_SUCCESS, success));

      Vault.executeCallback(tag, error);
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
    ContentValues values = new ContentValues();
    values.put("token", syncToken);
    values.put("locale", config.locale());
    db.delete(SpaceHelper.TABLE_SYNC_INFO, null, null);
    db.insert(SpaceHelper.TABLE_SYNC_INFO, null, values);
  }

  private void processResource(CDAResource resource) {
    if (wasDeleted(resource)) {
      HANDLER_DELETE.invoke(resource);
    } else {
      if (StringUtils.isNotBlank(config.locale())) {
        resource.setLocale(config.locale());
      }

      List<FieldMeta> fields = null;
      String tableName = null;
      if (isOfType(resource, Entry)) {
        Class<?> modelClass = spaceHelper.getTypes().get(extractContentTypeId(resource));
        if (modelClass == null) {
          return;
        }

        ModelHelper<?> modelHelper = spaceHelper.getModels().get(modelClass);
        tableName = modelHelper.getTableName();
        fields = modelHelper.getFields();
      }
      HANDLER_SAVE.invoke(resource, tableName, fields);
    }
  }

  private void checkLocale() {
    Cursor cursor = db.rawQuery("SELECT `locale` FROM sync_info", null);
    try {
      if (cursor.moveToFirst()) {
        String previousLocale = cursor.getString(0);
        if (!StringUtils.equals(config.locale(), previousLocale)) {
          SqliteHelper.clearRecords(spaceHelper, db);
        }
      }
    } finally {
      cursor.close();
    }
  }

  private void deleteAsset(CDAResource resource) {
    deleteResource(extractResourceId(resource), SpaceHelper.TABLE_ASSETS);
  }

  private void deleteEntry(CDAResource resource) {
    String remoteId = extractResourceId(resource);
    String contentTypeId = LinkResolver.fetchEntryType(db, remoteId);
    if (contentTypeId != null) {
      Class<?> clazz = spaceHelper.getTypes().get(contentTypeId);
      if (clazz != null) {
        deleteResource(remoteId, spaceHelper.getModels().get(clazz).getTableName());
        deleteEntryType(remoteId);
      }
    }
  }

  private void deleteEntryType(String remoteId) {
    String whereClause = "remote_id = ?";
    String[] whereArgs = new String[]{ remoteId };
    db.delete(SpaceHelper.TABLE_ENTRY_TYPES, whereClause, whereArgs);
  }

  private void deleteResource(String remoteId, String tableName) {
    // resource
    String whereClause = "remote_id = ?";
    String whereArgs[] = new String[]{ remoteId };
    db.delete(tableName, whereClause, whereArgs);

    // links
    whereClause = "`parent` = ? OR `child` = ?";
    whereArgs = new String[]{
        remoteId,
        remoteId
    };
    db.delete(SpaceHelper.TABLE_LINKS, whereClause, whereArgs);
  }

  @TargetApi(Build.VERSION_CODES.FROYO)
  private void saveAsset(CDAAsset asset) {
    ContentValues values = new ContentValues();
    putResourceFields(asset, values);
    values.put("url", asset.getUrl());
    values.put("mime_type", asset.getMimeType());
    db.insertWithOnConflict(SpaceHelper.TABLE_ASSETS, null, values, CONFLICT_REPLACE);
  }

  private <T> T extractRawFieldValue(CDAEntry entry, String fieldId) {
    Map value = (Map) entry.getRawFields().get(fieldId);
    if (value != null) {
      //noinspection unchecked
      return (T) value.get(entry.getLocale());
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private void saveEntry(CDAEntry entry, Object... objects) {
    String tableName = (String) objects[0];
    List<FieldMeta> fields = (List<FieldMeta>) objects[1];

    ContentValues values = new ContentValues();
    putResourceFields(entry, values);
    for (FieldMeta field : fields) {
      Object value = extractRawFieldValue(entry, field.id());
      if (field.isLink()) {
        processLink(entry, field.id(), (Map) value);
      } else if (field.isArray()) {
        processArray(entry, values, field);
      } else if ("BLOB".equals(field.sqliteType())) {
        if (value == null) {
          value = Collections.emptyMap();
        }
        saveBlob(entry, values, field, (Serializable) value);
      } else {
        String stringValue = null;
        if (value != null) {
          stringValue = value.toString();
        }
        values.put(escape(field.name()), stringValue);
      }
    }
    db.insertWithOnConflict(tableName, null, values, CONFLICT_REPLACE);

    values.clear();
    values.put("remote_id", extractResourceId(entry));
    values.put("type_id", extractContentTypeId(entry));
    db.insertWithOnConflict(SpaceHelper.TABLE_ENTRY_TYPES, null, values, CONFLICT_REPLACE);
  }

  private static String escape(String value) {
    return "`" + value + "`";
  }

  private void processArray(CDAEntry entry, ContentValues values, FieldMeta field) {
    if (field.isArrayOfSymbols()) {
      List list = (List) entry.getFields().get(field.id());
      if (list == null) {
        list = Collections.emptyList();
      }
      saveBlob(entry, values, field, (Serializable) list);
    } else {
      // Array of resources
      String entryId = extractResourceId(entry);
      deleteResourceLinks(entryId, field.id());

      List links = extractRawFieldValue(entry, field.id());
      if (links != null) {
        for (Object link : links) {
          processLink(entry, field.id(), (Map) link);
        }
      }
    }
  }

  private void processLink(CDAEntry entry, String fieldId, Map value) {
    String parentId = extractResourceId(entry);
    if (value != null) {
      Map linkInfo = (Map) value.get("sys");
      if (linkInfo != null) {
        String linkType = (String) linkInfo.get("linkType");
        String targetId = (String) linkInfo.get("id");

        if (linkType != null && targetId != null) {
          saveLink(parentId, fieldId, linkType, targetId);
        }
      }
    } else {
      deleteResourceLinks(parentId, fieldId);
    }
  }

  private void saveBlob(CDAEntry entry, ContentValues values, FieldMeta field, Serializable value) {
    try {
      values.put(escape(field.name()), BlobUtils.toBlob(value));
    } catch (IOException e) {
      throw new RuntimeException(
          String.format("Failed converting value to BLOB for entry id %s field %s.",
              extractResourceId(entry), field.name()));
    }
  }

  private void saveLink(String parentId, String fieldId, String linkType, String targetId) {
    ContentValues values = new ContentValues();
    values.put("parent", parentId);
    values.put("field", fieldId);
    values.put("child", targetId);
    values.put("is_asset", CDAResourceType.valueOf(linkType).equals(Asset));
    db.insertWithOnConflict(SpaceHelper.TABLE_LINKS, null, values, CONFLICT_REPLACE);
  }

  private void deleteResourceLinks(String parentId, String field) {
    String where = "parent = ? AND field = ?";
    String[] args = new String[]{ parentId, field };
    db.delete(SpaceHelper.TABLE_LINKS, where, args);
  }

  private static void putResourceFields(CDAResource resource, ContentValues values) {
    values.put("remote_id", extractResourceId(resource));
    values.put("created_at", (String) resource.getSys().get("createdAt"));
    values.put("updated_at", (String) resource.getSys().get("updatedAt"));
  }

  static abstract class ResourceHandler {
    abstract void asset(CDAResource resource, Object... objects);
    abstract void entry(CDAResource resource, Object... objects);

    void invoke(CDAResource resource, Object... objects) {
      CDAResourceType resourceType = CDAResourceType.valueOf(extractResourceType(resource));
      if (Asset.equals(resourceType) || DeletedAsset.equals(resourceType)) {
        asset(resource, objects);
      } else if (Entry.equals(resourceType) || DeletedEntry.equals(resourceType)) {
        entry(resource, objects);
      }
    }
  }

  static class Builder {
    private Context context;
    private SqliteHelper sqliteHelper;
    private SyncConfig config;
    private String tag;

    private Builder() {
    }

    public Builder setContext(Context context) {
      this.context = context.getApplicationContext();
      return this;
    }

    public Builder setSqliteHelper(SqliteHelper sqliteHelper) {
      this.sqliteHelper = sqliteHelper;
      return this;
    }

    public Builder setSyncConfig(SyncConfig config) {
      this.config = config;
      return this;
    }

    public Builder setTag(String tag) {
      this.tag = tag;
      return this;
    }

    public SyncRunnable build() {
      return new SyncRunnable(this);
    }
  }
}
