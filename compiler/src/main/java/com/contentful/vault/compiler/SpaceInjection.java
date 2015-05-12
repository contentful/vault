package com.contentful.vault.compiler;

import com.contentful.vault.ModelHelper;
import com.contentful.vault.SpaceHelper;
import com.squareup.javapoet.ClassName;
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
        .superclass(ClassName.get(SpaceHelper.class))
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

    appendModels(builder);
    appendTypes(builder);
    appendConstructor(builder);

    return builder;
  }

  private void appendConstructor(TypeSpec.Builder builder) {
    MethodSpec.Builder ctor = MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
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
}
