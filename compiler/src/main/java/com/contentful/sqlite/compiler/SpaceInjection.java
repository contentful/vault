package com.contentful.sqlite.compiler;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.contentful.sqlite.FieldMeta;
import com.contentful.sqlite.PersistenceHelper;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Modifier;

final class SpaceInjection extends Injection {
  private final List<ModelInjector> models;
  private final String tableName;
  private FieldSpec specModels;
  private FieldSpec specTables;
  private FieldSpec specTypes;
  private FieldSpec specFields;

  public SpaceInjection(String id, String classPackage, String className, String targetClass,
      List<ModelInjector> models, String tableName) {
    super(id, classPackage, className, targetClass);
    this.models = models;
    this.tableName = tableName;
  }

  @Override JavaFile brewJava() {
    TypeSpec.Builder builder = TypeSpec.classBuilder(className)
        .superclass(SQLiteOpenHelper.class)
        .addSuperinterface(PersistenceHelper.class);

    appendSingleton(builder);
    appendConstructor(builder);
    appendOnCreate(builder);
    appendOnUpgrade(builder);
    appendModels(builder);
    appendTables(builder);
    appendTypes(builder);
    appendFields(builder);
    appendStaticInitializer(builder);

    return JavaFile.builder(classPackage, builder.build())
        .build();
  }

  private void appendStaticInitializer(TypeSpec.Builder builder) {

  }

  private void appendFields(TypeSpec.Builder builder) {
    // Field
    TypeName classTypeName = ParameterizedTypeName.get(
        ClassName.get(Class.class),
        WildcardTypeName.subtypeOf(Object.class));

    TypeName listTypeName = ParameterizedTypeName.get(ClassName.get(List.class),
        ClassName.get(FieldMeta.class));

    specFields = createMapWithInitializer("fields", LinkedHashMap.class, classTypeName,
        listTypeName).build();

    builder.addField(specFields);

    // Getter
    builder.addMethod(createGetterImpl(specFields, "getFields").build());
  }

  private void appendTypes(TypeSpec.Builder builder) {
    // Field
    TypeName classTypeName = ParameterizedTypeName.get(ClassName.get(Class.class),
        WildcardTypeName.subtypeOf(Object.class));

    specTypes =
        createMapWithInitializer("types", LinkedHashMap.class, ClassName.get(String.class),
            classTypeName)
            .addModifiers(Modifier.STATIC, Modifier.FINAL)
            .build();

    builder.addField(specTypes);

    // Getter
    builder.addMethod(createGetterImpl(specTypes, "getTypes").build());
  }

  private void appendTables(TypeSpec.Builder builder) {
    // Field
    TypeName classTypeName = ParameterizedTypeName.get(
        ClassName.get(Class.class),
        WildcardTypeName.subtypeOf(Object.class));

    FieldSpec field = createMapWithInitializer("tables", LinkedHashMap.class, classTypeName,
        ClassName.get(String.class))
        .addModifiers(Modifier.STATIC, Modifier.FINAL)
        .build();

    builder.addField(field);

    // Getter
    builder.addMethod(createGetterImpl(field, "getTables").build());
  }

  private void appendModels(TypeSpec.Builder builder) {
    // Field
    TypeName classTypeName = ParameterizedTypeName.get(
        ClassName.get(Class.class),
        WildcardTypeName.subtypeOf(Object.class));

    FieldSpec field = createSetWithInitializer("models", LinkedHashSet.class, classTypeName)
        .addModifiers(Modifier.STATIC, Modifier.FINAL)
        .build();

    builder.addField(field);

    // Getter
    builder.addMethod(createGetterImpl(field, "getModels").build());
  }

  private MethodSpec.Builder createGetterImpl(FieldSpec field, String name) {
    return MethodSpec.methodBuilder(name)
        .returns(field.type)
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addStatement("return $N", field.name);
  }

  private void appendOnUpgrade(TypeSpec.Builder builder) {
    builder.addMethod(MethodSpec.methodBuilder("onUpgrade")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ParameterSpec.builder(SQLiteDatabase.class, "db").build())
        .addParameter(ParameterSpec.builder(int.class, "oldVersion").build())
        .addParameter(ParameterSpec.builder(int.class, "newVersion").build())
        .build());
  }

  private CodeBlock bodyForOnCreate() {
    CodeBlock.Builder builder = CodeBlock.builder()
        .addStatement("db.beginTransaction()")
        .beginControlFlow("try")
        .beginControlFlow("for ($T sql : DEFAULT_CREATE)", String.class)
        .addStatement("db.execSQL(sql)")
        .endControlFlow();

    for (ModelInjector modelInjector : models) {
      for (String sql : modelInjector.getCreateStatements()) {
        builder.addStatement("db.execSQL($S)", sql);
      }
    }

    builder.addStatement("db.setTransactionSuccessful()")
        .nextControlFlow("finally")
        .addStatement("db.endTransaction()")
        .endControlFlow();

    return builder.build();
  }

  private void appendOnCreate(TypeSpec.Builder builder) {
    builder.addMethod(MethodSpec.methodBuilder("onCreate")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ParameterSpec.builder(SQLiteDatabase.class, "db").build())
        .addCode(bodyForOnCreate())
        .build());
  }

  private void appendConstructor(TypeSpec.Builder builder) {
    builder.addMethod(MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PRIVATE)
        .addParameter(ParameterSpec.builder(Context.class, "context").build())
        .addStatement("super(context, $S, null, $L)", tableName, 1)
        .build());
  }

  private void appendSingleton(TypeSpec.Builder builder) {
    TypeName selfType = ClassName.get(classPackage, this.className);

    FieldSpec fieldInstance = FieldSpec.builder(ClassName.get(classPackage, this.className),
        "instance", Modifier.STATIC)
        .build();

    builder.addField(fieldInstance);

    builder.addMethod(MethodSpec.methodBuilder("get")
        .returns(selfType)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.SYNCHRONIZED)
        .addParameter(ParameterSpec.builder(Context.class, "context").build())
        .beginControlFlow("if ($N == null)", fieldInstance)
        .addStatement("$N = new $T(context)", fieldInstance, selfType)
        .endControlFlow()
        .addStatement("return $N", fieldInstance)
        .build());
  }

  private FieldSpec.Builder createSetWithInitializer(String name, Class<? extends Set> setClass,
      TypeName typeName) {
    TypeName setTypeName = ParameterizedTypeName.get(
        ClassName.get(Set.class), typeName);

    TypeName linkedSetTypeName = ParameterizedTypeName.get(
        ClassName.get(setClass), typeName);

    return FieldSpec.builder(setTypeName, name)
        .initializer("new $T()", linkedSetTypeName);
  }

  private FieldSpec.Builder createMapWithInitializer(String name, Class<? extends Map> mapClass,
      TypeName keyTypeName, TypeName valueTypeName) {
    TypeName mapTypeName = ParameterizedTypeName.get(
        ClassName.get(Map.class), keyTypeName, valueTypeName);

    TypeName linkedMapTypeName = ParameterizedTypeName.get(
        ClassName.get(mapClass), keyTypeName, valueTypeName);

    return FieldSpec.builder(mapTypeName, name)
        .initializer("new $T()", linkedMapTypeName);
  }

  /*
  @Override String brewJava() {
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
      Iterator<ModelMember> it = model.members.iterator();
      while (it.hasNext()) {
        ModelMember member = it.next();
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
            .append(member.className)
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
  */
}
