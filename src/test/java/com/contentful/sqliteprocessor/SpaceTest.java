package com.contentful.sqliteprocessor;

import com.contentful.sqliteprocessor.lib.TestUtils;
import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.contentful.sqliteprocessor.lib.TestUtils.processors;
import static com.google.common.truth.Truth.ASSERT;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class SpaceTest {
  @Test public void testInjection() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
        "package test;",
        "import com.contentful.sqliteprocessor.DbHelper;",
        "import com.contentful.sqliteprocessor.ContentType;",
        "import com.contentful.sqliteprocessor.Field;",
        "import com.contentful.sqliteprocessor.Space;",
        "import java.util.Arrays;",
        "import java.util.HashSet;",
        "import java.util.Set;",
        "import java.util.Map;",
        "public class Test {",
        "  @ContentType(\"cid\")",
        "  class Model {",
        "    @Field(\"fText\") String fText;",
        "    @Field(\"fBoolean\") Boolean fBoolean;",
        "    @Field(\"fInteger\") Integer fInteger;",
        "    @Field(\"fDouble\") Double fDouble;",
        "    @Field(\"fMap\") Map map;",
        "  }",
        "",
        "  @Space(value = \"sid\", models = { Model.class })",
        "  class Db extends DbHelper {",
        "  }",
        "}"));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/Test$Db$$Space",
        TestUtils.readTestResource("space_injection_java.txt"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test public void failsEmptyId() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n')
        .join("package test;",
            "import com.contentful.sqliteprocessor.Space;",
            "@Space(value = \"\", models = {})",
            "public class Test {",
            "}"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .failsToCompile()
        .withErrorContaining("@Space id may not be empty. (test.Test)");
  }

  @Test public void failsInvalidType() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n')
        .join("package test;",
            "import com.contentful.sqliteprocessor.Space;",
            "@Space(value = \"id\", models = {})",
            "public class Test {",
            "}"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .failsToCompile()
        .withErrorContaining(
            "@Space annotated targets must extend \"com.contentful.sqliteprocessor.DbHelper\". "
                + "(test.Test)");
  }

  @Test public void failsDuplicateId() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
        "package test;",
        "import com.contentful.sqliteprocessor.Space;",
        "import com.contentful.sqliteprocessor.DbHelper;",
        "import java.util.Set;",
        "@Space(value = \"sid\", models = {})",
        "public class Test extends DbHelper {",
        "  @Override public Set<Class<?>> getIncludedModels() { ",
        "    return null;",
        "  }",
        "",
        "  @Space(value = \"sid\", models = {})",
        "  public class Test2 extends DbHelper {",
        "    @Override public Set<Class<?>> getIncludedModels() { ",
        "      return null;",
        "    }",
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
        "import com.contentful.sqliteprocessor.Space;",
        "import com.contentful.sqliteprocessor.DbHelper;",
        "@Space(value = \"sid\", models = { })",
        "public class Test extends DbHelper {",
        "}"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .failsToCompile()
        .withErrorContaining("@Space models must not be empty."
            + " (test.Test)");
  }

  @Test public void failsInvalidModels() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
        "package test;",
        "import com.contentful.sqliteprocessor.Space;",
        "import com.contentful.sqliteprocessor.DbHelper;",
        "@Space(value = \"sid\", models = { Object.class })",
        "public class Test extends DbHelper {",
        "}"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .failsToCompile()
        .withErrorContaining("Cannot include model (\"java.lang.Object\"), "
            + "is not annotated with @ContentType. (test.Test)");
  }
}