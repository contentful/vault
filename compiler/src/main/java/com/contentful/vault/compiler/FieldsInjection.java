package com.contentful.vault.compiler;

import com.contentful.vault.BaseFields;
import com.contentful.vault.FieldMeta;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

final class FieldsInjection extends Injection {
  private final Set<FieldMeta> fields;

  public FieldsInjection(String remoteId, ClassName className, TypeElement originatingElement,
      Set<FieldMeta> fields) {
    super(remoteId, className, originatingElement);
    this.fields = fields;
  }

  @Override TypeSpec.Builder getTypeSpecBuilder() {
    TypeSpec.Builder builder = TypeSpec.classBuilder(className.simpleName())
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        .superclass(BaseFields.class);

    for (FieldMeta field : fields) {
      builder.addField(createFieldSpec(field));
    }

    return builder;
  }

  private FieldSpec createFieldSpec(FieldMeta field) {
    String name = CaseFormat.LOWER_CAMEL
        .converterTo(CaseFormat.UPPER_UNDERSCORE)
        .convert(field.name());
    if (name == null) {
      throw new IllegalArgumentException(
          "Invalid field with ID '" + field.id() + "' for generated class '" +
              className.simpleName() + "', has no name.");
    }
    return FieldSpec.builder(String.class, name, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
        .initializer("$S", field.name())
        .build();
  }
}
