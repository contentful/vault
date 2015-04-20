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
        "package test;",
        "import com.contentful.sqlite.Asset;",
        "import com.contentful.sqlite.DbHelper;",
        "import com.contentful.sqlite.ContentType;",
        "import com.contentful.sqlite.Field;",
        "import com.contentful.sqlite.Space;",
        "import com.contentful.sqlite.Resource;",
        "import java.util.Arrays;",
        "import java.util.HashSet;",
        "import java.util.Set;",
        "import java.util.Map;",
        "public class Test {",
        "  @ContentType(\"cid\")",
        "  class Model extends Resource {",
        "    @Field(\"id1\") String fText;",
        "    @Field(\"id2\") Boolean fBoolean;",
        "    @Field(\"id3\") Integer fInteger;",
        "    @Field(\"id4\") Double fDouble;",
        "    @Field(\"id5\") Map fMap;",
        "    @Field(value = \"id6\", link = true) Model fLinkedModel;",
        "    @Field(value = \"id7\", link = true) Asset fLinkedAsset;",
        "  }",
        "",
        "  @Space(value = \"sid\", models = { Model.class })",
        "  class Db {",
        "  }",
        "}"));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/Test$Db$$Space",
        readTestResource("space_injection_java.txt"));

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
        "import com.contentful.sqlite.DbHelper;",
        "import java.util.Set;",
        "@Space(value = \"sid\", models = {})",
        "public class Test {",
        "  @Override public Set<Class<?>> getIncludedModels() { ",
        "    return null;",
        "  }",
        "",
        "  @Space(value = \"sid\", models = {})",
        "  public class Test2 {",
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
        "import com.contentful.sqlite.Space;",
        "import com.contentful.sqlite.DbHelper;",
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
        "import com.contentful.sqlite.DbHelper;",
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
