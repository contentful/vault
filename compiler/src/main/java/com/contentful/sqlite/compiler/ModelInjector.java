package com.contentful.sqlite.compiler;

import com.contentful.sqlite.PersistenceHelper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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

  List<String> getCreateStatements() {
    List<String> statements = new ArrayList<String>();
    StringBuilder builder = new StringBuilder();
    builder.append("CREATE TABLE `")
        .append(tableName)
        .append("` (");

    for (String column : PersistenceHelper.RESOURCE_COLUMNS) {
      builder.append(column);
      builder.append(", ");
    }

    Set<ModelMember> filtered = getNonLinkMembers();
    ModelMember[] list = filtered.toArray(new ModelMember[filtered.size()]);
    for (int i = 0; i < list.length; i++) {
      ModelMember member = list[i];
      builder.append("`")
          .append(member.fieldName)
          .append("` ")
          .append(member.sqliteType);

      if (i < list.length - 1) {
        builder.append(", ");
      }
    }
    builder.append(");");
    statements.add(builder.toString());
    return statements;
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
