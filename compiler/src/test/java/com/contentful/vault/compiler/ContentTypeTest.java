package com.contentful.vault.compiler;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.contentful.vault.compiler.lib.TestUtils.processors;
import static com.google.common.truth.Truth.ASSERT;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class ContentTypeTest {
  @Test public void failsEmptyId() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("Test",
        Joiner.on('\n').join(
            "import com.contentful.vault.ContentType;",
            "import com.contentful.vault.Resource;",
            "@ContentType(\"\")",
            "class Test extends Resource {",
            "}"
        ));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .failsToCompile()
        .withErrorContaining("@ContentType id may not be empty. (Test)");
  }

  /*
  TODO fail only for the same space
  @Test public void failsDuplicateId() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
        "import com.contentful.vault.ContentType;",
        "import com.contentful.vault.Resource;",
        "class Test {",
        "  @ContentType(\"cid\")",
        "  static class Test1 extends Resource { }",
        "  @ContentType(\"cid\")",
        "  static class Test2 extends Resource { }",
        "}"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .failsToCompile()
        .withErrorContaining(
            "@ContentType for \"cid\" cannot be used on multiple classes."
                + " (Test.Test2)");
  }
  */

  @Test public void failsInvalidType() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("Test", Joiner.on('\n').join(
        "import com.contentful.vault.ContentType;",
        "import java.util.Date;",
        "@ContentType(\"cid\")",
        "class Test {",
        "}"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .failsToCompile()
        .withErrorContaining(
            "Classes annotated with @ContentType must extend \"com.contentful.vault.Resource\". "
                + "(Test)");
  }
}
