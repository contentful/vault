package com.contentful.sqlite;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.contentful.sqlite.lib.TestUtils.*;
import static com.google.common.truth.Truth.ASSERT;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class FieldTest {
  @Test public void failsDuplicateId() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
        "import com.contentful.sqlite.ContentType;",
        "import com.contentful.sqlite.Field;",
        "import com.contentful.sqlite.Resource;",
        "@ContentType(\"cid\")",
        "class Test extends Resource {",
        "  @Field(\"a\") String thing1;",
        "  @Field(\"a\") String thing2;",
        "}"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .failsToCompile()
        .withErrorContaining(
            "@Field for the same id (\"a\") was used multiple times in the same class. (Test)");
  }

  @Test public void failsInvalidType() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
        "import com.contentful.sqlite.ContentType;",
        "import com.contentful.sqlite.Field;",
        "import com.contentful.sqlite.Resource;",
        "import java.util.Date;",
        "@ContentType(\"cid\")",
        "class Test extends Resource {",
        "  @Field(\"a\") Date thing;",
        "}"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .failsToCompile()
        .withErrorContaining(
            "@Field specified for unsupported type (\"java.util.Date\"). (Test.thing)");
  }
}
