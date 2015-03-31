package com.contentful.sqlite;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
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
import java.util.Map;

import static com.contentful.java.cda.Constants.CDAResourceType.Asset;
import static com.contentful.java.cda.Constants.CDAResourceType.DeletedAsset;
import static com.contentful.java.cda.Constants.CDAResourceType.DeletedEntry;
import static com.contentful.java.cda.Constants.CDAResourceType.Entry;

public final class SyncRunnable implements Runnable {
  private final Context context;
  private final Class<?> space;
  private final CDAClient client;

  private static final ResourceHandler HANDLER_DELETE = new ResourceHandler() {
    @Override void asset(CDAAsset asset, DbHelper helper, SQLiteDatabase db, Object... objects) {
      deleteAsset(asset, helper, db);
    }

    @Override void entry(CDAEntry entry, DbHelper helper, SQLiteDatabase db, Object... objects) {
      // TODO delete entry
    }
  };

  private static final ResourceHandler HANDLER_SAVE = new ResourceHandler() {
    @Override void asset(CDAAsset asset, DbHelper helper, SQLiteDatabase db, Object... objects) {
      saveAsset(asset, db);
    }

    @Override void entry(CDAEntry entry, DbHelper helper, SQLiteDatabase db, Object... objects) {
      saveEntry(entry, db, objects);
    }
  };

  public SyncRunnable(Context context, Class<?> space, CDAClient client) {
    this.context = context;
    this.space = space;
    this.client = client;
  }

  @Override public void run() {
    DbHelper helper = Persistence.getOrCreateDbHelper(context, space);
    try {
      CDASyncedSpace syncedSpace = client.synchronization().performInitial();
      SQLiteDatabase db = ((SQLiteOpenHelper) helper).getWritableDatabase();
      db.beginTransaction();
      try {
        for (CDAResource resource : syncedSpace.getItems()) {
          if (isDeleted(resource)) {
            HANDLER_DELETE.invoke(resource, helper, db);
          } else {
            List<FieldMeta> fields = null;
            String tableName = null;
            if (isOfType(resource, Entry)) {
              Class<?> clazz = helper.getTypesMap().get(extractContentTypeId((CDAEntry) resource));
              tableName = helper.getTablesMap().get(clazz);
              if (tableName == null) {
                continue; // TODO show warning - skipping unregistered type
              }
              fields = helper.getFieldsMap().get(clazz);
            }
            HANDLER_SAVE.invoke(resource, helper, db, tableName, fields);
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

  private static void deleteAsset(CDAAsset asset, DbHelper helper, SQLiteDatabase db) {
    String whereClause = "remote_id = ?";
    String whereArgs[] = new String[]{ extractResourceId(asset) };
    db.delete(DbHelper.TABLE_ASSETS, whereClause, whereArgs);
    // TODO invalidate any links pointing to this resource.
  }

  @TargetApi(Build.VERSION_CODES.FROYO)
  private static void saveAsset(CDAAsset asset, SQLiteDatabase db) {
    ContentValues values = new ContentValues();
    putResourceFields(asset, values);
    values.put("url", asset.getUrl());
    values.put("mime_type", asset.getMimeType());
    db.insertWithOnConflict(DbHelper.TABLE_ASSETS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
  }

  @SuppressWarnings("unchecked")
  private static void saveEntry(CDAEntry entry, SQLiteDatabase db, Object... objects) {
    String tableName = (String) objects[0];
    List<FieldMeta> fields = (List<FieldMeta>) objects[1];

    ContentValues values = new ContentValues();
    putResourceFields(entry, values);
    for (FieldMeta field : fields) {
      if (field.link) {
        continue; // TODO
      }
      Object value = entry.getFields().get(field.id);
      if (value != null) {
        values.put(field.name, value.toString());
      }
    }
    db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE);
  }

  private static void putResourceFields(CDAResource resource, ContentValues values) {
    values.put("remote_id", extractResourceId(resource));
    values.put("created_at", (String) resource.getSys().get("createdAt"));
    values.put("updated_at", (String) resource.getSys().get("updatedAt"));
  }

  private static boolean isDeleted(CDAResource resource) {
    CDAResourceType resourceType = CDAResourceType.valueOf(extractResourceType(resource));
    return DeletedAsset.equals(resourceType) || DeletedEntry.equals(resourceType);
  }

  private static String extractResourceType(CDAResource resource) {
    return (String) resource.getSys().get("type");
  }

  private static String extractResourceId(CDAResource resource) {
    return (String) resource.getSys().get("id");
  }

  private static String extractContentTypeId(CDAEntry entry) {
    Map contentType = (Map) entry.getSys().get("contentType");
    return (String) ((Map) contentType.get("sys")).get("id");
  }

  private static boolean isOfType(CDAResource resource, CDAResourceType resourceType) {
    return resourceType.equals(CDAResourceType.valueOf(extractResourceType(resource)));
  }

  static abstract class ResourceHandler {
    abstract void asset(CDAAsset asset, DbHelper helper, SQLiteDatabase db, Object... objects);
    abstract void entry(CDAEntry entry, DbHelper helper, SQLiteDatabase db, Object... objects);

    void invoke(CDAResource resource, DbHelper helper, SQLiteDatabase db,Object... objects) {
      String type = extractResourceType(resource);
      if (Asset.toString().equals(type)) {
        asset((CDAAsset) resource, helper, db, objects);
      } else if (Entry.toString().equals(type)) {
        entry((CDAEntry) resource, helper, db, objects);
      }
    }
  }
}
