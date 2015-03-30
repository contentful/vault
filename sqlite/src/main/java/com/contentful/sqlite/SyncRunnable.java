package com.contentful.sqlite;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import com.contentful.java.cda.CDAClient;
import com.contentful.java.cda.model.CDAAsset;
import com.contentful.java.cda.model.CDAEntry;
import com.contentful.java.cda.model.CDAResource;
import com.contentful.java.cda.model.CDASyncedSpace;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.contentful.java.cda.Constants.CDAResourceType.Asset;
import static com.contentful.java.cda.Constants.CDAResourceType.DeletedAsset;
import static com.contentful.java.cda.Constants.CDAResourceType.DeletedEntry;
import static com.contentful.java.cda.Constants.CDAResourceType.Entry;

public final class SyncRunnable implements Runnable {
  private final Context context;
  private final Class<?> space;
  private final CDAClient client;

  private static final ResourceHandler HANDLER_DELETE = new ResourceHandler() {
    @Override void asset(CDAAsset asset, DbHelper helper, SQLiteDatabase db) {
      deleteAsset(asset, helper, db);
    }

    @Override void entry(CDAEntry entry, DbHelper helper, SQLiteDatabase db) {
      // TODO delete entry
    }
  };

  private static final ResourceHandler HANDLER_SAVE = new ResourceHandler() {
    @Override void asset(CDAAsset asset, DbHelper helper, SQLiteDatabase db) {
      saveAsset(asset, db);
    }

    @Override void entry(CDAEntry entry, DbHelper helper, SQLiteDatabase db) {
      // TODO
    }
  };

  public SyncRunnable(Context context, Class<?> space, CDAClient client) {
    this.context = context;
    this.space = space;
    this.client = client;
  }

  @Override public void run() {
    DbHelper helper = findDbHelper(space);
    CDASyncedSpace syncedSpace = null;
    try {
       syncedSpace = client.synchronization().performInitial();
    } catch (Exception e) {
      e.printStackTrace();
    }
    SQLiteDatabase db = ((SQLiteOpenHelper) helper).getWritableDatabase();
    db.beginTransaction();
    try {
      for (CDAResource resource : syncedSpace.getItems()) {
        if (isDeleted(resource)) {
          HANDLER_DELETE.invoke(resource, helper, db);
        } else {
          HANDLER_SAVE.invoke(resource, helper, db);
        }
      }
      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
    }
  }

  private DbHelper findDbHelper(Class<?> space) {
    synchronized (Persistence.INJECTORS) {
      DbHelper helper = Persistence.INJECTORS.get(space);
      if (helper == null) {
        try {
          Class<?> clazz = Class.forName(space.getName() + Constants.SUFFIX_SPACE);
          Method get = clazz.getMethod("get", Context.class);
          helper = (DbHelper) get.invoke(null, context);
          if (helper == null) {
            throw new SyncException(
                "Space injector returned empty helper for class \"" + space.getName() + "\".");
          }
          Persistence.INJECTORS.put(space, helper);
        } catch (ClassNotFoundException e) {
          throw new SyncException(e);
        } catch (InvocationTargetException e) {
          throw new SyncException(e);
        } catch (NoSuchMethodException e) {
          throw new SyncException(e);
        } catch (IllegalAccessException e) {
          throw new SyncException(e);
        }
      }
      return helper;
    }
  }

  private static void deleteAsset(CDAAsset asset, DbHelper helper, SQLiteDatabase db) {
    String whereClause = "REMOTE_ID = ?";
    String whereArgs[] = new String[]{ extractResourceId(asset) };
    db.delete(DbHelper.TABLE_ASSETS, whereClause, whereArgs);
    // TODO invalidate any links pointing to this resource.
  }

  @TargetApi(Build.VERSION_CODES.FROYO)
  private static void saveAsset(CDAAsset asset, SQLiteDatabase db) {
    ContentValues values = new ContentValues();
    values.put("remote_id", extractResourceId(asset));
    values.put("url", asset.getUrl());
    values.put("mime_type", asset.getMimeType());
    db.insertWithOnConflict(DbHelper.TABLE_ASSETS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
  }

  private static boolean isDeleted(CDAResource resource) {
    String type = extractResourceType(resource);
    return DeletedAsset.toString().equals(type) || DeletedEntry.toString().equals(type);
  }

  private static String extractResourceType(CDAResource resource) {
    return (String) resource.getSys().get("type");
  }

  private static String extractResourceId(CDAResource resource) {
    return (String) resource.getSys().get("id");
  }

  static abstract class ResourceHandler {
    abstract void asset(CDAAsset asset, DbHelper helper, SQLiteDatabase db);
    abstract void entry(CDAEntry entry, DbHelper helper, SQLiteDatabase db);

    void invoke(CDAResource resource, DbHelper helper, SQLiteDatabase db) {
      String type = extractResourceType(resource);
      if (Asset.toString().equals(type)) {
        asset((CDAAsset) resource, helper, db);
      } else if (Entry.toString().equals(type)) {
        entry((CDAEntry) resource, helper, db);
      }
    }
  }
}
