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
  private SQLiteDatabase db;

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

  public SyncRunnable(Context context, Class<?> space, CDAClient client) {
    this.context = context;
    this.space = space;
    this.client = client;
  }

  @Override public void run() {
    helper = Persistence.getOrCreateDbHelper(context, space);
    db = ((SQLiteOpenHelper) helper).getWritableDatabase();

    try {
      CDASyncedSpace syncedSpace = client.synchronization().performInitial();
      db.beginTransaction();
      try {
        for (CDAResource resource : syncedSpace.getItems()) {
          if (wasDeleted(resource)) {
            HANDLER_DELETE.invoke(resource);
          } else {
            List<FieldMeta> fields = null;
            String tableName = null;
            if (isOfType(resource, Entry)) {
              Class<?> clazz = helper.getTypesMap().get(
                  extractContentTypeId(resource));

              tableName = helper.getTablesMap().get(clazz);
              if (tableName == null) {
                continue; // TODO show warning - skipping unregistered type
              }
              fields = helper.getFieldsMap().get(clazz);
            }
            HANDLER_SAVE.invoke(resource, tableName, fields);
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

  private void deleteAsset(CDAResource resource) {
    deleteResource(extractResourceId(resource), DbHelper.TABLE_ASSETS);
  }

  private void deleteEntry(CDAResource resource) {
    String remoteId = extractResourceId(resource);
    String contentTypeId = fetchContentTypeId(remoteId);
    if (contentTypeId != null) {
      Class<?> clazz = helper.getTypesMap().get(contentTypeId);
      if (clazz != null) {
        deleteResource(remoteId, helper.getTablesMap().get(clazz));
        deleteEntryType(remoteId);
      }
    }
  }

  private void deleteEntryType(String remoteId) {
    String whereClause = "remote_id = ?";
    String[] whereArgs = new String[]{ remoteId };
    db.delete(DbHelper.TABLE_ENTRY_TYPES, whereClause, whereArgs);
  }

  private String fetchContentTypeId(String remoteId) {
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
    db.delete(DbHelper.TABLE_LINKS, whereClause, whereArgs);
  }

  @TargetApi(Build.VERSION_CODES.FROYO)
  private void saveAsset(CDAAsset asset) {
    ContentValues values = new ContentValues();
    putResourceFields(asset, values);
    values.put("url", asset.getUrl());
    values.put("mime_type", asset.getMimeType());
    db.insertWithOnConflict(DbHelper.TABLE_ASSETS, null, values, CONFLICT_REPLACE);
  }

  @SuppressWarnings("unchecked")
  private void saveEntry(CDAEntry entry, Object... objects) {
    String tableName = (String) objects[0];
    List<FieldMeta> fields = (List<FieldMeta>) objects[1];

    ContentValues values = new ContentValues();
    putResourceFields(entry, values);
    for (FieldMeta field : fields) {
      Object value = entry.getFields().get(field.id);
      if (field.isLink()) {
        if (value == null) {
          deleteResourceLinks(entry);
        } else if (value instanceof CDAResource) { // TODO enforcing nullifyUnresolved will make this test redundant
          //noinspection ConstantConditions
          saveLink(entry, field.name, (CDAResource) value);
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

  private void saveLink(CDAResource parent, String fieldName,
      CDAResource child) {
    String parentRemoteId = extractResourceId(parent);
    String childRemoteId = extractResourceId(child);
    String childContentType = null;

    if (!isOfType(child, CDAResourceType.Asset)) {
      childContentType = extractContentTypeId(child);
    }

    ContentValues values = new ContentValues();
    values.put("parent", parentRemoteId);
    values.put("field", fieldName);
    values.put("child", childRemoteId);
    values.put("child_content_type", childContentType);
    db.insertWithOnConflict(DbHelper.TABLE_LINKS, null, values, CONFLICT_REPLACE);
  }

  private void deleteResourceLinks(CDAResource resource) {
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
}
