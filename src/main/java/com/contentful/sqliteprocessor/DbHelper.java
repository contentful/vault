package com.contentful.sqliteprocessor;

import java.util.Set;

public abstract class DbHelper {
  public abstract Set<Class<?>> getIncludedModels();
}
