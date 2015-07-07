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

package com.contentful.vault.compiler;

import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;

final class SqliteUtils {
  private SqliteUtils() {
    throw new AssertionError();
  }

  static String typeForClass(String className) {
    if (String.class.getName().equals(className)) {
      return "STRING";
    } else if (Boolean.class.getName().equals(className)) {
      return "BOOL";
    } else if (Integer.class.getName().equals(className)) {
      return "INT";
    } else if (Double.class.getName().equals(className)) {
      return "DOUBLE";
    } else if (Map.class.getName().equals(className)) {
      return "BLOB";
    } else if (List.class.getName().equals(className)) {
      return "BLOB";
    }
    return null;
  }

  static String hashForId(String id) {
    return Base64.encodeBase64String(id.getBytes()).replaceAll("=", "").toLowerCase();
  }
}
