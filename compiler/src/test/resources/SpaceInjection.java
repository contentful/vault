package test;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.contentful.sqlite.FieldMeta;
import com.contentful.sqlite.PersistenceHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class Test$Db$$Space extends SQLiteOpenHelper implements PersistenceHelper {
  static Test$Db$$Space instance;

  static final Set<Class<?>> models = new LinkedHashSet<Class<?>>();

  static final Map<Class<?>, String> tables = new LinkedHashMap<Class<?>, String>();

  static final Map<String, Class<?>> types = new LinkedHashMap<String, Class<?>>();

  static final Map<Class<?>, List<FieldMeta>> fields = new LinkedHashMap<Class<?>, List<FieldMeta>>();

  private Test$Db$$Space(Context context) {
    super(context, "space_c2lk", null, 1);
    Class<?> clazz;
    ArrayList<FieldMeta> fieldsHolder = new ArrayList<FieldMeta>();
    clazz = test.Test.Model.class;
    types.put("cid", clazz);
    tables.put(clazz, "entry_y2lk");
    fieldsHolder.add(new FieldMeta("fText", "fText", "STRING", null, "java.lang.String"));
    fieldsHolder.add(new FieldMeta("fBoolean", "fBoolean", "INT", null, "java.lang.Boolean"));
    fieldsHolder.add(new FieldMeta("fInteger", "fInteger", "INT", null, "java.lang.Integer"));
    fieldsHolder.add(new FieldMeta("fDouble", "fDouble", "DOUBLE", null, "java.lang.Double"));
    fieldsHolder.add(new FieldMeta("fMap", "fMap", "BLOB", null, "java.util.Map"));
    fieldsHolder.add(new FieldMeta("fLinkedModel", "fLinkedModel", null, "Entry", "test.Test.Model"));
    fieldsHolder.add(new FieldMeta("fLinkedAsset", "fLinkedAsset", null, "Asset", "com.contentful.sqlite.Asset"));
    fields.put(clazz, fieldsHolder);
    models.addAll(tables.keySet());
  }

  public static synchronized Test$Db$$Space get(Context context) {
    if (instance == null) {
      instance = new Test$Db$$Space(context);
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
  public Set<Class<?>> getModels() {
    return models;
  }

  @Override
  public Map<Class<?>, String> getTables() {
    return tables;
  }

  @Override
  public Map<String, Class<?>> getTypes() {
    return types;
  }

  @Override
  public Map<Class<?>, List<FieldMeta>> getFields() {
    return fields;
  }
}