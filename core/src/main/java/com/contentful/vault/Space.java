package com.contentful.vault;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

@Target(TYPE)
@Retention(CLASS)
public @interface Space {
  String value();

  Class<?>[] models();

  int dbVersion() default 1;
}
