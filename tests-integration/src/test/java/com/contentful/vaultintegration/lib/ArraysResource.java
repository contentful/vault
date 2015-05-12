package com.contentful.vaultintegration.lib;

import com.contentful.vault.Asset;
import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;
import java.util.List;

@ContentType("4rD4z2Ex0kEOgeWCWG4Se2")
public class ArraysResource extends Resource {
  @Field public List<Asset> assets;
  @Field public List<String> symbols;
  @Field public List<BlobResource> blobs;
}
