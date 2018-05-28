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
import org.apache.commons.lang3.StringUtils;

import static com.contentful.vault.BaseFields.CREATED_AT;
import static com.contentful.vault.BaseFields.REMOTE_ID;
import static com.contentful.vault.BaseFields.UPDATED_AT;

public final class Sql {
  private Sql() {
    throw new AssertionError();
  }

  public static final String[] RESOURCE_COLUMNS = new String[] {
      declareField(REMOTE_ID, "STRING", false, " UNIQUE"),
      declareField(CREATED_AT, "STRING", false, null),
      declareField(UPDATED_AT, "STRING", true, null),
  };

  static final String TABLE_ENTRY_TYPES = "entry_types";
  static final String TABLE_SYNC_INFO = "sync_info";
  static final String TABLE_ASSETS = "assets";
  static final String TABLE_LINKS = "links";

  static final String CREATE_ENTRY_TYPES = "CREATE TABLE "
      + escape(TABLE_ENTRY_TYPES) + " ("
      + declareField(REMOTE_ID, "STRING", false, ", ")
      + declareField("type_id", "STRING", false, ", ")
      + "UNIQUE(" + escape(REMOTE_ID) + ")"
      + ");";

  static final String CREATE_SYNC_INFO = "CREATE TABLE "
      + escape(TABLE_SYNC_INFO) + " ("
      + declareField("token", "STRING", false, ", ")
      + declareField("last_sync_ts", "TIMESTAMP", false, " DEFAULT CURRENT_TIMESTAMP")
      + ");";

  static String createLinks(String locale) {
    return "CREATE TABLE " + escape(localizeName(TABLE_LINKS, locale)) + " ("
        + declareField("parent", "STRING", false, ", ")
        + declareField("child", "STRING", false, ", ")
        + declareField("field", "STRING", false, ", ")
        + declareField("is_asset", "INT", false, ", ")
        + declareField("position", "INT", false, ", ")
        + "UNIQUE ("
        + escape("parent") + ", "
        + escape("child") + ", "
        + escape("field") + ", "
        + escape("is_asset") + ", "
        + escape("position") + ")"
        + ");";
  }

  static String createAssets(String locale) {
    return "CREATE TABLE " + escape(localizeName(TABLE_ASSETS, locale)) + " ("
        + StringUtils.join(RESOURCE_COLUMNS, ", ") + ","
        + declareField(Asset.Fields.URL, "STRING", true, ", ")
        + declareField(Asset.Fields.MIME_TYPE, "STRING", true, ", ")
        + declareField(Asset.Fields.TITLE, "STRING", true, ", ")
        + declareField(Asset.Fields.DESCRIPTION, "STRING", true, ", ")
        + declareField(Asset.Fields.FILE, "BLOB", true, null)
        + ");";
  }

  static final List<String> RESOURCE_COLUMN_INDEXES = Arrays.asList(
      REMOTE_ID,
      CREATED_AT,
      UPDATED_AT);

  static final List<String> ASSET_COLUMN_INDEXES = Arrays.asList(
      Asset.Fields.URL,
      Asset.Fields.MIME_TYPE,
      Asset.Fields.TITLE,
      Asset.Fields.DESCRIPTION,
      Asset.Fields.FILE);

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

  static String escape(String name) {
    return String.format("`%s`", name);
  }

  static String declareField(String name, String type, boolean nullable, String suffix) {
    StringBuilder builder = new StringBuilder();
    builder.append(escape(name))
        .append(" ")
        .append(type);
    if (!nullable) {
      builder.append(" ")
          .append("NOT NULL");
    }
    if (suffix != null) {
      builder.append(suffix);
    }
    return builder.toString();
  }

  static String localizeName(String name, String locale) {
    return String.format("%s$%s", name, locale);
  }
}
