package com.contentful.vault.compiler;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.contentful.vault.compiler.lib.TestUtils.*;
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

  @Test public void failsInvalidModels() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("Test", Joiner.on('\n').join(
        "import com.contentful.vault.Space;",
        "@Space(value = \"sid\", models = { Object.class })",
        "class Test {",
        "}"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .failsToCompile()
        .withErrorContaining("Cannot include model (\"java.lang.Object\"), "
            + "is not annotated with @ContentType. (Test)");
  }
}
