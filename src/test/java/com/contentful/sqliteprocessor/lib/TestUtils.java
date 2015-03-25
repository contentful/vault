package com.contentful.sqliteprocessor.lib;

import com.contentful.sqliteprocessor.ModelProcessor;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.apache.commons.io.FileUtils;

public class TestUtils {
  public static Iterable<? extends ModelProcessor> processors() {
    return Arrays.asList(new ModelProcessor());
  }

  public static String readTestResource(String fileName) throws IOException {
    return FileUtils.readFileToString(new File("src/test/resources/" + fileName), "UTF-8");
  }
}
