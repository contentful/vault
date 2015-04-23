package com.contentful.sqlite.compiler;

import com.contentful.sqlite.ModelHelper;
import com.contentful.sqlite.SpaceHelper;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import java.util.LinkedHashMap;
import java.util.List;
import javax.lang.model.element.Modifier;

final class SpaceInjection extends Injection {
  private final List<ModelInjection> models;
  private final String tableName;
  private FieldSpec specModels;
  private FieldSpec specTypes;

  public SpaceInjection(String remoteId, String classPackage, String className,
      String enclosingClass, List<ModelInjection> models, String tableName) {
    super(remoteId, classPackage, className, enclosingClass);
    this.models = models;
    this.tableName = tableName;
  }

  @Override TypeSpec buildTypeSpec() {
    TypeSpec.Builder builder = TypeSpec.classBuilder(className)
        .superclass(ClassName.get("android.database.sqlite", "SQLiteOpenHelper"))
        .addSuperinterface(SpaceHelper.class)
        .addModifiers(Modifier.FINAL);

    appendModels(builder);
    appendTypes(builder);
    appendSingleton(builder);
    appendOnCreate(builder);
    appendOnUpgrade(builder);
    appendConstructor(builder);

    return builder.build();
  }

  private void appendConstructor(TypeSpec.Builder builder) {
    MethodSpec.Builder ctor = MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PRIVATE)
        .addParameter(
            ParameterSpec.builder(ClassName.get("android.content", "Context"), "context").build())
        .addStatement("super(context, $S, null, $L)", tableName, 1);

    for (ModelInjection model : models) {
      ctor.addStatement("$N.put($L.class, new $L())", specModels, model.enclosingClass,
          model.className);

      ctor.addStatement("$N.put($S, $L.class)", specTypes, model.remoteId, model.enclosingClass);
    }

    builder.addMethod(ctor.build());
  }

  private void appendTypes(TypeSpec.Builder builder) {
    // Field
    TypeName classTypeName = ParameterizedTypeName.get(ClassName.get(Class.class),
        WildcardTypeName.subtypeOf(Object.class));

    specTypes =
        createMapWithInitializer("types", LinkedHashMap.class, ClassName.get(String.class),
            classTypeName)
            .addModifiers(Modifier.FINAL)
            .build();

    builder.addField(specTypes);

    // Getter
    builder.addMethod(createGetterImpl(specTypes, "getTypes").build());
  }

  private void appendModels(TypeSpec.Builder builder) {
    // Field
    TypeName classTypeName = ParameterizedTypeName.get(ClassName.get(Class.class),
        WildcardTypeName.subtypeOf(Object.class));

    TypeName helperTypeName = ParameterizedTypeName.get(ClassName.get(ModelHelper.class),
        WildcardTypeName.subtypeOf(Object.class));

    specModels = createMapWithInitializer("models", LinkedHashMap.class, classTypeName,
        helperTypeName).addModifiers(Modifier.FINAL).build();

    builder.addField(specModels);

    // Getter
    builder.addMethod(createGetterImpl(specModels, "getModels").build());
  }

  private void appendOnUpgrade(TypeSpec.Builder builder) {
    builder.addMethod(MethodSpec.methodBuilder("onUpgrade")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addParameter(
            ParameterSpec.builder(ClassName.get("android.database.sqlite", "SQLiteDatabase"), "db")
                .build())
        .addParameter(ParameterSpec.builder(int.class, "oldVersion").build())
        .addParameter(ParameterSpec.builder(int.class, "newVersion").build())
        .build());
  }

  private CodeBlock bodyForOnCreate() {
    CodeBlock.Builder builder = CodeBlock.builder()
        .addStatement("db.beginTransaction()")
        .beginControlFlow("try");

    // default create statements
    builder.beginControlFlow("for ($T sql : DEFAULT_CREATE)", String.class)
        .addStatement("db.execSQL(sql)")
        .endControlFlow();

    // models create statements
    TypeName helperType = ParameterizedTypeName.get(ClassName.get(ModelHelper.class),
        WildcardTypeName.subtypeOf(Object.class));

    builder.beginControlFlow("for ($T modelHelper : $N.values())", helperType, specModels)
        .beginControlFlow("for ($T sql : modelHelper.getCreateStatements())", String.class)
        .addStatement("db.execSQL(sql)")
        .endControlFlow()
        .endControlFlow();

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
        .addParameter(
            ParameterSpec.builder(ClassName.get("android.database.sqlite", "SQLiteDatabase"), "db")
                .build())
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
        .addParameter(
            ParameterSpec.builder(ClassName.get("android.content", "Context"), "context").build())
        .beginControlFlow("if ($N == null)", fieldInstance)
        .addStatement("$N = new $T(context)", fieldInstance, selfType)
        .endControlFlow()
        .addStatement("return $N", fieldInstance)
        .build());
  }
}
