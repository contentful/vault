package com.contentful.sqlite;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.contentful.sqlite.lib.TestUtils.*;
import static com.google.common.truth.Truth.ASSERT;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class SpaceTest {
  @Test public void testInjection() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
        "import com.contentful.sqlite.Asset;",
        "import com.contentful.sqlite.ContentType;",
        "import com.contentful.sqlite.Field;",
        "import com.contentful.sqlite.Resource;",
        "import com.contentful.sqlite.Space;",
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
        "test/Test$AwesomeSpace$$SpaceHelper",
        readTestResource("SpaceInjection.java"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test public void failsEmptyId() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n')
        .join("package test;",
            "import com.contentful.sqlite.Space;",
            "@Space(value = \"\", models = {})",
            "public class Test {",
            "}"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .failsToCompile()
        .withErrorContaining("@Space id may not be empty. (test.Test)");
  }

  @Test public void failsDuplicateId() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
        "package test;",
        "import com.contentful.sqlite.Space;",
        "import com.contentful.sqlite.SpaceHelper;",
        "import java.util.Set;",
        "public class Test {",
        "  @Space(value = \"sid\", models = { })",
        "  public class Test1 {",
        "  }",
        "  @Space(value = \"sid\", models = { })",
        "  public class Test2 {",
        "  }",
        "}"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .failsToCompile()
        .withErrorContaining("@Space for \"sid\" cannot be used on multiple classes."
            + " (test.Test.Test2)");
  }

  @Test public void failsEmptyModels() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
        "package test;",
        "import com.contentful.sqlite.Space;",
        "@Space(value = \"sid\", models = { })",
        "public class Test {",
        "}"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .failsToCompile()
        .withErrorContaining("@Space models must not be empty. (test.Test)");
  }

  @Test public void failsInvalidModels() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
        "package test;",
        "import com.contentful.sqlite.Space;",
        "@Space(value = \"sid\", models = { Object.class })",
        "public class Test {",
        "}"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .failsToCompile()
        .withErrorContaining("Cannot include model (\"java.lang.Object\"), "
            + "is not annotated with @ContentType. (test.Test)");
  }
}
