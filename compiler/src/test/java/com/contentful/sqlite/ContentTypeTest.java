package com.contentful.sqlite;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.contentful.sqlite.lib.TestUtils.processors;
import static com.google.common.truth.Truth.ASSERT;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class ContentTypeTest {
  @Test public void failsEmptyId() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test",
        Joiner.on('\n').join(
            "package test;",
            "import com.contentful.sqlite.ContentType;",
            "import com.contentful.sqlite.Resource;",
            "@ContentType(\"\")",
            "public class Test extends Resource {",
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
        "import com.contentful.sqlite.ContentType;",
        "@ContentType(\"cid\")",
        "public class Test {",
        "  @ContentType(\"cid\")",
        "  public class Test2 {",
        "  }",
        "}"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .failsToCompile()
        .withErrorContaining(
            "@ContentType for \"cid\" cannot be used on multiple classes."
                + " (test.Test.Test2)");
  }

  @Test public void failsInvalidType() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
        "package test;",
        "import com.contentful.sqlite.ContentType;",
        "import java.util.Date;",
        "@ContentType(\"cid\")",
        "public class Test {",
        "}"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .failsToCompile()
        .withErrorContaining(
            "Classes annotated with @ContentType must extend \"com.contentful.sqlite.Resource\". "
                + "(test.Test)");
  }
}
