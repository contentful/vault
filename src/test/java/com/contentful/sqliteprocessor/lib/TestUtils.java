package com.contentful.sqliteprocessor.lib;

import com.contentful.sqliteprocessor.ModelProcessor;
import java.util.Arrays;

public class TestUtils {
  public static Iterable<? extends ModelProcessor> processors() {
    return Arrays.asList(
        new ModelProcessor()
    );
  }
}
