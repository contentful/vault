package com.contentful.vault.compiler;

public class ModelTest {
  /*
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
  */
}
