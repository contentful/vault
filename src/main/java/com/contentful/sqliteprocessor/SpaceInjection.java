package com.contentful.sqliteprocessor;

public class SpaceInjection extends Injection {
  public SpaceInjection(String id, String classPackage, String className, String targetClass) {
    super(id, classPackage, className, targetClass);
  }

  @Override String brewJava() {
    return "";
  }
}
