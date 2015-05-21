package com.contentful.vault;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Query<T extends Resource> {
  private final Vault vault;

  private final SqliteHelper sqliteHelper;

  private final String tableName;

  private final Class<T> clazz;

  private final List<FieldMeta> fields;

  private final Map<String, Resource> assets = new LinkedHashMap<String, Resource>();

  private final Map<String, Resource> entries = new LinkedHashMap<String, Resource>();

  private String whereClause;

  private String[] whereArgs;

  private Integer limit;

  private String[] order;

  private String[] queryArgs;

  public Query(Vault vault, SqliteHelper sqliteHelper, Class<T> clazz, String tableName,
      List<FieldMeta> fields) {
    this.vault = vault;
    this.sqliteHelper = sqliteHelper;
    this.clazz = clazz;
    this.tableName = tableName;
    this.fields = fields;
  }

  public Query<T> where(String expression, String... args) {
    this.whereClause = expression;
    this.whereArgs = args;
    return this;
  }

  public Query<T> limit(Integer limit) {
    this.limit = limit;
    return this;
  }

  public Query<T> order(String... order) {
    this.order = order;
    return this;
  }

  public List<T> all() {
    ArrayList<T> result = new ArrayList<T>();
    SQLiteDatabase db = sqliteHelper.getReadableDatabase();
    Cursor cursor = db.rawQuery(queryBuilder().toString(), queryArgs);
    try {
      if (cursor.moveToFirst()) {
        do {
          T resource = sqliteHelper.fromCursor(clazz, cursor);
          if (resource != null) {
            Map<String, Resource> map;
            if (SpaceHelper.TABLE_ASSETS.equals(tableName)) {
              map = assets;
            } else {
              map = entries;
            }
            map.put(resource.remoteId(), resource);
            result.add(resource);
          }
        } while (cursor.moveToNext());
      }
    } finally {
      cursor.close();
    }
    if (fields != null) {
      resolveLinks(result);
    }
    return result;
  }

  public T first() {
    return first(fields != null);
  }

  T first(boolean resolveLinks) {
    limit(1);
    T result = null;
    SQLiteDatabase db = sqliteHelper.getReadableDatabase();
    Cursor cursor = db.rawQuery(queryBuilder().toString(), queryArgs);
    try {
      if (cursor.moveToFirst()) {
        result = sqliteHelper.fromCursor(clazz, cursor);
      }
    } finally {
      cursor.close();
    }
    if (result != null) {
      boolean isAsset = SpaceHelper.TABLE_ASSETS.equals(tableName);
      Map<String, Resource> map = isAsset ? assets : entries;
      map.put(result.remoteId(), result);
      if (resolveLinks) {
        resolveLinks(result, createLinkResolver());
      }
    }
    return result;
  }

  Resource fetchResource(Link link) {
    Resource resource = null;
    Class<?> clazz;
    if (link.isAsset()) {
      clazz = Asset.class;
    } else {
      clazz = sqliteHelper.getSpaceHelper().getTypes().get(link.childContentType());
    }
    if (clazz != null) {
      //noinspection unchecked
      resource = vault.fetch((Class<? extends Resource>) clazz)
          .where("remote_id = ?", link.child())
          .first(false);
    }
    return resource;
  }

  private StringBuilder queryBuilder() {
    List<String> argsList = new ArrayList<String>();
    StringBuilder builder = new StringBuilder();
    builder.append("SELECT * FROM ")
        .append(tableName);

    if (whereClause != null) {
      builder.append(" WHERE ")
          .append(whereClause);
      argsList.addAll(Arrays.asList(whereArgs));
    }
    if (order != null && order.length > 0) {
      builder.append(" ORDER BY ");
      for (int i = 0; i < order.length; i++) {
        if (i > 0) {
          builder.append(", ");
        }
        builder.append(order[i]);
      }
    }
    if (limit != null) {
      builder.append(" LIMIT ")
          .append(limit.toString());
    }
    queryArgs = argsList.toArray(new String[argsList.size()]);
    return builder;
  }

  private void resolveLinks(List<T> resources) {
    boolean hasLinks = false;
    for (FieldMeta field : fields) {
      if (field.isLink() || (field.isArray() && !field.isArrayOfSymbols())) {
        hasLinks = true;
        break;
      }
    }

    if (hasLinks) {
      LinkResolver resolver = createLinkResolver();
      for (T resource : resources) {
        resolveLinks(resource, resolver);
      }
    }
  }

  private void resolveLinks(T resource, LinkResolver resolver) {
    resolver.resolveLinks(resource, fields);
  }

  private LinkResolver createLinkResolver() {
    return new LinkResolver(sqliteHelper, this);
  }

  Map<String, Resource> getAssetsCache() {
    return assets;
  }

  Map<String, Resource> getEntriesCache() {
    return entries;
  }
}
