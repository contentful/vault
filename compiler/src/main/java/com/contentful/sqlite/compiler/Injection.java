package com.contentful.sqlite.compiler;

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

abstract class Injection {
  final String remoteId;
  final String classPackage;
  final String className;
  final String enclosingClass;

  public Injection(String remoteId, String classPackage, String className, String enclosingClass) {
    this.remoteId = remoteId;
    this.classPackage = classPackage;
    this.className = className;
    this.enclosingClass = enclosingClass;
  }

  JavaFile brewJava() {
    return JavaFile.builder(classPackage, buildTypeSpec()).skipJavaLangImports(true).build();
  }

  abstract TypeSpec buildTypeSpec();

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
