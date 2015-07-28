package com.contentful.vault;

import java.util.List;

public final class Query<T extends Resource> {
  private final Class<T> type;

  private final Vault vault;

  private String selection;

  private String[] selectionArgs;

  private String limit;

  private String[] order;

  public Query(Class<T> type, Vault vault) {
    this.type = type;
    this.vault = vault;
  }

  public Query<T> where(String selection, String... args) {
    this.selection = selection;
    this.selectionArgs = args;
    return this;
  }

  public Query<T> limit(Integer limit) {
    String value = null;
    if (limit != null) {
      value = limit.toString();
    }
    this.limit = value;
    return this;
  }

  public Query<T> order(String... order) {
    this.order = order;
    return this;
  }

  List<T> all(boolean resolveLinks) {
    return new QueryResolver<T>(this).all(resolveLinks);
  }

  T first(boolean resolveLinks) {
    List<T> all = limit(1).all(resolveLinks);
    if (all.isEmpty()) {
      return null;
    }
    return all.get(0);
  }

  public List<T> all() {
    return all(true);
  }

  public T first() {
    return first(true);
  }

  Class<T> type() {
    return type;
  }

  Vault vault() {
    return vault;
  }

  String selection() {
    return selection;
  }

  String[] selectionArgs() {
    return selectionArgs;
  }

  String limit() {
    return limit;
  }

  String[] order() {
    return order;
  }
}
