package com.contentful.vaultintegration.lib.localizedlinks;

import com.contentful.vault.Asset;
import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;

@ContentType("2MciU8KpbOsieeoIUYQkse")
public final class Container extends Resource {
  @Field Asset image;

  public Asset image() {
    return image;
  }
}
