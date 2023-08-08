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

import com.contentful.vault.FieldMeta;
import com.contentful.vault.ModelHelper;
import com.contentful.vault.SpaceHelper;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import static com.contentful.vault.Sql.RESOURCE_COLUMNS;

final class ModelInjection extends Injection {
  final String sqlTableName;

  final Set<FieldMeta> fields;

  private FieldSpec specFields;

  public ModelInjection(String remoteId, ClassName className, TypeElement originatingElement,
      String sqlTableName, Set<FieldMeta> fields) {
    super(remoteId, className, originatingElement);
    this.sqlTableName = sqlTableName;
    this.fields = fields;
  }

  @Override TypeSpec.Builder getTypeSpecBuilder() {
    ParameterizedTypeName modelHelperType =
        ParameterizedTypeName.get(ClassName.get(ModelHelper.class),
            ClassName.get(originatingElement));

    TypeSpec.Builder builder = TypeSpec.classBuilder(className.simpleName())
        .superclass(modelHelperType)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

    appendFields(builder);
    appendTableName(builder);
    appendCreateStatements(builder);
    appendFromCursor(builder);
    appendSetField(builder);
    appendConstructor(builder);

    return builder;
  }

  @SuppressWarnings("unchecked")
  private void appendConstructor(TypeSpec.Builder builder) {
    MethodSpec.Builder ctor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);

    for (FieldMeta f : fields) {
      CodeBlock.Builder block = CodeBlock.builder();

      block.add("$N.add($T.builder()", specFields, ClassName.get(FieldMeta.class))
          .add(".setId($S)", f.id())
          .add(".setName($S)", f.name());

      if (f.setter() != null) {
        block.add(".setSetter($S)", f.setter());
      }

      if (f.sqliteType() != null) {
        block.add(".setSqliteType($S)", f.sqliteType());
      }

      if (f.isLink()) {
        block.add(".setLinkType($S)", f.linkType());
      }

      if (f.isArray()) {
        block.add(".setArrayType($S)", f.arrayType());
      }

      block.add(".build());\n");

      ctor.addCode(block.build());
    }

