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
