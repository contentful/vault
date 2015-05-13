package com.contentful.vault;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public abstract class SpaceHelper {
  public static final String[] RESOURCE_COLUMNS = new String[] {
      "`remote_id` STRING NOT NULL UNIQUE",
      "`created_at` STRING NOT NULL",
      "`updated_at` STRING"
  };

  static final String TABLE_ASSETS = "assets";
  static final String TABLE_ENTRY_TYPES = "entry_types";
  static final String TABLE_LINKS = "links";
  static final String TABLE_SYNC_INFO = "sync_info";

  // Static resources column indexes
  static final int COLUMN_REMOTE_ID = 0;
  static final int COLUMN_CREATED_AT = 1;
  static final int COLUMN_UPDATED_AT = 2;

  // Static assets column indexes
  static final int COLUMN_ASSET_URL = RESOURCE_COLUMNS.length;
  static final int COLUMN_ASSET_MIME_TYPE = RESOURCE_COLUMNS.length + 1;

  static final String CREATE_ASSETS = "CREATE TABLE `"
      + TABLE_ASSETS
      + "` ("
      + StringUtils.join(RESOURCE_COLUMNS, ", ") + ","
      + "`url` STRING NOT NULL,"
      + "`mime_type` STRING NOT NULL"
      + ");";

  static final String CREATE_ENTRY_TYPES = "CREATE TABLE `"
      + TABLE_ENTRY_TYPES
      + "` ("
      + "`remote_id` STRING NOT NULL,"
      + "`type_id` STRING NOT NULL,"
      + "UNIQUE(`remote_id`)"
      + ");";

  static final String CREATE_LINKS = "CREATE TABLE `"
      + TABLE_LINKS
      + "` ("
      + "`parent` STRING NOT NULL,"
      + "`child` STRING NOT NULL,"
      + "`field` STRING NOT NULL,"
      + "`child_content_type` STRING,"
      + "UNIQUE (`parent`, `child`, `field`)"
      + ");";

  static final String CREATE_SYNC_INFO = "CREATE TABLE `"
      + TABLE_SYNC_INFO
      + "` ("
      + "`token` STRING NOT NULL,"
      + "`locale` STRING,"
      + "`last_sync_ts` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL"
      + ");";

  public static final List<String> DEFAULT_CREATE =
      Arrays.asList(CREATE_ASSETS, CREATE_ENTRY_TYPES, CREATE_LINKS, CREATE_SYNC_INFO);

  public static final List<String> DEFAULT_TABLES =
      Arrays.asList(TABLE_ASSETS, TABLE_ENTRY_TYPES, TABLE_LINKS, TABLE_SYNC_INFO);

  public abstract String getDatabaseName();

  public abstract int getDatabaseVersion();

  public abstract Map<Class<?>, ModelHelper<?>> getModels();

  public abstract Map<String, Class<?>> getTypes();
}
