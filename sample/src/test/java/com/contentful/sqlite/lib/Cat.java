package com.contentful.sqlite.lib;

import com.contentful.sqlite.Asset;
import com.contentful.sqlite.ContentType;
import com.contentful.sqlite.Field;
import com.contentful.sqlite.Resource;

@ContentType("cat")
public class Cat extends Resource {
  @Field public String name;
  @Field public Cat bestFriend;
  @Field public Asset image;
}
