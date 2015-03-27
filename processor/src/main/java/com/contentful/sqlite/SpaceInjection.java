package com.contentful.sqlite;

import java.util.List;

final class SpaceInjection extends Injection {
  private final List<ModelInjection> models;

  public SpaceInjection(String id, String classPackage, String className, String targetClass,
      List<ModelInjection> models) {
    super(id, classPackage, className, targetClass);
    this.models = models;
  }

  @Override String brewJava() {
    StringBuilder builder = new StringBuilder();

    // Emit: package
    builder.append("// Generated code from sqlite-processor.\n")
        .append("package ").append(classPackage).append(";\n\n");

    // Emit: imports
    builder.append("import android.database.sqlite.SQLiteOpenHelper;\n")
        .append("import android.database.sqlite.SQLiteDatabase;\n")
        .append("import android.content.Context;\n\n");

    // Emit: class
    builder.append("public class ")
        .append(className)
        .append(" extends SQLiteOpenHelper {\n");

    // Emit: constructor
    builder.append("  public ")
        .append(className)
        .append("(Context context) {\n")
        .append("    super(context, null, null, 1);\n")
        .append("  }\n\n");

    // Emit: onCreate
    builder.append("  @Override public void onCreate(SQLiteDatabase db) {\n");

    // Emit: create model tables statements
    for (ModelInjection model : models) {
      List<String> createStatements = model.getCreateStatements("    ");
      for (int i = 0; i < createStatements.size(); i++) {
        builder.append("    db.execSQL(")
            .append(createStatements.get(i))
            .append(");\n");

        if (i < createStatements.size() - 1) {
          builder.append("\n");
        }
      }
    }

    builder.append("  }\n\n");

    // Emit: onUpgrade
    builder.append(
        "  @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {\n")
        .append("  }\n");

    builder.append("}");
    return builder.toString();
  }
}
