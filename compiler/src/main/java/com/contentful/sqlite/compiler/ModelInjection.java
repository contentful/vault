package com.contentful.sqlite.compiler;

import com.contentful.sqlite.FieldMeta;
import com.contentful.sqlite.ModelHelper;
import com.contentful.sqlite.PersistenceHelper;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;

final class ModelInjection extends Injection {
  final String sqlTableName;
  final Set<ModelMember> members;
  private FieldSpec specFields;

  public ModelInjection(String remoteId, String classPackage, String className,
      String enclosingClass, String sqlTableName, Set<ModelMember> members) {
    super(remoteId, classPackage, className, enclosingClass);
    this.sqlTableName = sqlTableName;
    this.members = members;
  }

  @Override TypeSpec buildTypeSpec() {
    TypeName enclosingTypeName = ClassName.get(classPackage, enclosingClass);

    ParameterizedTypeName modelHelperType =
        ParameterizedTypeName.get(ClassName.get(ModelHelper.class), enclosingTypeName);

    TypeSpec.Builder builder = TypeSpec.classBuilder(this.className)
        .addSuperinterface(modelHelperType)
        .addModifiers(Modifier.FINAL);

    appendFields(builder);
    appendTableName(builder);
    appendConstructor(builder);

    return builder.build();
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

  List<String> getCreateStatements() {
    List<String> statements = new ArrayList<String>();
    StringBuilder builder = new StringBuilder();
    builder.append("CREATE TABLE `")
        .append(sqlTableName)
        .append("` (");

    for (String column : PersistenceHelper.RESOURCE_COLUMNS) {
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
