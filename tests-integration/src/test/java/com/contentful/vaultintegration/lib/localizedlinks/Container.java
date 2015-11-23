package com.contentful.vaultintegration.lib.localizedlinks;

import com.contentful.vault.Asset;
import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;
import java.util.List;

@ContentType("2Ux73cONSU2w2sCe42Gusy")
public final class Container extends Resource {
  @Field List<Asset> assets;

  @Field Asset one;

  public List<Asset> assets() {
    return assets;
  }

  public Asset one() {
    return one;
  }
}
