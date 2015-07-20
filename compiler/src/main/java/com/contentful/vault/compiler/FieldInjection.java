package com.contentful.vault.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.TypeElement;

final class FieldInjection extends Injection {
  public FieldInjection(String remoteId, ClassName className, TypeElement originatingElement) {
    super(remoteId, className, originatingElement);
  }

  @Override TypeSpec.Builder getTypeSpecBuilder() {
    // TODO
    return null;
  }
}
