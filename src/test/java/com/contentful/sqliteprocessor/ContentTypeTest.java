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
            "@ContentType(id = \"cid\")",
            "public class Test {",
            "}"
        ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/Test$Sqlite",
        Joiner.on('\n').join(
            "package test;",
            "class Test$Sqlite {}"
        ));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }
}
