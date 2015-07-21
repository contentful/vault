package com.contentful.vault;

import android.content.ContentValues;
import java.util.Map;
import java.util.Set;

import static com.contentful.vault.Utils.escape;

final class AutoEscapeValues {
  ContentValues values = new ContentValues();

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
    return values.get(key);
  }

  public String getAsString(String key) {
    return values.getAsString(key);
  }

  public Long getAsLong(String key) {
    return values.getAsLong(key);
  }

  public Integer getAsInteger(String key) {
    return values.getAsInteger(key);
  }

  public Short getAsShort(String key) {
    return values.getAsShort(key);
  }

  public Byte getAsByte(String key) {
    return values.getAsByte(key);
  }

  public Double getAsDouble(String key) {
    return values.getAsDouble(key);
  }

  public Float getAsFloat(String key) {
    return values.getAsFloat(key);
  }

  public Boolean getAsBoolean(String key) {
    return values.getAsBoolean(key);
  }

  public byte[] getAsByteArray(String key) {
    return values.getAsByteArray(key);
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
