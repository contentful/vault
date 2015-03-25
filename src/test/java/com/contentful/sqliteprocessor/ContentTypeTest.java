package com.contentful.sqliteprocessor;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.contentful.sqliteprocessor.lib.TestUtils.processors;
import static com.google.common.truth.Truth.ASSERT;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class ContentTypeTest {
  @Test public void testContentTypeInjection() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test",
        Joiner.on('\n').join(
            "package test;",
            "import com.contentful.sqliteprocessor.ContentType;",
            "import com.contentful.sqliteprocessor.Field;",
            "@ContentType(\"cid\")",
            "public class Test {",
            "  @Field(\"fid\") String text;",
            "}"
        ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/Test$Sqlite",
        Joiner.on('\n').join(
            "package test;",
            "class Test$Sqlite {}"
        ));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .compilesWithoutError();
        // TODO
        //.and()
        //.generatesSources(expectedSource);
  }

  @Test public void failsContentTypeWithoutId() throws Exception {
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

  @Test public void failsFieldWithoutId() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test",
        Joiner.on('\n').join(
            "package test;",
            "import com.contentful.sqliteprocessor.ContentType;",
            "import com.contentful.sqliteprocessor.Field;",
            "@ContentType(\"cid\")",
            "public class Test {",
            "  @Field(\"\") String text;",
            "}"
        ));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .failsToCompile()
        .withErrorContaining("@Field id may not be empty. (test.Test.text)");
  }

  @Test public void failsWithSameId() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test",
        Joiner.on('\n').join(
            "package test;",
            "import com.contentful.sqliteprocessor.ContentType;",
            "@ContentType(\"cid\")",
            "public class Test {",
            "  @ContentType(\"cid\")",
            "  public class Test2 {",
            "  }",
            "}"
        ));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .failsToCompile()
        .withErrorContaining(Joiner.on("").join(
            "@ContentTypeInjection for \"cid\" cannot be used on multiple classes. ",
            "(test.Test.Test2)"));
  }
}
