package com.contentful.sqliteprocessor;

import java.util.ArrayList;
import java.util.List;
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

  List<String> getCreateStatements(String indent) {
    List<String> result = new ArrayList<String>();
    String code = Base64.encodeBase64String(id.getBytes()).replaceAll("=", "").toLowerCase();
    String tableName = String.format("entry_%s", code);
    StringBuilder builder = new StringBuilder();

    // Emit: CREATE
    builder.append("\"CREATE TABLE `")
        .append(tableName)
        .append("` (\"\n")
        .append(indent)
        .append("    + \"`_ID` INTEGER PRIMARY KEY AUTOINCREMENT,\"\n")
        .append(indent)
        .append("    + \"`remote_id` STRING NOT NULL,\"\n");

    Member[] list = members.toArray(new Member[members.size()]);
    for (int i = 0; i < list.length; i++) {
      Member member = list[i];
      builder.append(indent)
          .append("    + \"`")
          .append(member.fieldName)
          .append("` ")
          .append(SqliteUtils.typeForClass(member.className));

      if (i < list.length - 1) {
        builder.append(',');
      }
      builder.append("\"\n");
    }
    builder.append(indent).append("    + \");\"");
    result.add(builder.toString());
    return result;
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
