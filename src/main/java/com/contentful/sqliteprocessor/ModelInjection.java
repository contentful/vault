package com.contentful.sqliteprocessor;

import java.util.Set;
import org.apache.commons.codec.binary.Base64;

final class ModelInjection extends Injection {
  final Set<Member> members;

  public ModelInjection(String id, String classPackage, String className, String targetClass,
      Set<Member> members) {
    super(id, classPackage, className, targetClass);
    this.members = members;
  }

  @Override String brewJava() {
    StringBuilder builder = new StringBuilder();

    // Emit: package
    builder.append("// Generated code from sqlite-processor.\n")
        .append("package ").append(classPackage).append(";\n\n");

    // Emit: imports
    builder.append("import ")
        .append(ModelHelper.class.getName())
        .append(";\n\n");

    // Emit: class
    builder.append("public class ")
        .append(className)
        .append(" implements ")
        .append(ModelHelper.class.getSimpleName())
        .append(" {\n")
        .append("}\n");

    return builder.toString();
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
}
