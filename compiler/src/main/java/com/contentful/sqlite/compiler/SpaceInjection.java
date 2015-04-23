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
import java.util.ArrayList;
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
    appendOnCreate(builder);
    appendOnUpgrade(builder);
    appendModels(builder);
    appendTables(builder);
    appendTypes(builder);
    appendFields(builder);
    appendConstructor(builder);

    return JavaFile.builder(classPackage, builder.build())
        .build();
  }

  private void appendConstructor(TypeSpec.Builder builder) {
    MethodSpec.Builder ctor = MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PRIVATE)
        .addParameter(ParameterSpec.builder(Context.class, "context").build())
        .addStatement("super(context, $S, null, $L)", tableName, 1);

    // Temporary variable to hold model class reference
    String classHolder = "clazz";

    TypeName classHolderType = ParameterizedTypeName.get(
        ClassName.get(Class.class),
        WildcardTypeName.subtypeOf(Object.class));

    ctor.addStatement("$T $N", classHolderType, classHolder);

    // Temporary variable to hold model fields
    String fieldsHolder = "fieldsHolder";

    TypeName fieldsHolderType = ParameterizedTypeName.get(
        ClassName.get(ArrayList.class),
        WildcardTypeName.get(FieldMeta.class));

    ctor.addStatement("$T $N = new $T()", fieldsHolderType, fieldsHolder, fieldsHolderType);

    for (int i = 0; i < models.size(); i++) {
      ModelInjector model = models.get(i);
      ctor.addStatement("$N = $L.class", classHolder, model.className) // class holder
          .addStatement("$N.put($S, $N)", specTypes, model.id, classHolder) // types
          .addStatement("$N.put($N, $S)", specTables, classHolder, model.tableName); // tables

      // fields
      if (i > 0) {
        ctor.addStatement("$N.clear()", fieldsHolder);
      }
      for (ModelMember member : model.members) {
        ctor.addStatement("$N.add(new $T($S, $S, $S, $S, $S))",
            fieldsHolder,
            ClassName.get(FieldMeta.class),
            member.id,
            member.fieldName,
            member.sqliteType,
            member.linkType,
            member.className);
      }
      ctor.addStatement("$N.put($N, $N)", specFields, classHolder, fieldsHolder);
    }

    ctor.addStatement("$N.addAll($N.keySet())", specModels, specTables);

    builder.addMethod(ctor.build());
  }

  private void appendFields(TypeSpec.Builder builder) {
    // Field
    TypeName classTypeName = ParameterizedTypeName.get(
        ClassName.get(Class.class),
        WildcardTypeName.subtypeOf(Object.class));

    TypeName listTypeName = ParameterizedTypeName.get(ClassName.get(List.class),
        ClassName.get(FieldMeta.class));

    specFields = createMapWithInitializer("fields", LinkedHashMap.class, classTypeName,
        listTypeName)
        .addModifiers(Modifier.STATIC, Modifier.FINAL)
        .build();

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

    specTables = createMapWithInitializer("tables", LinkedHashMap.class, classTypeName,
        ClassName.get(String.class))
        .addModifiers(Modifier.STATIC, Modifier.FINAL)
        .build();

    builder.addField(specTables);

    // Getter
    builder.addMethod(createGetterImpl(specTables, "getTables").build());
  }

  private void appendModels(TypeSpec.Builder builder) {
    // Field
    TypeName classTypeName = ParameterizedTypeName.get(ClassName.get(Class.class),
        WildcardTypeName.subtypeOf(Object.class));

    specModels = createSetWithInitializer("models", LinkedHashSet.class, classTypeName)
        .addModifiers(Modifier.STATIC, Modifier.FINAL)
        .build();

    builder.addField(specModels);

    // Getter
    builder.addMethod(createGetterImpl(specModels, "getModels").build());
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
}
