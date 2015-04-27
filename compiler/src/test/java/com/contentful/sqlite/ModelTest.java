package com.contentful.sqlite;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import org.junit.Test;

import static com.contentful.sqlite.lib.TestUtils.processors;
import static com.contentful.sqlite.lib.TestUtils.readTestResource;
import static com.google.common.truth.Truth.ASSERT;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class ModelTest {
  @Test public void testInjection() throws Exception {
    JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n')
        .join(
            "import com.contentful.sqlite.Asset;",
            "import com.contentful.sqlite.ContentType;",
            "import com.contentful.sqlite.Field;",
            "import com.contentful.sqlite.Resource;",
            "import com.contentful.sqlite.SpaceHelper;",
            "import java.util.Map;",
            "public class Test {",
            "  @ContentType(\"cid\")",
            "  class AwesomeModel extends Resource {",
            "    @Field String fText;",
            "    @Field Boolean fBoolean;",
            "    @Field Integer fInteger;",
            "    @Field Double fDouble;",
            "    @Field Map fMap;",
            "    @Field AwesomeModel fLinkedModel;",
            "    @Field Asset fLinkedAsset;",
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
