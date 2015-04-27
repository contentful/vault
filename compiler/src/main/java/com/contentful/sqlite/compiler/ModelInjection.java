package com.contentful.sqlite.compiler;

import com.contentful.sqlite.FieldMeta;
import com.contentful.sqlite.ModelHelper;
import com.contentful.sqlite.SpaceHelper;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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
        .addSuperinterface(modelHelperType)
        .addModifiers(Modifier.FINAL);

    appendFields(builder);
    appendTableName(builder);
    appendCreateStatements(builder);
    appendConstructor(builder);

    return builder;
  }

  private void appendConstructor(TypeSpec.Builder builder) {
    MethodSpec.Builder ctor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);

    for (ModelMember m : members) {
      ctor.addStatement("$N.add(new $T($S, $S, $S, $S, $S))",
          specFields,
          ClassName.get(FieldMeta.class),
          m.id,
          m.fieldName,
          m.sqliteType,
          m.linkType,
          m.className);
    }

    builder.addMethod(ctor.build());
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

    for (String column : SpaceHelper.RESOURCE_COLUMNS) {
      builder.append(column);
      builder.append(", ");
    }

    Set<ModelMember> filtered = getNonLinkMembers();
    ModelMember[] list = filtered.toArray(new ModelMember[filtered.size()]);
    for (int i = 0; i < list.length; i++) {
      ModelMember member = list[i];
      builder.append("`")
          .append(member.fieldName)
          .append("` ")
          .append(member.sqliteType);

      if (i < list.length - 1) {
        builder.append(", ");
      }
    }
    builder.append(");");
    statements.add(builder.toString());
    return statements;
  }

  Set<ModelMember> getNonLinkMembers() {
    Set<ModelMember> result = new LinkedHashSet<ModelMember>();
    for (ModelMember member : members) {
      if (!member.isLink()) {
        result.add(member);
      }
    }
    return result;
  }
}
