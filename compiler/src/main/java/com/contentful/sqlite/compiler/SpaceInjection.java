package com.contentful.sqlite.compiler;

import com.contentful.sqlite.compiler.ModelInjector.Member;
import java.util.Iterator;
import java.util.List;

final class SpaceInjection extends Injection {
  private final List<ModelInjector> models;
  private final String tableName;

  public SpaceInjection(String id, String classPackage, String className, String targetClass,
      List<ModelInjector> models, String tableName) {
    super(id, classPackage, className, targetClass);
    this.models = models;
    this.tableName = tableName;
  }

  @Override String brewJava() {
    StringBuilder builder = new StringBuilder();

    // Emit: package
    builder.append("// Generated code from sqlite-processor.\n")
        .append("package ")
        .append(classPackage)
        .append(";\n\n");

    // Emit: imports
    builder.append("import android.content.Context;\n")
        .append("import android.database.sqlite.SQLiteDatabase;\n")
        .append("import android.database.sqlite.SQLiteOpenHelper;\n")
        .append("import android.text.TextUtils;\n")
        .append("import com.contentful.sqlite.DbHelper;\n")
        .append("import com.contentful.sqlite.FieldMeta;\n")
        .append("import java.util.Arrays;\n")
        .append("import java.util.LinkedHashMap;\n")
        .append("import java.util.LinkedHashSet;\n")
        .append("import java.util.List;\n")
        .append("import java.util.Map;\n")
        .append("import java.util.Set;\n\n");

    // Emit: class
    builder.append("public class ")
        .append(className)
        .append(" extends SQLiteOpenHelper")
        .append(" implements DbHelper {\n");

    // Emit: fields
    builder.append("  static ")
        .append(className)
        .append(" instance;\n\n");

    builder.append("  static final Set<Class<?>> models =\n")
        .append("      new LinkedHashSet<Class<?>>(Arrays.asList(\n");

    for (int i = 0; i < models.size(); i++) {
      builder.append("          ")
          .append(models.get(i).className)
          .append(".class");

      if (i < models.size() - 1) {
        builder.append(",");
      }
      builder.append("\n");
    }
    builder.append("      ));\n\n");

    builder.append("  static final Map<Class<?>, String> tables =\n")
        .append("      new LinkedHashMap<Class<?>, String>();\n\n");

    builder.append("  static final Map<String, Class<?>> types =\n")
        .append("      new LinkedHashMap<String, Class<?>>();\n\n");

    builder.append("  static final Map<Class<?>, List<FieldMeta>> fields =\n")
        .append("      new LinkedHashMap<Class<?>, List<FieldMeta>>();\n\n");

    // Emit: static initializer
    builder.append("  static {\n")
        .append("    Class<?> clazz;\n\n");

    for (int i = 0; i < models.size(); i++) {
      ModelInjector model = models.get(i);

      // Emit: class
      builder.append("    clazz = ")
          .append(model.className)
          .append(".class;\n");

      // Emit: tables mapping
      builder.append("    tables.put(clazz, ")
          .append("\"")
          .append(model.tableName)
          .append("\"")
          .append(");\n");

      // Emit: types mapping
      builder.append("    types.put(")
          .append("\"")
          .append(model.id)
          .append("\"")
          .append(", clazz);\n");

      // Emit: fields mapping
      builder.append("    fields.put(clazz, Arrays.asList(\n");
      Iterator<Member> it = model.members.iterator();
      while (it.hasNext()) {
        Member member = it.next();
        builder.append("        ")
            .append("new FieldMeta(\"")
            .append(member.id)
            .append("\", \"")
            .append(member.fieldName)
            .append("\", ");

        if (member.isLink()) {
          builder.append("null");
        } else {
          builder.append("\"")
              .append(member.sqliteType)
              .append("\"");
        }

        builder.append(", ");
        if (member.isLink()) {
          builder.append("\"")
              .append(member.linkType)
              .append("\"");
        } else {
          builder.append("null");
        }

        builder.append(", \"")
            .append(member.enclosedType)
            .append("\"")
            .append(")");

        if (it.hasNext()) {
          builder.append(",\n");
        }
      }
      builder.append("));\n");
      if (i < models.size() - 1) {
        builder.append("\n");
      }
    }

    builder.append("  }\n\n");

    // Emit: singleton
    builder.append("  public static synchronized ")
        .append(className)
        .append(" get(Context context) {\n")
        .append("    if (instance == null) {\n")
        .append("      instance = new ")
        .append(className)
        .append("(context);\n")
        .append("    }\n")
        .append("    return instance;\n")
        .append("  }\n\n");


    // Emit: constructor
    builder.append("  private ")
        .append(className)
        .append("(Context context) {\n")
        .append("    super(context, \"")
        .append(tableName)
        .append("\", null, 1);\n")
        .append("  }\n\n");

    // Emit: onCreate
    builder.append("  @Override public void onCreate(SQLiteDatabase db) {\n");

    builder.append("    db.beginTransaction();\n\n")
        .append("    try {\n");

    // Emit: CREATE default statements
    builder.append("      for (String sql : DEFAULT_CREATE) {\n")
        .append("        db.execSQL(sql);\n")
        .append("      }\n\n");

    // Emit: CREATE model tables
    for (int i = 0; i < models.size(); i++) {
      if (i > 0) {
        builder.append("\n");
      }
      ModelInjector modelInjector = models.get(i);
      modelInjector.emitCreateStatements(builder, "      ");
    }
    builder.append("\n");

    builder.append("      db.setTransactionSuccessful();\n")
        .append("    } finally {\n")
        .append("      db.endTransaction();\n")
        .append("    }\n")
        .append("  }\n\n");

    // Emit: onUpgrade
    builder.append(
        "  @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {\n")
        .append("  }\n\n");

    // Emit: getModels
    builder.append("  @Override public Set<Class<?>> getModels() {\n")
        .append("    return models;\n")
        .append("  }\n\n");

    // Emit: getTablesMap
    builder.append("  @Override public Map<Class<?>, String> getTablesMap() {\n")
        .append("    return tables;\n")
        .append("  }\n\n");

    // Emit: getTypesMap
    builder.append("  @Override public Map<String, Class<?>> getTypesMap() {\n")
        .append("    return types;\n")
        .append("  }\n\n");

    // Emit: getFieldsMap
    builder.append("  @Override public Map<Class<?>, List<FieldMeta>> getFieldsMap() {\n")
        .append("    return fields;\n")
        .append("  }\n")
        .append("}");

    return builder.toString();
  }
}
