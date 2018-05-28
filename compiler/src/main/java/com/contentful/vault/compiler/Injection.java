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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

abstract class Injection {
  final String remoteId;

  final ClassName className;

  final TypeElement originatingElement;

  public Injection(String remoteId, ClassName className, TypeElement originatingElement) {
    this.remoteId = remoteId;
    this.className = className;
    this.originatingElement = originatingElement;
  }

  JavaFile brewJava() {
    TypeSpec.Builder typeSpecBuilder = getTypeSpecBuilder()
        .addOriginatingElement(originatingElement);

    return JavaFile.builder(className.packageName(), typeSpecBuilder.build())
        .skipJavaLangImports(true)
        .build();
  }

  abstract TypeSpec.Builder getTypeSpecBuilder();

  protected MethodSpec.Builder createGetterImpl(FieldSpec field, String name) {
    return MethodSpec.methodBuilder(name)
        .returns(field.type)
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addStatement("return $N", field.name);
  }

  protected FieldSpec.Builder createListWithInitializer(String name,
      Class<? extends List> listClass, TypeName typeName) {
    TypeName listTypeName = ParameterizedTypeName.get(ClassName.get(List.class), typeName);

    TypeName initializerTypeName =
        ParameterizedTypeName.get(ClassName.get(listClass), typeName);

    return FieldSpec.builder(listTypeName, name).initializer("new $T()", initializerTypeName);
  }

  protected FieldSpec.Builder createMapWithInitializer(String name, Class<? extends Map> mapClass,
      TypeName keyTypeName, TypeName valueTypeName) {
    TypeName mapTypeName = ParameterizedTypeName.get(
        ClassName.get(Map.class), keyTypeName, valueTypeName);

    TypeName linkedMapTypeName = ParameterizedTypeName.get(
        ClassName.get(mapClass), keyTypeName, valueTypeName);

    return FieldSpec.builder(mapTypeName, name)
        .initializer("new $T()", linkedMapTypeName);
  }
}
