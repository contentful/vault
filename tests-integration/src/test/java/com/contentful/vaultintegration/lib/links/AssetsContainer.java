package com.contentful.vaultintegration.lib.links;

import com.contentful.vault.Asset;
import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;
import java.util.List;

@ContentType("5QVPSeE41yo28mA4sU6gIo")
public class AssetsContainer extends Resource {
  @Field List<Asset> assets;

  public List<Asset> assets() {
    return assets;
  }
}
