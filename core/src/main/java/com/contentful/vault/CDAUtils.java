package com.contentful.vault;

import com.contentful.java.cda.Constants.CDAResourceType;
import com.contentful.java.cda.model.CDAResource;
import java.util.Map;

import static com.contentful.java.cda.Constants.CDAResourceType.DeletedAsset;
import static com.contentful.java.cda.Constants.CDAResourceType.DeletedEntry;

final class CDAUtils {
  private CDAUtils() {
    throw new AssertionError();
  }

  static boolean isOfType(CDAResource resource, CDAResourceType resourceType) {
    return resourceType.equals(CDAResourceType.valueOf(extractResourceType(resource)));
  }

  static String extractResourceType(CDAResource resource) {
    return (String) resource.getSys().get("type");
  }

  static String extractResourceId(CDAResource resource) {
    return (String) resource.getSys().get("id");
  }

  static String extractContentTypeId(CDAResource entry) {
    Map contentType = (Map) entry.getSys().get("contentType");
    if (contentType != null) {
      return (String) ((Map) contentType.get("sys")).get("id");
    }
    return null;
  }

  static boolean wasDeleted(CDAResource resource) {
    CDAResourceType resourceType = CDAResourceType.valueOf(extractResourceType(resource));
    return DeletedAsset.equals(resourceType) || DeletedEntry.equals(resourceType);
  }
}
