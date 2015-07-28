package com.contentful.vault;

class AbsQuery<T extends Resource, E extends AbsQuery<T, E>> {
  protected final Class<T> type;

  protected final Vault vault;

  protected String selection;

  protected String[] selectionArgs;

  protected String limit;

  protected String[] order;

  public AbsQuery(Class<T> type, Vault vault) {
    this.type = type;
    this.vault = vault;
  }

  @SuppressWarnings("unchecked")
  public E where(String selection, String... args) {
    this.selection = selection;
    this.selectionArgs = args;
    return (E) this;
  }

  @SuppressWarnings("unchecked")
  public E limit(Integer limit) {
    String value = null;
    if (limit != null) {
      value = limit.toString();
    }
    this.limit = value;
    return (E) this;
  }

  @SuppressWarnings("unchecked")
  public E order(String... order) {
    this.order = order;
    return (E) this;
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
