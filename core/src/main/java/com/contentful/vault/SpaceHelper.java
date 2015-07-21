/*
 * Copyright (C) 2015 Contentful GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.contentful.vault;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

import static com.contentful.vault.BaseFields.CREATED_AT;
import static com.contentful.vault.BaseFields.REMOTE_ID;
import static com.contentful.vault.BaseFields.UPDATED_AT;
import static com.contentful.vault.Utils.declareField;
import static com.contentful.vault.Utils.escape;

public abstract class SpaceHelper {
  public static final String[] RESOURCE_COLUMNS = new String[] {
      declareField(REMOTE_ID, "STRING", false, " UNIQUE"),
      declareField(CREATED_AT, "STRING", false, null),
      declareField(UPDATED_AT, "STRING", true, null),
  };

  static final String TABLE_ASSETS = "assets";

  static final String TABLE_ENTRY_TYPES = "entry_types";

  static final String TABLE_LINKS = "links";

  static final String TABLE_SYNC_INFO = "sync_info";

  // Static resources column indexes
  static final int IDX_REMOTE_ID = 0;

  static final int IDX_CREATED_AT = 1;

  static final int IDX_UPDATED_AT = 2;

  static final List<String> RESOURCE_COLUMN_INDEXES = Arrays.asList(
      REMOTE_ID,
      CREATED_AT,
      UPDATED_AT);

  // Static assets column indexes
  static final List<String> ASSET_COLUMN_INDEXES = Arrays.asList(
      Asset.Fields.URL,
      Asset.Fields.MIME_TYPE,
      Asset.Fields.TITLE,
      Asset.Fields.DESCRIPTION,
      Asset.Fields.FILE);

  static final String CREATE_ASSETS = "CREATE TABLE "
      + escape(TABLE_ASSETS) + " ("
      + StringUtils.join(RESOURCE_COLUMNS, ", ") + ","
      + declareField(Asset.Fields.URL, "STRING", false, ", ")
      + declareField(Asset.Fields.MIME_TYPE, "STRING", false, ", ")
      + declareField(Asset.Fields.TITLE, "STRING", true, ", ")
      + declareField(Asset.Fields.DESCRIPTION, "STRING", true, ", ")
      + declareField(Asset.Fields.FILE, "BLOB", true, null)
      + ");";

  static final String CREATE_ENTRY_TYPES = "CREATE TABLE "
      + escape(TABLE_ENTRY_TYPES) + " ("
      + declareField(REMOTE_ID, "STRING", false, ", ")
      + declareField("type_id", "STRING", false, ", ")
      + "UNIQUE(" + escape(REMOTE_ID) + ")"
      + ");";

  static final String CREATE_LINKS = "CREATE TABLE "
      + escape(TABLE_LINKS)+ " ("
      + declareField("parent", "STRING", false, ", ")
      + declareField("child", "STRING", false, ", ")
      + declareField("field", "STRING", false, ", ")
      + declareField("is_asset", "INT", false, ", ")
      + "UNIQUE ("
      + escape("parent") + ", "
      + escape("child") + ", "
      + escape("field") + ", "
      + escape("is_asset") + ")"
      + ");";

  static final String CREATE_SYNC_INFO = "CREATE TABLE "
      + escape(TABLE_SYNC_INFO) + " ("
      + declareField("token", "STRING", false, ", ")
      + declareField("locale", "STRING", true, ", ")
      + declareField("last_sync_ts", "TIMESTAMP", false, " DEFAULT CURRENT_TIMESTAMP")
      + ");";

  public static final List<String> DEFAULT_CREATE =
      Arrays.asList(CREATE_ASSETS, CREATE_ENTRY_TYPES, CREATE_LINKS, CREATE_SYNC_INFO);

  public static final List<String> DEFAULT_TABLES =
      Arrays.asList(TABLE_ASSETS, TABLE_ENTRY_TYPES, TABLE_LINKS, TABLE_SYNC_INFO);

  public abstract String getDatabaseName();

  public abstract int getDatabaseVersion();

  public abstract Map<Class<?>, ModelHelper<?>> getModels();

  public abstract Map<String, Class<? extends Resource>> getTypes();

  public abstract String getCopyPath();

  static int resourceColumnIndex(String name) {
    int i = RESOURCE_COLUMN_INDEXES.indexOf(name);
    if (i == -1) {
      throw new IllegalArgumentException("Invalid resource column name '" + name + '.');
    }
    return i;
  }

  static int assetColumnIndex(String name) {
    int i = ASSET_COLUMN_INDEXES.indexOf(name);
    if (i == -1) {
      throw new IllegalArgumentException("Invalid asset column name '" + name + '.');
    }
    return RESOURCE_COLUMNS.length + i;
  }
}
