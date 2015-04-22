package com.contentful.sqlite.compiler;

import java.util.LinkedHashSet;
import java.util.Set;

final class ModelInjector {
  final String id;
  final String className;
  final String tableName;
  final Set<ModelMember> members;

  public ModelInjector(String id, String className, String tableName, Set<ModelMember> members) {
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
        .append("    + TextUtils.join(\",\", RESOURCE_COLUMNS) + \",\"\n");

    Set<ModelMember> filtered = getNonLinkMembers();
    ModelMember[] list = filtered.toArray(new ModelMember[filtered.size()]);
    for (int i = 0; i < list.length; i++) {
      ModelMember member = list[i];
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

  Set<ModelMember> getNonLinkMembers() {
    Set<ModelMember> result = new LinkedHashSet<ModelMember>();
    for (ModelMember member : members) {
      if (!member.isLink()) {
        result.add(member);
      }
    }
    return result;
  }
}
