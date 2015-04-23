package com.contentful.sqlite.compiler;

final class ModelMember {
  /** Remote field ID */
  final String id;

  /** Field name in model class */
  final String fieldName;

  /** Type of field in model class */
  final String className;

  /** SQLite column type */
  final String sqliteType;

  /** Link type - {@code Asset}, {@code Entry} or {@code null} */
  final String linkType;

  private ModelMember(Builder builder) {
    this.id = builder.id;
    this.fieldName = builder.fieldName;
    this.className = builder.className;
    this.sqliteType = builder.sqliteType;
    this.linkType = builder.linkType;
  }

  boolean isLink() {
    return this.linkType != null;
  }

  static Builder builder() {
    return new Builder();
  }

  final static class Builder {
    String id;
    String fieldName;
    String className;
    String sqliteType;
    String linkType;

    private Builder() {
    }

    public Builder setId(String id) {
      this.id = id;
      return this;
    }

    public Builder setFieldName(String fieldName) {
      this.fieldName = fieldName;
      return this;
    }

    public Builder setClassName(String className) {
      this.className = className;
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

    public ModelMember build() {
      return new ModelMember(this);
    }
  }
}
