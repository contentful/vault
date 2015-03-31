package com.contentful.sqlite.compiler;

import java.util.LinkedHashSet;
import java.util.Set;

final class ModelInjector {
  final String id;
  final String className;
  final String tableName;
  final Set<Member> members;

  public ModelInjector(String id, String className, String tableName, Set<Member> members) {
    this.id = id;
    this.className = className;
    this.tableName = tableName;
    this.members = members;
  }

  void emitCreateStatements(StringBuilder builder, String indent) {
    builder
        .append(indent)
        .append("db.execSQL(")
        .append("\"CREATE TABLE `")
        .append(tableName)
        .append("` (\"\n")
        .append(indent)
        .append("    + \"`_ID` INTEGER PRIMARY KEY AUTOINCREMENT,\"\n")
        .append(indent)
        .append("    + TextUtils.join(\",\", RESOURCE_COLUMNS) + \",\"\n");

    Set<Member> filtered = getNonLinkMembers();
    Member[] list = filtered.toArray(new Member[filtered.size()]);
    for (int i = 0; i < list.length; i++) {
      Member member = list[i];
      builder.append(indent)
          .append("    + \"`")
          .append(member.fieldName)
          .append("` ")
          .append(member.sqliteType);

      if (i < list.length - 1) {
        builder.append(',');
      }
      builder.append("\"\n");
    }
    builder.append(indent)
        .append("    + \");\"")
        .append(");\n");
  }

  Set<Member> getNonLinkMembers() {
    Set<Member> result = new LinkedHashSet<Member>();
    for (Member member : members) {
      if (!member.link) {
        result.add(member);
      }
    }
    return result;
  }

  final static class Member {
    final String id;
    final String fieldName;
    final String className;
    final String sqliteType;
    final boolean link;
    final String enclosedType;

    public Member(String id, String fieldName, String className, String sqliteType, boolean link,
        String enclosedType) {
      this.id = id;
      this.fieldName = fieldName;
      this.className = className;
      this.sqliteType = sqliteType;
      this.link = link;
      this.enclosedType = enclosedType;
    }
  }
}
