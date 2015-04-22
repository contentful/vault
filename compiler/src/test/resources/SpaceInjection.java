package test;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import com.contentful.sqlite.FieldMeta;
import com.contentful.sqlite.PersistenceHelper;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Test$Db$$Space extends SQLiteOpenHelper implements PersistenceHelper {
  static Test$Db$$Space instance;

  static final Set<Class<?>> models =
      new LinkedHashSet<Class<?>>(Arrays.asList(
        test.Test.Model.class
      ));

  static final Map<Class<?>, String> tables =
      new LinkedHashMap<Class<?>, String>();

  static final Map<String, Class<?>> types =
      new LinkedHashMap<String, Class<?>>();

  static final Map<Class<?>, List<FieldMeta>> fields =
      new LinkedHashMap<Class<?>, List<FieldMeta>>();

  static {
    Class<?> clazz;

    clazz = test.Test.Model.class;
    tables.put(clazz, "entry_y2lk");
    types.put("cid", clazz);
    fields.put(clazz, Arrays.asList(
        new FieldMeta("fText", "fText", "STRING", null, "java.lang.String"),
        new FieldMeta("fBoolean", "fBoolean", "INT", null, "java.lang.Boolean"),
        new FieldMeta("fInteger", "fInteger", "INT", null, "java.lang.Integer"),
        new FieldMeta("fDouble", "fDouble", "DOUBLE", null, "java.lang.Double"),
        new FieldMeta("fMap", "fMap", "BLOB", null, "java.util.Map"),
        new FieldMeta("fLinkedModel", "fLinkedModel", null, "Entry", "test.Test.Model"),
        new FieldMeta("fLinkedAsset", "fLinkedAsset", null, "Asset", "com.contentful.sqlite.Asset")));
  }

  public static synchronized Test$Db$$Space get(Context context) {
    if (instance == null) {
      instance = new Test$Db$$Space(context);
    }
    return instance;
  }

  private Test$Db$$Space(Context context) {
    super(context, "space_c2lk", null, 1);
  }

  @Override public void onCreate(SQLiteDatabase db) {
    db.beginTransaction();

    try {
      for (String sql : DEFAULT_CREATE) {
        db.execSQL(sql);
      }

      db.execSQL("CREATE TABLE `entry_y2lk` ("
          + TextUtils.join(",", RESOURCE_COLUMNS) + ","
          + "`fText` STRING,"
          + "`fBoolean` INT,"
          + "`fInteger` INT,"
          + "`fDouble` DOUBLE,"
          + "`fMap` BLOB"
          + ");");

      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
    }
  }

  @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
  }

  @Override public Set<Class<?>> getModels() {
    return models;
  }

  @Override public Map<Class<?>, String> getTablesMap() {
    return tables;
  }

  @Override public Map<String, Class<?>> getTypesMap() {
    return types;
  }

  @Override public Map<Class<?>, List<FieldMeta>> getFieldsMap() {
    return fields;
  }
}