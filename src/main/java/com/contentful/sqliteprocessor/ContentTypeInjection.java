package com.contentful.sqliteprocessor;

import java.util.LinkedHashMap;
import java.util.Map;

final class ContentTypeInjection {
  private final String classPackage;
  private final String className;
  private final String targetClass;
  private final Map<String, Class> fields;

  public ContentTypeInjection(String classPackage, String className, String targetClass) {
    this.classPackage = classPackage;
    this.className = className;
    this.targetClass = targetClass;
    this.fields = new LinkedHashMap<String, Class>();
  }

  public void addField(String id, Class clazz) {
    fields.put(id, clazz);
  }
}
