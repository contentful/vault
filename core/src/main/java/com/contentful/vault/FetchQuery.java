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

public final class FetchQuery<T extends Resource> extends AbsQuery<T, FetchQuery<T>> {
  FetchQuery(Class<T> type, Vault vault) {
    super(type, vault);
  }

  List<T> resolveAll(boolean resolveLinks, String locale) {
    if (locale == null) {
      locale = vault().getSqliteHelper().getSpaceHelper().getDefaultLocale();
    }
    return new QueryResolver<T>(this).all(resolveLinks, locale);
  }

  T resolveFirst(boolean resolveLinks, String locale) {
    if (locale == null) {
      locale = vault().getSqliteHelper().getSpaceHelper().getDefaultLocale();
    }
    List<T> all = limit(1).resolveAll(resolveLinks, locale);
    if (all.isEmpty()) {
      return null;
    }
    return all.get(0);
  }

  public List<T> all() {
    return all(null);
  }

  public List<T> all(String locale) {
    return resolveAll(true, locale);
  }

  public T first() {
    return first(null);
  }

  public T first(String locale) {
    return resolveFirst(true, locale);
  }
}
