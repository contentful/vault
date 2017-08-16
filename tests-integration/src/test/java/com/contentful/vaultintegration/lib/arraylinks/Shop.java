package com.contentful.vaultintegration.lib.arraylinks;

import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;

import java.util.List;

@ContentType("shop")
public class Shop extends Resource {
  @Field String name;

  @Field List<Product> products;

  public String name() {
    return name;
  }

  public List<Product> products() {
    return products;
  }
}
