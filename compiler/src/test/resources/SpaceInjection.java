package test;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.contentful.sqlite.ModelHelper;
import com.contentful.sqlite.PersistenceHelper;
import java.util.LinkedHashMap;
import java.util.Map;

final class Test$AwesomeSpace$$SpaceHelper extends SQLiteOpenHelper implements PersistenceHelper {
  static Test$AwesomeSpace$$SpaceHelper instance;

  final Map<Class<?>, ModelHelper<?>> models = new LinkedHashMap<Class<?>, ModelHelper<?>>();

  final Map<String, Class<?>> types = new LinkedHashMap<String, Class<?>>();

  private Test$AwesomeSpace$$SpaceHelper(Context context) {
    super(context, "space_c2lk", null, 1);
    models.put(test.Test.Model.class, new Test$Model$$ModelHelper());
    types.put("cid", test.Test.Model.class);
  }

  public static synchronized Test$AwesomeSpace$$SpaceHelper get(Context context) {
    if (instance == null) {
      instance = new Test$AwesomeSpace$$SpaceHelper(context);
    }
    return instance;
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.beginTransaction();
    try {
      for (String sql : DEFAULT_CREATE) {
        db.execSQL(sql);
      }
      db.execSQL("CREATE TABLE `entry_y2lk` (`remote_id` STRING NOT NULL UNIQUE, `created_at` STRING NOT NULL, `updated_at` STRING, `fText` STRING, `fBoolean` INT, `fInteger` INT, `fDouble` DOUBLE, `fMap` BLOB);");
      db.setTransactionSuccessful();
    } finally{
      db.endTransaction();
    }
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
  }

  @Override
  public Map<Class<?>, ModelHelper<?>> getModels() {
    return models;
  }

  @Override
  public Map<String, Class<?>> getTypes() {
    return types;
  }
}