package com.contentful.sqlite.compiler;

import com.contentful.sqlite.ModelHelper;
import com.contentful.sqlite.SpaceHelper;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.squareup.javapoet.WildcardTypeName;
import java.util.LinkedHashMap;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

final class SpaceInjection extends Injection {
  private final List<ModelInjection> models;
  private final String tableName;
  private FieldSpec specModels;
  private FieldSpec specTypes;

  public SpaceInjection(String remoteId, ClassName className, TypeElement originatingElement,
      List<ModelInjection> models, String tableName) {
    super(remoteId, className, originatingElement);
    this.models = models;
    this.tableName = tableName;
  }

  @Override TypeSpec.Builder getTypeSpecBuilder() {
    TypeSpec.Builder builder = TypeSpec.classBuilder(className.simpleName())
        .superclass(ClassName.get("android.database.sqlite", "SQLiteOpenHelper"))
        .addSuperinterface(SpaceHelper.class)
        .addModifiers(Modifier.FINAL);

    appendModels(builder);
    appendTypes(builder);
    appendSingleton(builder);
    appendOnCreate(builder);
    appendOnUpgrade(builder);
    appendFromCursor(builder);
    appendConstructor(builder);

    return builder;
  }

  private void appendConstructor(TypeSpec.Builder builder) {
    MethodSpec.Builder ctor = MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PRIVATE)
        .addParameter(
            ParameterSpec.builder(ClassName.get("android.content", "Context"), "context").build())
        .addStatement("super(context, $S, null, $L)", tableName, 1);

    for (ModelInjection model : models) {
      ClassName enclosingClassName = ClassName.get(model.originatingElement);
      ctor.addStatement("$N.put($L.class, new $L())", specModels, enclosingClassName,
          model.className);

      ctor.addStatement("$N.put($S, $L.class)", specTypes, model.remoteId, enclosingClassName);
    }

    builder.addMethod(ctor.build());
  }

  private void appendFromCursor(TypeSpec.Builder builder) {
    TypeVariableName typeVariableName = TypeVariableName.get("T");

    TypeName classTypeName =
        ParameterizedTypeName.get(ClassName.get(Class.class), typeVariableName);

    ClassName cursorType = ClassName.get("android.database", "Cursor");

    MethodSpec.Builder method = MethodSpec.methodBuilder("fromCursor")
        .returns(typeVariableName)
        .addTypeVariable(typeVariableName)
        .addAnnotation(Override.class)
        .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
            .addMember("value", "$S", "unchecked")
            .build())
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ParameterSpec.builder(classTypeName, "clazz").build())
        .addParameter(ParameterSpec.builder(cursorType, "cursor").build())
        .addStatement("return ($T) $N.get($N).fromCursor($N)", typeVariableName, specModels,
            "clazz", "cursor");

    builder.addMethod(method.build());
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
    FieldSpec fieldInstance = FieldSpec.builder(className, "instance", Modifier.STATIC).build();

    builder.addField(fieldInstance);

    builder.addMethod(MethodSpec.methodBuilder("get")
        .returns(className)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.SYNCHRONIZED)
        .addParameter(
            ParameterSpec.builder(ClassName.get("android.content", "Context"), "context").build())
        .beginControlFlow("if ($N == null)", fieldInstance)
        .addStatement("$N = new $T(context)", fieldInstance, className)
        .endControlFlow()
        .addStatement("return $N", fieldInstance)
        .build());
  }
}
