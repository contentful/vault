package com.contentful.sqlite.compiler;

import com.squareup.javapoet.JavaFile;

abstract class Injection {
  final String id;
  final String classPackage;
  final String className;
  final String targetClass;

  public Injection(String id, String classPackage, String className, String targetClass) {
    this.id = id;
    this.classPackage = classPackage;
    this.className = className;
    this.targetClass = targetClass;
  }

  abstract JavaFile brewJava();
}
