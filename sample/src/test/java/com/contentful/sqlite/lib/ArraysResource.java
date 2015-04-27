package com.contentful.sqlite.lib;

import com.contentful.sqlite.Asset;
import com.contentful.sqlite.ContentType;
import com.contentful.sqlite.Field;
import com.contentful.sqlite.Resource;
import java.util.List;

@ContentType("4rD4z2Ex0kEOgeWCWG4Se2")
public class ArraysResource extends Resource {
  @Field public List<Asset> assets;
  @Field public List<String> symbols;
  @Field public List<BlobResource> blobs;
}
