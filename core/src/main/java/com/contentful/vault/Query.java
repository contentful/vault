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
