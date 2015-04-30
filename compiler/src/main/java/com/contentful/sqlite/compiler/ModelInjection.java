package com.contentful.sqlite.compiler;

import com.contentful.sqlite.FieldMeta;
import com.contentful.sqlite.ModelHelper;
import com.contentful.sqlite.SpaceHelper;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
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

final class ModelInjection extends Injection {
  final String sqlTableName;
  final Set<ModelMember> members;
  private FieldSpec specFields;

  public ModelInjection(String remoteId, ClassName className, TypeElement originatingElement,
      String sqlTableName, Set<ModelMember> members) {
    super(remoteId, className, originatingElement);
    this.sqlTableName = sqlTableName;
    this.members = members;
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

    for (ModelMember m : members) {
      StringBuilder statement = new StringBuilder();
      List args = new ArrayList();

      statement.append("$N.add($T.builder()");
      args.add(specFields);
      args.add(ClassName.get(FieldMeta.class));

      statement.append(".setId($S)");
      args.add(m.id);

      statement.append(".setName($S)");
      args.add(m.fieldName);

      if (m.sqliteType != null) {
        statement.append(".setSqliteType($S)");
        args.add(m.sqliteType);
      }

      if (m.isLink()) {
        statement.append(".setLinkType($S)");
        args.add(m.linkType);
      }

      if (m.isArray()) {
        statement.append(".setArrayType($S)");
        args.add(m.arrayType);
      }

      statement.append(".build())");

      ctor.addStatement(statement.toString(), args.toArray());
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

    ModelMember[] array = members.toArray(new ModelMember[members.size()]);
    for (int i = 0; i < array.length; i++) {
      ModelMember member = array[i];
      if (i == 0) {
        method.beginControlFlow("if ($S.equals(name))", member.fieldName);
      } else {
        method.endControlFlow().beginControlFlow("else if ($S.equals(name))", member.fieldName);
      }
      method.addStatement("resource.$L = ($T) value", member.fieldName, member.element.asType());
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
        .addModifiers(Modifier.PUBLIC)
        .addParameter(
            ParameterSpec.builder(ClassName.get("android.database", "Cursor"), "cursor").build());

    String result = "result";
    method.addStatement("$T $N = new $T()", modelClassName, result, modelClassName);

    List<ModelMember> nonLinkMembers = extractFieldMembers();
    for (int i = 0; i < nonLinkMembers.size(); i++) {
      ModelMember member = nonLinkMembers.get(i);
      int columnIndex = SpaceHelper.RESOURCE_COLUMNS.length + i;
      String typeNameString = member.element.asType().toString();
      if (String.class.getName().equals(typeNameString)) {
        method.addStatement("$N.$L = cursor.getString($L)", result, member.fieldName, columnIndex);
      } else if (Boolean.class.getName().equals(typeNameString)) {
        method.addStatement("$N.$L = Integer.valueOf(1).equals(cursor.getInt($L))", result,
            member.fieldName, columnIndex);
      } else if (Integer.class.getName().equals(typeNameString)) {
        method.addStatement("$N.$L = cursor.getInt($L)", result, member.fieldName, columnIndex);
      } else if (Double.class.getName().equals(typeNameString)) {
        method.addStatement("$N.$L = cursor.getDouble($L)", result, member.fieldName, columnIndex);
      } else if (Map.class.getName().equals(typeNameString)) {
        method.addStatement("$N.$L = fieldFromBlob($T.class, cursor, $L)", result, member.fieldName,
            ClassName.get(HashMap.class), columnIndex);
      } else if (member.isArrayOfSymbols()) {
        method.addStatement("$N.$L = fieldFromBlob($T.class, cursor, $L)", result, member.fieldName,
            ClassName.get(ArrayList.class), columnIndex);
      }
    }

    method.addStatement("return $N", result);
    builder.addMethod(method.build());
  }

  private void appendCreateStatements(TypeSpec.Builder builder) {
    MethodSpec.Builder method = MethodSpec.methodBuilder("getCreateStatements")
        .returns(ParameterizedTypeName.get(List.class, String.class))
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC);

    method.addStatement("$T list = new $T()",
        ParameterizedTypeName.get(List.class, String.class),
        ParameterizedTypeName.get(ArrayList.class, String.class));

    for (String sql : getSqlCreateStatements()) {
      method.addStatement("list.add($S)", sql);
    }

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

  List<String> getSqlCreateStatements() {
    List<String> statements = new ArrayList<String>();
    StringBuilder builder = new StringBuilder();
    builder.append("CREATE TABLE `")
        .append(sqlTableName)
        .append("` (");

    for (int i = 0; i < SpaceHelper.RESOURCE_COLUMNS.length; i++) {
      builder.append(SpaceHelper.RESOURCE_COLUMNS[i]);
      if (i < SpaceHelper.RESOURCE_COLUMNS.length - 1) {
        builder.append(", ");
      }
    }

    List<ModelMember> list = extractFieldMembers();
    for (int i = 0; i < list.size(); i++) {
      ModelMember member = list.get(i);
      builder.append(", `")
          .append(member.fieldName)
          .append("` ")
          .append(member.sqliteType);
    }
    builder.append(");");
    statements.add(builder.toString());
    return statements;
  }

  List<ModelMember> extractFieldMembers() {
    List<ModelMember> result = new ArrayList<ModelMember>();
    for (ModelMember member : members) {
      // Skip links
      if (member.isLink()) {
        continue;
      }
      // Skip arrays of links
      if (member.arrayType != null && !String.class.getName().equals(member.arrayType.toString())) {
        continue;
      }
      result.add(member);
    }
    return result;
  }
}
