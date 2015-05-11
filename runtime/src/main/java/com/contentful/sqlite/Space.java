package com.contentful.sqlite;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

@Target(TYPE)
@Retention(CLASS)
public @interface Space {
  /** Remote ID. */
  String value();

  /** List of model classes annotated with {@link com.contentful.sqlite.ContentType}. */
  Class<?>[] models();

  /** Optional locale code to use when persisting data, leave empty for default locale. */
  String localeCode() default "";
}