    builder.addMethod(ctor.build());
  }

  private void appendSetField(TypeSpec.Builder builder) {
    MethodSpec.Builder method = MethodSpec.methodBuilder("setField")
        .addAnnotation(Override.class)
        .addAnnotation(
            AnnotationSpec.builder(SuppressWarnings.class)
                .addMember("value", "$S", "unchecked")
                .build())
        .addModifiers(Modifier.PUBLIC)
        .returns(boolean.class)
        .addParameter(ParameterSpec.builder(ClassName.get(originatingElement), "resource").build())
        .addParameter(ParameterSpec.builder(ClassName.get(String.class), "name").build())
        .addParameter(ParameterSpec.builder(ClassName.get(Object.class), "value").build());

    FieldMeta[] array = fields.toArray(new FieldMeta[fields.size()]);
    for (int i = 0; i < array.length; i++) {
      FieldMeta field = array[i];
      if (i == 0) {
        method.beginControlFlow("if ($S.equals(name))", field.name());
      } else {
        method.endControlFlow().beginControlFlow("else if ($S.equals(name))", field.name());
      }
      addStatement(method, field, "($T) value", "resource", field.type());
    }
    method.endControlFlow()
        .beginControlFlow("else")
          .addStatement("return false")
        .endControlFlow()
        .addStatement("return true");

    builder.addMethod(method.build());
  }

  private void appendFromCursor(TypeSpec.Builder builder) {
    ClassName modelClassName = ClassName.get(originatingElement);

    MethodSpec.Builder method = MethodSpec.methodBuilder("fromCursor")
        .returns(modelClassName)
        .addAnnotation(Override.class)
        .addAnnotation(
            AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "$S", "unchecked").build())
        .addModifiers(Modifier.PUBLIC)
        .addParameter(
            ParameterSpec.builder(ClassName.get("android.database", "Cursor"), "cursor").build());

    String result = "result";
    method.addStatement("$T $N = new $T()", modelClassName, result, modelClassName)
        .addStatement("setContentType($N, $S)", result, remoteId);

    List<FieldMeta> nonLinkFields = extractNonLinkFields();
    for (int i = 0; i < nonLinkFields.size(); i++) {
      FieldMeta field = nonLinkFields.get(i);
      int columnIndex = RESOURCE_COLUMNS.length + i;
      String fqClassName = field.type().toString();

      if (String.class.getName().equals(fqClassName)) {
        addStatement(method, field,"cursor.getString($L)", result, columnIndex);
      } else if (Boolean.class.getName().equals(fqClassName)) {
        addStatement(method, field, "Integer.valueOf(1).equals(cursor.getInt($L))", result, columnIndex);
      } else if (Integer.class.getName().equals(fqClassName)) {
        addStatement(method, field, "cursor.getInt($L)", result, columnIndex);
      } else if (Double.class.getName().equals(fqClassName)) {
        addStatement(method, field, "cursor.getDouble($L)", result, columnIndex);
      } else if (Map.class.getName().equals(fqClassName)) {
        addStatement(method, field, "fieldFromBlob($T.class, cursor, $L)", result,
            ClassName.get(HashMap.class), columnIndex);
      } else if (field.isArrayOfSymbols()) {
        addStatement(method, field, "fieldFromBlob($T.class, cursor, $L)", result,
            ClassName.get(ArrayList.class), columnIndex);
      }
    }

    method.addStatement("return $N", result);
    builder.addMethod(method.build());
  }

  private void addStatement(MethodSpec.Builder method, FieldMeta field, String operation, String resource, Object... args) {
    Object[] newArgs = new Object[args.length + 2];
    System.arraycopy(args, 0, newArgs, 2, args.length);
    newArgs[0] = resource;

    if(field.setter() != null) {
      newArgs[1] = field.setter();
      method.addStatement("$N.$L(" + operation + ")", newArgs);
    }
    else {
      newArgs[1] = field.name();
      method.addStatement("$N.$L = " + operation, newArgs);
    }
  }

  private void appendCreateStatements(TypeSpec.Builder builder) {
    MethodSpec.Builder method = MethodSpec.methodBuilder("getCreateStatements")
        .returns(ParameterizedTypeName.get(List.class, String.class))
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addParameter(SpaceHelper.class, "spaceHelper");

    method.addStatement("$T list = new $T()",
        ParameterizedTypeName.get(List.class, String.class),
        ParameterizedTypeName.get(ArrayList.class, String.class));


    method.beginControlFlow("for (String code : spaceHelper.getLocales())");
    for (String sql : getModelCreateStatements()) {
      method.addStatement("list.add($L)", sql);
    }
    method.endControlFlow();

    method.addStatement("return list");
    builder.addMethod(method.build());
  }

  private void appendTableName(TypeSpec.Builder builder) {
    builder.addMethod(MethodSpec.methodBuilder("getTableName")
        .returns(ClassName.get(String.class))
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addStatement("return $S", sqlTableName)
        .build());
  }

  private void appendFields(TypeSpec.Builder builder) {
    // Field
    specFields = createListWithInitializer("fields", ArrayList.class,
        ClassName.get(FieldMeta.class)).addModifiers(Modifier.FINAL).build();

    builder.addField(specFields);

    // Getter
    builder.addMethod(createGetterImpl(specFields, "getFields").build());
  }

  List<String> getModelCreateStatements() {
    List<String> statements = new ArrayList<>();
    StringBuilder builder = new StringBuilder();
    builder.append("\"CREATE TABLE `")
        .append(sqlTableName)
        .append("$\" + code + \"")
        .append("` (");

    for (int i = 0; i < RESOURCE_COLUMNS.length; i++) {
      builder.append(RESOURCE_COLUMNS[i]);
      if (i < RESOURCE_COLUMNS.length - 1) {
        builder.append(", ");
      }
    }

    List<FieldMeta> list = extractNonLinkFields();
    for (int i = 0; i < list.size(); i++) {
      FieldMeta f = list.get(i);
      builder.append(", `")
          .append(f.name())
          .append("` ")
          .append(f.sqliteType());
    }
    builder.append(");\"");
    statements.add(builder.toString());
    return statements;
  }

  List<FieldMeta> extractNonLinkFields() {
    List<FieldMeta> result = new ArrayList<>();
    for (FieldMeta f : fields) {
      // Skip links / arrays of links
      if (f.isLink() || f.isArrayOfLinks()) {
        continue;
      }
      result.add(f);
    }
    return result;
  }
}
