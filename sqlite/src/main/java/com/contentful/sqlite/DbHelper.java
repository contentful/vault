package com.contentful.sqlite;

import java.util.Arrays;
import java.util.List;

public abstract class DbHelper {
  public static final String TABLE_ASSETS = "assets";
  public static final String TABLE_LINKS = "links";

  static final String CREATE_ASSETS = "CREATE TABLE `"
      + TABLE_ASSETS
      + "` ("
      + "`_ID` INTEGER PRIMARY KEY AUTOINCREMENT,"
      + "`remote_id` STRING NOT NULL,"
      + "`url` STRING NOT NULL,"
      + "`mime_type` STRING NOT NULL"
      + ");";

  static final String CREATE_LINKS = "CREATE TABLE `"
      + TABLE_LINKS
      + "` ("
      + "`_ID` INTEGER PRIMARY KEY AUTOINCREMENT,"
      + "`parent` STRING NOT NULL,"
      + "`child` STRING NOT NULL"
      + ");";

  public static List<String> getDefaultCreateStatements() {
    return Arrays.asList(CREATE_ASSETS, CREATE_LINKS);
  }
}
