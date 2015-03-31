package com.contentful.sqlite.lib;

import com.contentful.sqlite.compiler.Processor;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import org.apache.commons.io.FileUtils;

public class TestUtils {
  public static Iterable<? extends Processor> processors() {
    return Arrays.asList(new Processor());
  }

  public static String readTestResource(String fileName) throws IOException {
    URL resource = TestUtils.class.getClassLoader().getResource(fileName);
    return FileUtils.readFileToString(new File(resource.getPath()));
  }
}
