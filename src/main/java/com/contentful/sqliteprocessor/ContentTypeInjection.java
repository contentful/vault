package com.contentful.sqliteprocessor;

import java.util.Set;
import org.apache.commons.codec.binary.Base64;

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
        .append("public class ").append(className).append(" {\n");

    emitCreateTable(builder);

    builder.append("}\n");
    return builder.toString();
  }

  private void emitCreateTable(StringBuilder builder) {
    String code = Base64.encodeBase64String(id.getBytes()).replaceAll("=", "").toLowerCase();
    String tableName = String.format("entry_%s", code);
    String indent = "  ";

    // Emit: NAME
    builder.append(indent)
        .append("public static final String NAME = \"")
        .append(tableName)
        .append("\";\n\n");

    // Emit: CREATE
    builder.append(indent)
        .append("public static final String CREATE = \"CREATE TABLE `\"")
        .append(" + ")
        .append("NAME")
        .append(" + ")
        .append("\"` (\"\n");

    indent = "    ";
    builder.append(indent).append("  + \"`_ID` INTEGER PRIMARY KEY AUTOINCREMENT,\"\n")
            .append(indent).append("  + \"`remote_id` STRING NOT NULL,\"\n");

    Member[] list = members.toArray(new Member[members.size()]);
    for (int i = 0; i < list.length; i++) {
      Member member = list[i];
      builder.append(indent).append("  + \"`")
          .append(member.fieldName)
          .append("` ")
          .append(SqliteUtils.typeForClass(member.className));

      if (i < list.length - 1) {
        builder.append(',');
      }
      builder.append("\"\n");
    }
    builder.append(indent).append("  + \");\";\n");
  }
}
