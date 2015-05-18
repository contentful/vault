/*
 * Copyright (C) 2015 Contentful GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
