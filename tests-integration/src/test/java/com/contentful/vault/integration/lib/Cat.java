package com.contentful.vault.integration.lib;

import com.contentful.vault.Asset;
import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;

@ContentType("cat")
public class Cat extends Resource {
  @Field public String name;
  @Field public Cat bestFriend;
  @Field public Asset image;
}
