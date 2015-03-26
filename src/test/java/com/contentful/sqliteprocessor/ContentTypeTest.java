package com.contentful.sqliteprocessor;

import com.contentful.sqliteprocessor.lib.TestUtils;
import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.contentful.sqliteprocessor.lib.TestUtils.processors;
import static com.google.common.truth.Truth.ASSERT;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class ContentTypeTest {
  @Test public void testInjection() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
        "package test;",
        "import com.contentful.sqliteprocessor.ContentType;",
        "import com.contentful.sqliteprocessor.Field;",
        "import java.util.Map;",
        "@ContentType(\"cid\")",
        "public class Test {",
        "  @Field(\"boolean\") Boolean fBoolean;",
        "  @Field(\"integer\") Integer fInteger;",
        "  @Field(\"double\") Double fDouble;",
        "  @Field(\"text\") String fText;",
        "  @Field(\"map\") Map fMap;",
        "}"));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/Test$$Model",
        TestUtils.readTestResource("content_type_injection_java.txt"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }

  @Test public void failsEmptyId() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test",
        Joiner.on('\n').join(
            "package test;",
            "import com.contentful.sqliteprocessor.ContentType;",
            "@ContentType(\"\")",
            "public class Test {",
            "}"
        ));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .failsToCompile()
        .withErrorContaining("@ContentType id may not be empty. (test.Test)");
  }

  @Test public void failsDuplicateId() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
        "package test;",
        "import com.contentful.sqliteprocessor.ContentType;",
        "@ContentType(\"cid\")",
        "public class Test {",
        "  @ContentType(\"cid\")",
        "  public class Test2 {",
        "  }",
        "}"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .failsToCompile()
        .withErrorContaining(Joiner.on("").join(
            "@ContentTypeInjection for \"cid\" cannot be used on multiple classes. ",
            "(test.Test.Test2)"));
  }

  @Test public void failsInvalidType() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
        "package test;",
        "import com.contentful.sqliteprocessor.ContentType;",
        "import com.contentful.sqliteprocessor.Field;",
        "import java.util.Date;",
        "@ContentType(\"cid\")",
        "public class Test {",
        "  @Field(\"a\") Date thing;",
        "}"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .failsToCompile()
        .withErrorContaining(
            "@Field specified for unsupported type (\"java.util.Date\"). (test.Test.thing)");
  }
}
