package com.contentful.sqlite;

public final class FieldMeta {
  final String id;
  final String name;
  final String sqliteType;
  final String linkType;
  final String arrayType;

  private FieldMeta(Builder builder) {
    this.id = builder.id;
    this.name = builder.name;
    this.sqliteType = builder.sqliteType;
    this.linkType = builder.linkType;
    this.arrayType = builder.arrayType;
  }

  public boolean isLink() {
    return linkType != null;
  }

  public boolean isArray() {
    return arrayType != null;
  }

  public boolean isArrayOfSymbols() {
    return isArray() && String.class.getName().equals(arrayType);
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof FieldMeta)) return false;

    FieldMeta fieldMeta = (FieldMeta) o;

    return id.equals(fieldMeta.id);
  }

  @Override public int hashCode() {
    return id.hashCode();
  }

  public static class Builder {
    String id;
    String name;
    String sqliteType;
    String linkType;
    String arrayType;

    private Builder() {
    }

    public Builder setId(String id) {
      this.id = id;
      return this;
    }

    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    public Builder setSqliteType(String sqliteType) {
      this.sqliteType = sqliteType;
      return this;
    }

    public Builder setLinkType(String linkType) {
      this.linkType = linkType;
      return this;
    }

    public Builder setArrayType(String arrayType) {
      this.arrayType = arrayType;
      return this;
    }

    public FieldMeta build() {
      return new FieldMeta(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
