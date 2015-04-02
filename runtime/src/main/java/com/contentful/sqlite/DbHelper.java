package com.contentful.sqlite;

import android.text.TextUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DbHelper {
  String TABLE_ASSETS = "assets";
  String TABLE_ENTRY_TYPES = "entry_types";
  String TABLE_LINKS = "links";

  // Static resources column indexes
  int COLUMN_REMOTE_ID = 0;
  int COLUMN_CREATED_AT = 1;
  int COLUMN_UPDATED_AT = 2;

  // Static assets column indexes
  int COLUMN_ASSET_URL = 3;
  int COLUMN_ASSET_MIME_TYPE = 4;

  // Static entry types column indexes
  int COLUMN_ET_REMOTE_ID = 3;
  int COLUMN_ET_CONTENT_TYPE = 3;

  // Static links column indexes
  int COLUMN_LINKS_PARENT = 3;
  int COLUMN_LINKS_CHILD = 4;
  int COLUMN_LINKS_FIELD = 5;
  int COLUMN_LINKS_CHILD_CT = 6;

  String[] RESOURCE_COLUMNS = new String[] {
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

  String CREATE_ENTRY_TYPES = "CREATE TABLE `"
      + TABLE_ENTRY_TYPES
      + "` ("
      + "_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
      + "`remote_id` STRING NOT NULL,"
      + "`type_id` STRING NOT NULL,"
      + "UNIQUE(`remote_id`)"
      + ");";

  String CREATE_LINKS = "CREATE TABLE `"
      + TABLE_LINKS
      + "` ("
      + "`_ID` INTEGER PRIMARY KEY AUTOINCREMENT,"
      + "`parent` STRING NOT NULL,"
      + "`child` STRING NOT NULL,"
      + "`field` STRING NOT NULL,"
      + "`child_content_type` STRING,"
      + "UNIQUE (`parent`, `child`, `field`)"
      + ");";

  List<String> DEFAULT_CREATE = Arrays.asList(CREATE_ASSETS, CREATE_ENTRY_TYPES, CREATE_LINKS);

  Set<Class<?>> getModels();

  Map<Class<?>, String> getTablesMap();

  Map<String, Class<?>> getTypesMap();

  Map<Class<?>, List<FieldMeta>> getFieldsMap();
}
