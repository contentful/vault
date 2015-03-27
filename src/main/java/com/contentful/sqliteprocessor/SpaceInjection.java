package com.contentful.sqliteprocessor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.List;

public class SpaceInjection extends Injection {
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
    builder.append("import ")
        .append(SQLiteOpenHelper.class.getName())
        .append(";\n");

    builder.append("import ")
        .append(SQLiteDatabase.class.getName())
        .append(";\n");

    builder.append("import ")
        .append(Context.class.getName()).append(";\n");

    builder.append("\n");

    // Emit: class
    builder.append("public class ")
        .append(className)
        .append(" extends ")
        .append(SQLiteOpenHelper.class.getSimpleName()).append(" {\n");

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
      builder.append("    db.rawQuery(");
      model.emitCreateStatement("    ", builder);
      builder.append(", null);\n");
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
