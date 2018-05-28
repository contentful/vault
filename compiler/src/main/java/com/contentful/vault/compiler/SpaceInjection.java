/*
 * Copyright (C) 2018 Contentful GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.contentful.vault.compiler;

import com.contentful.vault.ModelHelper;
import com.contentful.vault.Resource;
import com.contentful.vault.SpaceHelper;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

final class SpaceInjection extends Injection {
  private final List<ModelInjection> models;

  private final String dbName;

  private final int dbVersion;

  private final String copyPath;

  private final List<String> locales;

  private FieldSpec specModels;

  private FieldSpec specTypes;

  public SpaceInjection(String remoteId, ClassName className, TypeElement originatingElement,
      List<ModelInjection> models, String dbName, int dbVersion, String copyPath,
      List<String> locales) {
    super(remoteId, className, originatingElement);
    this.models = models;
    this.dbName = dbName;
    this.dbVersion = dbVersion;
    this.copyPath = copyPath;
    this.locales = locales;
  }

  @Override TypeSpec.Builder getTypeSpecBuilder() {
    TypeSpec.Builder builder = TypeSpec.classBuilder(className.simpleName())
        .superclass(ClassName.get(SpaceHelper.class))
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

    appendDbName(builder);
    appendDbVersion(builder);
    appendModels(builder);
    appendTypes(builder);
    appendCopyPath(builder);
    appendSpaceId(builder);
    appendLocales(builder);
    appendConstructor(builder);

    return builder;
  }

  private void appendConstructor(TypeSpec.Builder builder) {
    MethodSpec.Builder ctor = MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC);

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
        WildcardTypeName.subtypeOf(Resource.class));

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

  private void appendDbVersion(TypeSpec.Builder builder) {
    builder.addMethod(MethodSpec.methodBuilder("getDatabaseVersion")
        .returns(int.class)
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC).addStatement("return $L", dbVersion)
        .build());
  }

  private void appendDbName(TypeSpec.Builder builder) {
    builder.addMethod(MethodSpec.methodBuilder("getDatabaseName")
        .returns(String.class)
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addStatement("return $S", dbName)
        .build());
  }

  private void appendCopyPath(TypeSpec.Builder builder) {
    builder.addMethod(MethodSpec.methodBuilder("getCopyPath")
        .returns(String.class)
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addStatement("return $S", copyPath)
        .build());
  }

  private void appendSpaceId(TypeSpec.Builder builder) {
    builder.addMethod(MethodSpec.methodBuilder("getSpaceId")
        .returns(String.class)
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addStatement("return $S", remoteId)
        .build());
  }

  private void appendLocales(TypeSpec.Builder builder) {
    CodeBlock.Builder block = CodeBlock.builder().add("$T.asList(", Arrays.class);

    String[] array = locales.toArray(new String[locales.size()]);
    for (int i = 0; i < array.length; i++) {
      if (i > 0) {
        block.add(", ");
      }

      block.add("$S", array[i]);
    }
    block.add(")");

    FieldSpec field =
        FieldSpec.builder(ParameterizedTypeName.get(List.class, String.class), "locales",
            Modifier.FINAL)
            .initializer(block.build())
            .build();

    builder.addField(field);

    builder.addMethod(MethodSpec.methodBuilder("getLocales")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(ParameterizedTypeName.get(List.class, String.class))
        .addStatement("return $N", field.name)
        .build());
  }
}
