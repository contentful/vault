package com.contentful.vaultintegration.lib.arraylinks;

import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;

import java.util.List;

@ContentType("product")
public class Product extends Resource {
  @Field String name;

  @Field List<Shop> shops;

  public String name() {
    return name;
  }

  public List<Shop> shops() {
    return shops;
  }
}
