package com.contentful.sqlite;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DbHelper {
  String TABLE_ASSETS = "assets";
  String TABLE_LINKS = "links";

  String CREATE_ASSETS = "CREATE TABLE `"
      + TABLE_ASSETS
      + "` ("
      + "`_ID` INTEGER PRIMARY KEY AUTOINCREMENT,"
      + "`remote_id` STRING NOT NULL UNIQUE,"
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
