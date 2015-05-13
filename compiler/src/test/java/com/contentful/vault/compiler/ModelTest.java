package com.contentful.vault.compiler;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.contentful.vault.compiler.lib.TestUtils.processors;
import static com.contentful.vault.compiler.lib.TestUtils.readTestResource;
import static com.google.common.truth.Truth.ASSERT;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class ModelTest {
  @Test public void testInjection() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("Test", Joiner.on('\n').join(
        "import com.contentful.vault.Asset;",
        "import com.contentful.vault.ContentType;",
        "import com.contentful.vault.Field;",
        "import com.contentful.vault.Resource;",
        "import com.contentful.vault.SpaceHelper;",
        "import java.util.List;",
        "import java.util.Map;",
        "class Test {",
        "  @ContentType(\"cid\")",
        "  static class AwesomeModel extends Resource {",
        "    @Field String textField;",
        "    @Field Boolean booleanField;",
        "    @Field Integer integerField;",
        "    @Field Double doubleField;",
        "    @Field Map mapField;",
        "    @Field Asset assetLink;",
        "    @Field AwesomeModel entryLink;",
        "    @Field List<Asset> arrayOfAssets;",
        "    @Field List<AwesomeModel> arrayOfModels;",
        "    @Field List<String> arrayOfSymbols;",
        "  }",
        "}"
    ));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString(
        "Test$AwesomeModel$$ModelHelper",
        readTestResource("ModelInjection.java"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .compilesWithoutError()
        .and()
        .generatesSources(expectedSource);
  }
}
