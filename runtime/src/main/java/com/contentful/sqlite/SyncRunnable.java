package com.contentful.sqlite;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import com.contentful.java.cda.*;
import com.contentful.java.cda.Constants.CDAResourceType;
import com.contentful.java.cda.model.CDAAsset;
import com.contentful.java.cda.model.CDAEntry;
import com.contentful.java.cda.model.CDAResource;
import com.contentful.java.cda.model.CDASyncedSpace;
import java.util.List;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;
import static com.contentful.java.cda.Constants.CDAResourceType.Asset;
import static com.contentful.java.cda.Constants.CDAResourceType.DeletedAsset;
import static com.contentful.java.cda.Constants.CDAResourceType.DeletedEntry;
import static com.contentful.java.cda.Constants.CDAResourceType.Entry;
import static com.contentful.sqlite.CfUtils.extractContentTypeId;
import static com.contentful.sqlite.CfUtils.extractResourceId;
import static com.contentful.sqlite.CfUtils.extractResourceType;
import static com.contentful.sqlite.CfUtils.isOfType;
import static com.contentful.sqlite.CfUtils.wasDeleted;

public final class SyncRunnable implements Runnable {
  private final Context context;
  private final Class<?> space;
  private final CDAClient client;
  private DbHelper helper;

  private final ResourceHandler HANDLER_DELETE = new ResourceHandler() {
    @Override void asset(CDAResource resource, SQLiteDatabase db, Object... objects) {
      deleteAsset(resource, db);
    }

    @Override void entry(CDAResource resource, SQLiteDatabase db, Object... objects) {
      deleteEntry(resource, db);
    }
  };

  private final ResourceHandler HANDLER_SAVE = new ResourceHandler() {
    @Override void asset(CDAResource resource, SQLiteDatabase db, Object... objects) {
      saveAsset((CDAAsset) resource, db);
    }

    @Override void entry(CDAResource resource, SQLiteDatabase db, Object... objects) {
      saveEntry((CDAEntry) resource, db, objects);
    }
  };

  public SyncRunnable(Context context, Class<?> space, CDAClient client) {
    this.context = context;
    this.space = space;
    this.client = client;
  }

  @Override public void run() {
    helper = Persistence.getOrCreateDbHelper(context, space);
    try {
      CDASyncedSpace syncedSpace = client.synchronization().performInitial();
      SQLiteDatabase db = ((SQLiteOpenHelper) helper).getWritableDatabase();
      db.beginTransaction();
      try {
        for (CDAResource resource : syncedSpace.getItems()) {
          if (wasDeleted(resource)) {
            HANDLER_DELETE.invoke(resource, db);
          } else {
            List<FieldMeta> fields = null;
            String tableName = null;
            if (isOfType(resource, Entry)) {
              Class<?> clazz = helper.getTypesMap().get(
                  extractContentTypeId((CDAEntry) resource));

              tableName = helper.getTablesMap().get(clazz);
              if (tableName == null) {
                continue; // TODO show warning - skipping unregistered type
              }
              fields = helper.getFieldsMap().get(clazz);
            }
            HANDLER_SAVE.invoke(resource, db, tableName, fields);
          }
        }
        db.setTransactionSuccessful();
      } finally {
        db.endTransaction();
      }
    } catch (Exception e) {
      throw new SyncException(e);
    }
  }

  private void deleteAsset(CDAResource resource, SQLiteDatabase db) {
    deleteResource(extractResourceId(resource), DbHelper.TABLE_ASSETS, db);
  }

  private void deleteEntry(CDAResource resource, SQLiteDatabase db) {
    String remoteId = extractResourceId(resource);
    String contentTypeId = fetchContentTypeId(remoteId, db);
    if (contentTypeId != null) {
      Class<?> clazz = helper.getTypesMap().get(contentTypeId);
      if (clazz != null) {
        deleteResource(extractResourceId(resource), helper.getTablesMap().get(clazz), db);
      }
    }
  }

