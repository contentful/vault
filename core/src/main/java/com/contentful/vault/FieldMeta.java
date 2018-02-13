/*
 * Copyright (C) 2015 Contentful GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.contentful.vault;

import javax.lang.model.type.TypeMirror;

public final class FieldMeta {
  private final String id;

  private final String name;

  private final TypeMirror type;

  private final String sqliteType;

  private final String linkType;

  private final String arrayType;

  FieldMeta(Builder builder) {
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
    String id;
    String name;
    TypeMirror type;
    String sqliteType;
    String linkType;
    String arrayType;

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
