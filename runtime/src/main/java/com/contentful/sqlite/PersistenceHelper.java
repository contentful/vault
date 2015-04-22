package com.contentful.sqlite;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

public interface PersistenceHelper {
  String TABLE_ASSETS = "assets";
  String TABLE_ENTRY_TYPES = "entry_types";
  String TABLE_LINKS = "links";

  String[] RESOURCE_COLUMNS = new String[] {
      "`remote_id` STRING NOT NULL UNIQUE",
      "`created_at` STRING NOT NULL",
      "`updated_at` STRING"
  };

  // Static resources column indexes
  int COLUMN_REMOTE_ID = 0;
  int COLUMN_CREATED_AT = 1;
  int COLUMN_UPDATED_AT = 2;

  // Static assets column indexes
  int COLUMN_ASSET_URL = RESOURCE_COLUMNS.length;
  int COLUMN_ASSET_MIME_TYPE = RESOURCE_COLUMNS.length + 1;

  String CREATE_ASSETS = "CREATE TABLE `"
      + TABLE_ASSETS
      + "` ("
      + StringUtils.join(RESOURCE_COLUMNS, ", ") + ","
      + "`url` STRING NOT NULL,"
      + "`mime_type` STRING NOT NULL"
      + ");";

  String CREATE_ENTRY_TYPES = "CREATE TABLE `"
      + TABLE_ENTRY_TYPES
      + "` ("
      + "`remote_id` STRING NOT NULL,"
      + "`type_id` STRING NOT NULL,"
      + "UNIQUE(`remote_id`)"
      + ");";

  String CREATE_LINKS = "CREATE TABLE `"
      + TABLE_LINKS
      + "` ("
      + "`parent` STRING NOT NULL,"
      + "`child` STRING NOT NULL,"
      + "`field` STRING NOT NULL,"
      + "`child_content_type` STRING,"
      + "UNIQUE (`parent`, `child`, `field`)"
      + ");";

  List<String> DEFAULT_CREATE = Arrays.asList(CREATE_ASSETS, CREATE_ENTRY_TYPES, CREATE_LINKS);

  Set<Class<?>> getModels();

  Map<Class<?>, String> getTables();

  Map<String, Class<?>> getTypes();

  Map<Class<?>, List<FieldMeta>> getFields();
}