package com.contentful.vaultintegration.lib.escape;

import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;

@ContentType("foo")
public class SqliteEscapeModel extends Resource {
  @Field public String order;
}
