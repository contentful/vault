package com.contentful.sqliteprocessor;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.contentful.sqliteprocessor.lib.TestUtils.processors;
import static com.google.common.truth.Truth.ASSERT;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class SpaceTest {
  @Test public void failsEmptyId() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n')
        .join("package test;",
            "import com.contentful.sqliteprocessor.Space;",
            "@Space(\"\")",
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
            "@Space(\"id\")",
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
        "@Space(\"sid\")",
        "public class Test extends DbHelper {",
        "  @Override public Set<Class<?>> getIncludedModels() { ",
        "    return null;",
        "  }",
        "",
        "  @Space(\"sid\")",
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
}
