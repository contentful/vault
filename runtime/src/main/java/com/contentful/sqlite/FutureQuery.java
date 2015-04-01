package com.contentful.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.contentful.java.cda.Constants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
        do {
          T resource = ResourceFactory.fromCursor(cursor, clazz, fields);
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

  private T first(boolean resolveLinks) {
    limit(1);
    Cursor cursor = db.rawQuery(queryBuilder().toString(), queryArgs);
    T result = null;
    try {
      if (cursor.moveToFirst()) {
        result = ResourceFactory.fromCursor(cursor, clazz, fields);
      }
    } finally {
      cursor.close();
    }
    if (result != null) {
      boolean isAsset = DbHelper.TABLE_ASSETS.equals(tableName);
      Map<String, Resource> map = isAsset ? assets : entries;
      map.put(result.remoteId, result);
      if (resolveLinks) {
        resolveLinks(Collections.singletonList(result));
      }
    }
    return result;
  }

  private void resolveLinks(List<? extends Resource> list) {
    if (fields != null) {
      for (FieldMeta field : fields) {
        if (field.isLink()) {
          for (Resource t : list) {
            resolveLink(t, field);
          }
        }
      }
    }
  }

  private void resolveLink(Resource t, FieldMeta field) {
    LinkInfo linkInfo = fetchLinkInfo(t.getRemoteId(), field.name, field.linkType);
    if (linkInfo != null) {
      boolean isAsset = DbHelper.TABLE_ASSETS.equals(linkInfo.childContentType);
      Map<String, Resource> map = isAsset ? assets : entries;
      Resource resource = map.get(linkInfo.child);
      if (resource == null) {
        Class<?> clazz = helper.getTypesMap().get(linkInfo.childContentType);
        if (clazz != null) {
          //noinspection unchecked
          resource = Persistence.fetch(helper, (Class<? extends Resource>) clazz)
              .where("remote_id = ?", linkInfo.child)
              .first(false);

          if (resource != null) {
            map.put(resource.getRemoteId(), resource);
            resolveLinks(Collections.singletonList(resource));
          }
        }
      }
      if (resource != null) {
        setFieldValue(t, field, resource);
      }
    }
  }

  private void setFieldValue(Resource t, FieldMeta field, Object value) {
    try {
      java.lang.reflect.Field f = t.getClass().getDeclaredField(field.name);
      f.setAccessible(true);
      f.set(t, value);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private LinkInfo fetchLinkInfo(String parent, String field, String type) {
    StringBuilder builder = new StringBuilder()
        .append("SELECT `child`, `child_content_type` FROM ")
        .append(DbHelper.TABLE_LINKS)
        .append(" WHERE parent = ? AND field = ? AND `child_content_type` IS ");

    if (!Constants.CDAResourceType.Asset.toString().equals(type)) {
      builder.append("NOT ");
    }
    builder.append("NULL;");

    String[] args = new String[]{
        parent,
        field
    };

    Cursor cursor = db.rawQuery(builder.toString(), args);
    LinkInfo result = null;
    try {
      if (cursor.moveToFirst()) {
        String child = cursor.getString(0);
        String childContentType = cursor.getString(1);
        result = new LinkInfo(parent, child, field, childContentType);
      }
    } finally {
      cursor.close();
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
}
