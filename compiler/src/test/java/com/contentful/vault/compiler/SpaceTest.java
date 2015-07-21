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

package com.contentful.vault.compiler;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.contentful.vault.compiler.lib.TestUtils.processors;
import static com.contentful.vault.compiler.lib.TestUtils.readTestResource;
import static com.google.common.truth.Truth.ASSERT;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class SpaceTest {
  @Test public void testInjection() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("Test", Joiner.on('\n').join(
        "import com.contentful.vault.Asset;",
        "import com.contentful.vault.ContentType;",
        "import com.contentful.vault.Field;",
        "import com.contentful.vault.Resource;",
        "import com.contentful.vault.Space;",
        "import java.util.Map;",
        "class Test {",
        "  @ContentType(\"cid\")",
        "  static class Model extends Resource {",
        "    @Field String fText;",
        "    @Field Boolean fBoolean;",
        "    @Field Integer fInteger;",
        "    @Field Double fDouble;",
        "    @Field Map fMap;",
        "    @Field Model fLinkedModel;",
        "    @Field Asset fLinkedAsset;",
        "  }",
        "",
        "  @Space(value = \"sid\", models = { Model.class })",
        "  static class AwesomeSpace {",
        "  }",
        "}"));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString(
        "Test$AwesomeSpace$$SpaceHelper",
        readTestResource("SpaceInjection.java"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test public void testVersion() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("Test", Joiner.on('\n').join(
        "import com.contentful.vault.ContentType;",
        "import com.contentful.vault.Field;",
        "import com.contentful.vault.Resource;",
        "import com.contentful.vault.Space;",
        "import java.util.Map;",
        "class Test {",
        "  @ContentType(\"cid\")",
        "  static class Model extends Resource {",
        "    @Field String f;",
        "  }",
        "",
        "  @Space(value = \"sid\", models = { Model.class }, dbVersion = 9)",
        "  static class AwesomeSpace {",
        "  }",
        "}"));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString(
        "Test$AwesomeSpace$$SpaceHelper",
        readTestResource("SpaceInjectionVersion.java"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test public void testCopyPath() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("Test", Joiner.on('\n').join(
        "import com.contentful.vault.ContentType;",
        "import com.contentful.vault.Field;",
        "import com.contentful.vault.Resource;",
        "import com.contentful.vault.Space;",
        "import java.util.Map;",
        "class Test {",
        "  @ContentType(\"cid\")",
        "  static class Model extends Resource {",
        "    @Field String f;",
        "  }",
        "",
        "  @Space(value = \"sid\", models = { Model.class }, copyPath = \"foo.db\")",
        "  static class AwesomeSpace {",
        "  }",
        "}"));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString(
        "Test$AwesomeSpace$$SpaceHelper",
        readTestResource("SpaceInjectionCopyPath.java"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test public void failsEmptyId() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("Test", Joiner.on('\n').join(
        "import com.contentful.vault.Space;",
        "@Space(value = \"\", models = {})",
        "class Test {",
        "}"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .failsToCompile()
        .withErrorContaining("@Space id may not be empty. (Test)");
  }

  @Test public void failsEmptyModels() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("Test", Joiner.on('\n').join(
        "import com.contentful.vault.Space;",
        "@Space(value = \"sid\", models = { })",
        "class Test {",
        "}"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .failsToCompile()
        .withErrorContaining("@Space models must not be empty. (Test)");
  }

  @Test public void failsDuplicateId() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
        "import com.contentful.vault.ContentType;",
        "import com.contentful.vault.Resource;",
        "import com.contentful.vault.Space;",
        "class Test {",
        "  @ContentType(\"cid\")",
        "  static class Test1 extends Resource { }",
        "  @ContentType(\"cid\")",
        "  static class Test2 extends Resource { }",
        "  @Space(value = \"sid\", models = { Test1.class, Test2.class })",
        "  static class Test3 {}",
        "}"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .failsToCompile()
        .withErrorContaining(
            "@Space includes multiple models with the same id \"cid\". (Test.Test3)");
  }
}
