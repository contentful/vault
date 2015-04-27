package com.contentful.sqlite.compiler;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

// TODO this should be removed and replaced with FieldMeta
final class ModelMember {
  /** Remote field ID */
  final String id;

  /** Field name in model class */
  final String fieldName;

  /** TypeElement of enclosing field in model class */
  final Element element;

  /** SQLite column type */
  final String sqliteType;

  /** Link type - {@code Asset}, {@code Entry} or {@code null} */
  final String linkType;

  /** Array TypeMirror */
  final TypeMirror arrayType;

  private ModelMember(Builder builder) {
    this.id = builder.id;
    this.fieldName = builder.fieldName;
    this.element = builder.element;
    this.sqliteType = builder.sqliteType;
    this.linkType = builder.linkType;
    this.arrayType = builder.arrayType;
  }

  boolean isLink() {
    return this.linkType != null;
  }

  boolean isArray() { return this.arrayType != null; }

  boolean isArrayOfSymbols() {
    return isArray() && String.class.getName().equals(arrayType.toString());
  }

  static Builder builder() {
    return new Builder();
  }

  final static class Builder {
    String id;
    String fieldName;
    Element element;
    String sqliteType;
    String linkType;
    TypeMirror arrayType;

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

    public Builder setFieldElement(Element element) {
      this.element = element;
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

    public Builder setArrayType(TypeMirror type) {
      this.arrayType = type;
      return this;
    }

    public ModelMember build() {
      return new ModelMember(this);
    }
  }
}
