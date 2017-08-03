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

package com.contentful.vault.compiler.lib;

import com.contentful.vault.compiler.Processor;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;

public class TestUtils {
  public static Iterable<? extends Processor> processors() {
    return Arrays.asList(new Processor());
  }

  public static String readTestResource(String fileName) throws IOException {
    URL resource = TestUtils.class.getClassLoader().getResource(fileName);
    return FileUtils.readFileToString(new File(resource.getPath()), Charset.defaultCharset());
  }
}