  private String fetchContentTypeId(String remoteId, SQLiteDatabase db) {
    String sql = "SELECT `type_id` FROM " + DbHelper.TABLE_ENTRY_TYPES + " WHERE remote_id = ?";
    String args[] = new String[]{ remoteId };
    String result = null;
    Cursor cursor = db.rawQuery(sql, args);
    try {
      if (cursor.moveToFirst()) {
        result = cursor.getString(0);
      }
    } finally {
      cursor.close();
    }
    return result;
  }

  private static void deleteResource(String remoteId, String tableName, SQLiteDatabase db) {
    String whereClause = "remote_id = ?";
    String whereArgs[] = new String[]{ remoteId };
    db.delete(tableName, whereClause, whereArgs);
    // TODO invalidate any links pointing to this resource.
    // TODO delete from entry_types *for entries*
  }

  @TargetApi(Build.VERSION_CODES.FROYO)
  private static void saveAsset(CDAAsset asset, SQLiteDatabase db) {
    ContentValues values = new ContentValues();
    putResourceFields(asset, values);
    values.put("url", asset.getUrl());
    values.put("mime_type", asset.getMimeType());
    db.insertWithOnConflict(DbHelper.TABLE_ASSETS, null, values, CONFLICT_REPLACE);
  }

  @SuppressWarnings("unchecked")
  private void saveEntry(CDAEntry entry, SQLiteDatabase db, Object... objects) {
    String tableName = (String) objects[0];
    List<FieldMeta> fields = (List<FieldMeta>) objects[1];

    ContentValues values = new ContentValues();
    putResourceFields(entry, values);
    for (FieldMeta field : fields) {
      Object value = entry.getFields().get(field.id);
      if (field.isLink()) {
        if (value == null) {
          deleteResourceLinks(db, entry);
        } else if (value instanceof CDAResource) { // TODO enforcing nullifyUnresolved will make this test redundant
          //noinspection ConstantConditions
          saveLink(db, entry, field.name, (CDAResource) value);
        }
      } else {
        if (value != null) {
          values.put(field.name, value.toString());
        }
      }
    }
    db.insertWithOnConflict(tableName, null, values, CONFLICT_REPLACE);

    values.clear();
    values.put("remote_id", extractResourceId(entry));
    values.put("type_id", extractContentTypeId(entry));
    db.insertWithOnConflict(DbHelper.TABLE_ENTRY_TYPES, null, values, CONFLICT_REPLACE);
  }

  private void saveLink(SQLiteDatabase db, CDAResource parent, String fieldName,
      CDAResource child) {
    String parentRemoteId = extractResourceId(parent);
    String childRemoteId = extractResourceId(child);
    String childContentType = null;

    if (!isOfType(child, CDAResourceType.Asset)) {
      childContentType = extractContentTypeId((CDAEntry) child);
    }

    ContentValues values = new ContentValues();
    values.put("parent", parentRemoteId);
    values.put("field", fieldName);
    values.put("child", childRemoteId);
    values.put("child_content_type", childContentType);
    db.insertWithOnConflict(DbHelper.TABLE_LINKS, null, values, CONFLICT_REPLACE);
  }

  private static void deleteResourceLinks(SQLiteDatabase db, CDAResource resource) {
    String where = "parent = ?";
    String[] args = new String[]{ extractResourceId(resource) };
    db.delete(DbHelper.TABLE_LINKS, where, args);
  }

  private static void putResourceFields(CDAResource resource, ContentValues values) {
    values.put("remote_id", extractResourceId(resource));
    values.put("created_at", (String) resource.getSys().get("createdAt"));
    values.put("updated_at", (String) resource.getSys().get("updatedAt"));
  }

  static abstract class ResourceHandler {
    abstract void asset(CDAResource resource, SQLiteDatabase db, Object... objects);
    abstract void entry(CDAResource resource, SQLiteDatabase db, Object... objects);

    void invoke(CDAResource resource, SQLiteDatabase db, Object... objects) {
      CDAResourceType resourceType = CDAResourceType.valueOf(extractResourceType(resource));
      if (Asset.equals(resourceType) || DeletedAsset.equals(resourceType)) {
        asset(resource, db, objects);
      } else if (Entry.equals(resourceType) || DeletedEntry.equals(resourceType)) {
        entry(resource, db, objects);
      }
    }
  }
}
