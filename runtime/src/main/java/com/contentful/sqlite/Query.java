package com.contentful.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Query<T extends Resource> {
  private final String tableName;

  private final Class<T> clazz;

  private final SQLiteDatabase db;

  private final List<FieldMeta> fields;

  private String whereClause;

  private String[] whereArgs;

  private Integer limit;

  private String[] order;

  private String[] queryArgs;

  public Query(SQLiteDatabase db, Class<T> clazz, String tableName,
      List<FieldMeta> fields) {
    this.db = db;
    this.clazz = clazz;
    this.tableName = tableName;
    this.fields = fields;
  }

  public String getTableName() {
    return tableName;
  }

  public SQLiteDatabase getDb() {
    return db;
  }

  public Query<T> where(String expression, String... args) {
    this.whereClause = expression; // TODO replace field names
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
    Cursor cursor = db.rawQuery(queryBuilder().toString(), queryArgs);
    ArrayList<T> result = new ArrayList<T>();
    try {
      if (cursor.moveToFirst()) {
        do {
          //noinspection unchecked
          result.add((T) ResourceFactory.fromCursor(cursor, clazz, fields));
        } while (cursor.moveToNext());
      }
    } finally {
      cursor.close();
    }

    return result;
  }

  public T first() {
    limit(1);
    Cursor cursor = db.rawQuery(queryBuilder().toString(), queryArgs);
    T result = null;
    try {
      if (cursor.moveToFirst()) {
        //noinspection unchecked
        result = (T) ResourceFactory.fromCursor(cursor, clazz, fields);
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
