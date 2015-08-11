/*
 * Copyright (C) 2015 Contentful GmbH
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

package com.contentful.vault;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

@Target(TYPE)
@Retention(CLASS)
public @interface Space {
  /** Remote ID */
  String value();

  /** Supported {@link Resource} models. */
  Class<?>[] models();

  /**
   * Version of the database. Increasing this value will trigger a migration.
   * WARNING: Migrations invalidate any pre-existing data.
   */
  int dbVersion() default 1;

  /**
   * File path under the application's 'assets' folder pointing to an existing Vault database file.
   * This is used for pre-populating the database with static content prior to initial sync.
   * The first time the database file is created if this property is not blank Vault will attempt
   * and copy the contents to its database.
   *
   * If you wish to apply a {@code copyPath} to an shipped app make sure to increase
   * the {@code dbVersion} property.
   */
  String copyPath() default "";

  /** Array of locale codes. */
  String[] locales();
}
