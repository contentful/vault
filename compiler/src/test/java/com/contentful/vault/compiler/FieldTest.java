package com.contentful.vault.compiler;

public class FieldTest {
  /*
  @Test public void testListTypes() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
        "import com.contentful.vault.Asset;",
        "import com.contentful.vault.ContentType;",
        "import com.contentful.vault.Field;",
        "import com.contentful.vault.Resource;",
        "import java.util.List;",
        "@ContentType(\"cid\")",
        "class Test extends Resource {",
        "  @Field List<String> listOfStrings;",
        "  @Field List<Asset> listOfAssets;",
        "}"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .compilesWithoutError();
  }

  @Test public void failsDuplicateId() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
        "import com.contentful.vault.ContentType;",
        "import com.contentful.vault.Field;",
        "import com.contentful.vault.Resource;",
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
        "import com.contentful.vault.ContentType;",
        "import com.contentful.vault.Field;",
        "import com.contentful.vault.Resource;",
        "import java.util.Date;",
        "@ContentType(\"cid\")",
        "class Test extends Resource {",
        "  @Field Date thing;",
        "}"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .failsToCompile()
        .withErrorContaining(
            "@Field specified for unsupported type (\"java.util.Date\"). (Test.thing)");
  }

  @Test public void failsUntypedList() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
        "import com.contentful.vault.ContentType;",
        "import com.contentful.vault.Field;",
        "import com.contentful.vault.Resource;",
        "import java.util.List;",
        "@ContentType(\"cid\")",
        "class Test extends Resource {",
        "  @Field List list;",
        "}"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .failsToCompile()
        .withErrorContaining(
            "Array fields must have a type parameter specified. (Test.list)");
  }

  @Test public void testInvalidListType() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
        "import com.contentful.vault.ContentType;",
        "import com.contentful.vault.Field;",
        "import com.contentful.vault.Resource;",
        "import java.util.List;",
        "@ContentType(\"cid\")",
        "class Test extends Resource {",
        "  @Field List<Integer> list;",
        "}"));

    ASSERT.about(javaSource()).that(source)
        .processedWith(processors())
        .failsToCompile()
        .withErrorContaining(
            "Invalid list type \"java.lang.Integer\" specified. (Test.list)");
  }
  */
}
