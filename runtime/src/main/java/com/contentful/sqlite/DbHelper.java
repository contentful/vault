package com.contentful.sqlite;

import android.text.TextUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DbHelper {
  String TABLE_ASSETS = "assets";
  String TABLE_LINKS = "links";

  // Static resource column indexes
  int COLUMN_REMOTE_ID = 1;
  int COLUMN_CREATED_AT = 2;
  int COLUMN_UPDATED_AT = 3;

  // Static asset column indexes
  int COLUMN_ASSET_URL = 2;
  int COLUMN_ASSET_MIME_TYPE = 3;

  String[] RESOURCE_COLUMNS = new String[]{
      "`remote_id` STRING NOT NULL UNIQUE",
      "`created_at` STRING NOT NULL",
      "`updated_at` STRING"
  };

  String CREATE_ASSETS = "CREATE TABLE `"
      + TABLE_ASSETS
      + "` ("
      + "`_ID` INTEGER PRIMARY KEY AUTOINCREMENT,"
      + TextUtils.join(",", RESOURCE_COLUMNS) + ","
      + "`url` STRING NOT NULL,"
      + "`mime_type` STRING NOT NULL"
      + ");";

  String CREATE_LINKS = "CREATE TABLE `"
      + TABLE_LINKS
      + "` ("
      + "`_ID` INTEGER PRIMARY KEY AUTOINCREMENT,"
      + "`parent` STRING NOT NULL,"
      + "`child` STRING NOT NULL,"
      + "UNIQUE (`parent`, `child`)"
      + ");";

  List<String> DEFAULT_CREATE = Arrays.asList(CREATE_ASSETS, CREATE_LINKS);

  Set<Class<?>> getModels();

  Map<Class<?>, String> getTablesMap();

  Map<String, Class<?>> getTypesMap();

  Map<Class<?>, List<FieldMeta>> getFieldsMap();
}
