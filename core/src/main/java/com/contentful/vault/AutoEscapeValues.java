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

import android.content.ContentValues;
import java.util.Map;
import java.util.Set;

import static com.contentful.vault.Sql.escape;

final class AutoEscapeValues {
  private final ContentValues values = new ContentValues();

  public void put(String key, String value) {
    values.put(escape(key), value);
  }

  public void put(String key, Byte value) {
    values.put(escape(key), value);
  }

  public void put(String key, Short value) {
    values.put(escape(key), value);
  }

  public void put(String key, Integer value) {
    values.put(escape(key), value);
  }

  public void put(String key, Long value) {
    values.put(escape(key), value);
  }

  public void put(String key, Float value) {
    values.put(escape(key), value);
  }

  public void put(String key, Double value) {
    values.put(escape(key), value);
  }

  public void put(String key, Boolean value) {
    values.put(escape(key), value);
  }

  public void put(String key, byte[] value) {
    values.put(escape(key), value);
  }

  public void putNull(String key) {
    values.putNull(escape(key));
  }

  public int size() {
    return values.size();
  }

  public void remove(String key) {
    values.remove(escape(key));
  }

  public void clear() {
    values.clear();
  }

  public boolean containsKey(String key) {
    return values.containsKey(escape(key));
  }

  public Object get(String key) {
    return values.get(escape(key));
  }

  public String getAsString(String key) {
    return values.getAsString(escape(key));
  }

  public Long getAsLong(String key) {
    return values.getAsLong(escape(key));
  }

  public Integer getAsInteger(String key) {
    return values.getAsInteger(escape(key));
  }

  public Short getAsShort(String key) {
    return values.getAsShort(escape(key));
  }

  public Byte getAsByte(String key) {
    return values.getAsByte(escape(key));
  }

  public Double getAsDouble(String key) {
    return values.getAsDouble(escape(key));
  }

  public Float getAsFloat(String key) {
    return values.getAsFloat(escape(key));
  }

  public Boolean getAsBoolean(String key) {
    return values.getAsBoolean(escape(key));
  }

  public byte[] getAsByteArray(String key) {
    return values.getAsByteArray(escape(key));
  }

  public Set<Map.Entry<String, Object>> valueSet() {
    return values.valueSet();
  }

  public Set<String> keySet() {
    return values.keySet();
  }

  public ContentValues get() {
    return values;
  }
}
