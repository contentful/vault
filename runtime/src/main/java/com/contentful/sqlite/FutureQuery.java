package com.contentful.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FutureQuery<T extends Resource> {
  private final String tableName;

  private final Class<T> clazz;

  private final DbHelper helper;

  private final SQLiteDatabase db;

  private final List<FieldMeta> fields;

  private final Map<String, Resource> assets = new LinkedHashMap<String, Resource>();

  private final Map<String, Resource> entries = new LinkedHashMap<String, Resource>();

  private String whereClause;

  private String[] whereArgs;

  private Integer limit;

  private String[] order;

  private String[] queryArgs;

  public FutureQuery(DbHelper helper, Class<T> clazz, String tableName, List<FieldMeta> fields) {
    this.helper = helper;
    this.db = ((SQLiteOpenHelper) helper).getReadableDatabase();
    this.clazz = clazz;
    this.tableName = tableName;
    this.fields = fields;
  }

  public FutureQuery<T> where(String expression, String... args) {
    this.whereClause = expression; // TODO replace field names
    this.whereArgs = args;
    return this;
  }

  public FutureQuery<T> limit(Integer limit) {
    this.limit = limit;
    return this;
  }

  public FutureQuery<T> order(String... order) {
    this.order = order;
    return this;
  }

  public List<T> all() {
    Cursor cursor = db.rawQuery(queryBuilder().toString(), queryArgs);
    ArrayList<T> result = new ArrayList<T>();
    try {
      if (cursor.moveToFirst()) {
        List<FieldMeta> filteredFields = fields == null ? null : getNonLinkFields();
        do {
          T resource = ResourceFactory.fromCursor(cursor, clazz, filteredFields);
          Map<String, Resource> map;
          if (DbHelper.TABLE_ASSETS.equals(tableName)) {
            map = assets;
          } else {
            map = entries;
          }
          map.put(resource.getRemoteId(), resource);
          result.add(resource);
        } while (cursor.moveToNext());
      }
    } finally {
      cursor.close();
    }
    resolveLinks(result);
    return result;
  }

  public T first() {
    return first(true);
  }

  T first(boolean resolveLinks) {
    limit(1);
    Cursor cursor = db.rawQuery(queryBuilder().toString(), queryArgs);
    T result = null;
    try {
      if (cursor.moveToFirst()) {
        result = ResourceFactory.fromCursor(cursor, clazz,
            fields == null ? null : getNonLinkFields());
      }
    } finally {
      cursor.close();
    }
    if (result != null) {
      boolean isAsset = DbHelper.TABLE_ASSETS.equals(tableName);
      Map<String, Resource> map = isAsset ? assets : entries;
      map.put(result.remoteId, result);
      if (resolveLinks) {
        resolveLinks(result);
      }
    }
    return result;
  }

  Resource fetchResource(LinkInfo linkInfo) {
    Resource resource = null;
    Class<?> clazz;
    if (linkInfo.childContentType == null) {
      clazz = Asset.class;
    } else {
      clazz = helper.getTypesMap().get(linkInfo.childContentType);
    }
    if (clazz != null) {
      //noinspection unchecked
      resource = Persistence.fetch(helper, (Class<? extends Resource>) clazz)
          .where("remote_id = ?", linkInfo.child)
          .first(false);
    }
    return resource;
  }

  private List<FieldMeta> getNonLinkFields() {
    ArrayList<FieldMeta> result = new ArrayList<FieldMeta>();
    for (FieldMeta field : fields) {
      if (!field.isLink()) {
        result.add(field);
      }
    }
    return result;
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
    // Skip for assets
    if (fields == null) {
      return;
    }

    List<FieldMeta> links = new ArrayList<FieldMeta>();
    for (FieldMeta field : fields) {
      if (field.isLink()) {
        links.add(field);
      }
    }

    if (links.size() > 0) {
      QueryLinkResolver resolver = new QueryLinkResolver(helper, this);
      for (T resource : resources) {
        resolver.resolveLinks(resource, fields);
      }
    }
  }

  private void resolveLinks(T resource) {
    // Skip for assets
    if (fields == null) {
      return;
    }
    new QueryLinkResolver(helper, this).resolveLinks(resource, fields);
  }

  Map<String, Resource> getAssetsCache() {
    return assets;
  }

  Map<String, Resource> getEntriesCache() {
    return entries;
  }
}
