package com.contentful.vault;

abstract class AbsQuery<T extends Resource, E extends AbsQuery<T, E>> {
  private final Class<T> type;

  private final Vault vault;

  private Params params = new Params();

  AbsQuery(Class<T> type, Vault vault) {
    this.type = type;
    this.vault = vault;
  }

  @SuppressWarnings("unchecked")
  public E where(String selection, String... args) {
    params.setSelection(selection);
    params.setSelectionArgs(args);
    return (E) this;
  }

  @SuppressWarnings("unchecked")
  public E limit(Integer limit) {
    String value = null;
    if (limit != null) {
      value = limit.toString();
    }
    params.setLimit(value);
    return (E) this;
  }

  @SuppressWarnings("unchecked")
  public E order(String... order) {
    params.setOrder(order);
    return (E) this;
  }

  Class<T> type() {
    return type;
  }

  Vault vault() {
    return vault;
  }

  Params params() {
    return params;
  }

  void setParams(Params params) {
    this.params = params;
  }

  static final class Params {
    private String selection;
    private String[] selectionArgs;
    private String limit;
    private String[] order;

    public String selection() {
      return selection;
    }

    public void setSelection(String selection) {
      this.selection = selection;
    }

    public String[] selectionArgs() {
      return selectionArgs;
    }

    public void setSelectionArgs(String[] selectionArgs) {
      this.selectionArgs = selectionArgs;
    }

    public String limit() {
      return limit;
    }

    public void setLimit(String limit) {
      this.limit = limit;
    }

    public String[] order() {
      return order;
    }

    public void setOrder(String[] order) {
      this.order = order;
    }
  }
}
