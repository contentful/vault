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

import android.database.Cursor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.text.TextUtils.join;
import static com.contentful.vault.SpaceHelper.TABLE_ASSETS;

final class QueryResolver<T extends Resource> {
  private final Query<T> query;

  private final Vault vault;

  private final SqliteHelper sqliteHelper;

  private final Map<String, Resource> assets = new HashMap<String, Resource>();

  private final Map<String, Resource> entries = new HashMap<String, Resource>();

  QueryResolver(Query<T> query) {
    this.query = query;
    this.vault = query.vault();
    this.sqliteHelper = vault.getOrCreateSqliteHelper();
  }

  List<T> all(boolean resolveLinks) {
    Cursor cursor = cursorFromQuery(query);
    List<T> result = new ArrayList<T>();
    try {
      if (cursor.moveToFirst()) {
        Map<String, Resource> cache = cacheForType(query.type());
        do {
          T item = sqliteHelper.fromCursor(query.type(), cursor);
          if (item == null) {
            continue;
          }
          result.add(item);
          cache.put(item.remoteId(), item);
        } while (cursor.moveToNext());
      }
    } finally {
      cursor.close();
    }

    if (resolveLinks && query.type() != Asset.class && !result.isEmpty()) {
      resolveLinks(result);
    }

    return result;
  }

  private Map<String, Resource> cacheForType(Class<T> type) {
    if (type == Asset.class) {
      return assets;
    }
    return entries;
  }

  private void resolveLinks(List<T> resources) {
    LinkResolver resolver = new LinkResolver(query, assets, entries);
    for (T resource : resources) {
      resolver.resolveLinks(resource, helperForEntry(resource).getFields());
    }
  }

  private ModelHelper<?> helperForEntry(T resource) {
    SpaceHelper spaceHelper = sqliteHelper.getSpaceHelper();
    Class<?> modelType = spaceHelper.getTypes().get(resource.contentType());
    return spaceHelper.getModels().get(modelType);
  }

  private static Cursor cursorFromQuery(Query<?> query) {
    String[] orderArray = query.order();
    String order = null;
    if (orderArray != null && orderArray.length > 0) {
      order = join(", ", orderArray);
    }
    String tableName;
    if (query.type() == Asset.class) {
      tableName = TABLE_ASSETS;
    } else {
      tableName = query.vault()
          .getOrCreateSqliteHelper()
          .getSpaceHelper()
          .getModels()
          .get(query.type())
          .getTableName();
    }
    return query.vault().getReadableDatabase().query(tableName,
        null,                       // columns
        query.selection(),          // selection
        query.selectionArgs(),      // selectionArgs
        null,                       // groupBy
        null,                       // having
        order,                      // order
        query.limit());             // limit
  }
}
