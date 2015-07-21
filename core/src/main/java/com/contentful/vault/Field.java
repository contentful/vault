package com.contentful.vault;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.CLASS;

@Target(FIELD)
@Retention(CLASS)
public @interface Field {
  /** Remote ID. Leave this blank in order to use the name of the annotated field as the ID. */
  String value() default "";
}
