package com.contentful.vault;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.contentful.java.cda.Constants.CDAResourceType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class LinkResolver {
  final SpaceHelper spaceHelper;
  final SQLiteDatabase db;
  final Query<?> query;

  LinkResolver(SpaceHelper spaceHelper, Query<?> query) {
    this.spaceHelper = spaceHelper;
    this.db = spaceHelper.getReadableDatabase();
    this.query = query;
  }

  void resolveLinks(Resource resource, List<FieldMeta> links) {
    for (FieldMeta field : links) {
      if (field.isLink()) {
        resolveLinksForField(resource, field);
      } else if (field.isArray() && !field.isArrayOfSymbols()) {
        resolveLinksForField(resource, field);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void resolveLinksForField(Resource resource, FieldMeta field) {
    List<Link> links = fetchLinks(resource.getRemoteId(), field);
    if (links.size() == 0) {
      return;
    }

    List<Resource> targets = new ArrayList<Resource>();
    for (Link link : links) {
      boolean isEntryLink = link.childContentType != null;
      Map<String, Resource> cache = isEntryLink ? query.getEntriesCache() : query.getAssetsCache();
      Resource child = cache.get(link.child);
      if (child == null) {
        // Link target not found in cache, fetch from DB
        child = query.fetchResource(link);
        if (child != null) {
          if (isEntryLink) {
            // Resolve links for linked target
            ModelHelper modelHelper = spaceHelper.getModels().get(child.getClass());
            resolveLinks(child, modelHelper.getFields());
          }

          // Put into cache
          cache.put(child.getRemoteId(), child);
        }
      }
      if (child != null) {
        targets.add(child);
      }
    }

    if (targets.size() > 0) {
      Object result = field.isArray() ? targets : targets.get(0);
      ModelHelper modelHelper = spaceHelper.getModels().get(resource.getClass());
      modelHelper.setField(resource, field.name, result);
    }
  }

  private List<Link> fetchLinks(String parent, FieldMeta field) {
    StringBuilder builder = new StringBuilder()
        .append("SELECT `child`, `child_content_type` FROM ")
        .append(SpaceHelper.TABLE_LINKS)
        .append(" WHERE parent = ? AND field = ? AND `child_content_type` IS ");

    boolean linksToAssets = CDAResourceType.Asset.toString().equals(field.linkType) ||
        Asset.class.getName().equals(field.arrayType);

    if (!linksToAssets) {
      builder.append("NOT ");
    }
    builder.append("NULL;");

    String[] args = new String[]{
        parent,
        field.name
    };

    Cursor cursor = db.rawQuery(builder.toString(), args);
    List<Link> result = new ArrayList<Link>();
    try {
      if (cursor.moveToFirst()) {
        String child = cursor.getString(0);
        String childContentType = cursor.getString(1);
        do {
          result.add(new Link(parent, child, field.name, childContentType));
        } while (cursor.moveToNext());
      }
    } finally {
      cursor.close();
    }
    return result;
  }
}
