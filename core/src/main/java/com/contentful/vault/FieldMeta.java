package com.contentful.vault;

import javax.lang.model.type.TypeMirror;

public final class FieldMeta {
  private final String id;

  private final String name;

  private final TypeMirror type;

  private final String sqliteType;

  private final String linkType;

  private final String arrayType;

  private FieldMeta(Builder builder) {
    this.id = builder.id;
    this.name = builder.name;
    this.type = builder.type;
    this.sqliteType = builder.sqliteType;
    this.linkType = builder.linkType;
    this.arrayType = builder.arrayType;
  }

  public String id() {
    return id;
  }

  public String name() {
    return name;
  }

  public TypeMirror type() {
    return type;
  }

  public String sqliteType() {
    return sqliteType;
  }

  public String linkType() {
    return linkType;
  }

  public String arrayType() {
    return arrayType;
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

  public boolean isArrayOfLinks() {
    return isArray() && !isArrayOfSymbols();
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
    private String id;

    private String name;

    private TypeMirror type;

    private String sqliteType;

    private String linkType;

    private String arrayType;

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

    public Builder setType(TypeMirror type) {
      this.type = type;
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
