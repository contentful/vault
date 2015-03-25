package com.contentful.sqliteprocessor;

import java.util.Set;

final class ContentTypeInjection {
  final String id;
  final String classPackage;
  final String className;
  final String targetClass;
  final Set<Member> members;

  public ContentTypeInjection(String id, String classPackage, String className, String targetClass,
      Set<Member> members) {
    this.id = id;
    this.classPackage = classPackage;
    this.className = className;
    this.targetClass = targetClass;
    this.members = members;
  }

  final static class Member {
    final String remoteId;
    final String fieldName;
    final String className;

    public Member(String remoteId, String fieldName, String className) {
      this.remoteId = remoteId;
      this.fieldName = fieldName;
      this.className = className;
    }
  }

  String getFqcn() {
    return classPackage + "." + className;
  }

  String brewJava() {
    StringBuilder builder = new StringBuilder();
    builder.append("// Generated code from sqlite-processor.\n")
        .append("package ").append(classPackage).append(";\n\n")
        .append("public class ").append(className).append(" {\n")
        .append("}\n");
    return builder.toString();
  }
}
