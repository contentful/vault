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
        .append("import com.contentful.sqlite.DbHelper;\n")
        .append("import android.content.Context;\n\n");

    // Emit: class
    builder.append("public class ")
        .append(className)
        .append(" extends SQLiteOpenHelper {\n");

    // Emit: fields
    builder.append("  final DbHelper dbHelper;\n\n");

    // Emit: constructor
    builder.append("  public ")
        .append(className)
        .append("(Context context, DbHelper dbHelper) {\n")
        .append("    super(context, null, null, 1);\n")
        .append("    this.dbHelper = dbHelper;\n")
        .append("  }\n\n");

    // Emit: onCreate
    builder.append("  @Override public void onCreate(SQLiteDatabase db) {\n");

    builder.append("    db.beginTransaction();\n")
        .append("    try {\n");

    // Emit: CREATE default statements
    builder.append("      for (String sql : dbHelper.getDefaultCreateStatements()) {\n")
        .append("        db.execSQL(sql);\n")
        .append("      }\n\n");

    // Emit: CREATE model tables
    for (ModelInjection model : models) {
      List<String> createStatements = model.getCreateStatements("      ");
      for (int i = 0; i < createStatements.size(); i++) {
        builder.append("      db.execSQL(")
            .append(createStatements.get(i))
            .append(");\n\n");
      }
    }

    builder.append("      db.setTransactionSuccessful();\n")
        .append("    } finally {\n")
        .append("      db.endTransaction();\n")
        .append("    }\n")
        .append("  }\n\n");

    // Emit: onUpgrade
    builder.append(
        "  @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {\n")
        .append("  }\n");

    builder.append("}");
    return builder.toString();
  }
}
