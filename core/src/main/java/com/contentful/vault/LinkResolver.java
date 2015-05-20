package com.contentful.vault;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.contentful.java.cda.Constants.CDAResourceType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

final class LinkResolver {
  private final SqliteHelper sqliteHelper;

  private final Query<?> query;

  LinkResolver(SqliteHelper sqliteHelper, Query<?> query) {
    this.sqliteHelper = sqliteHelper;
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
    List<Link> links = fetchLinks(resource.remoteId(), field);
    List<Resource> targets = null;
    if (links.size() > 0) {
      targets = new ArrayList<Resource>();
      for (Link link : links) {
        boolean isEntryLink = link.childContentType() != null;
        Map<String, Resource> cache = isEntryLink ? query.getEntriesCache() : query.getAssetsCache();
        Resource child = cache.get(link.child());
        if (child == null) {
          // Link target not found in cache, fetch from DB
          child = query.fetchResource(link);
          if (child != null) {
            if (isEntryLink) {
              // Resolve links for linked target
              ModelHelper modelHelper =
                  sqliteHelper.getSpaceHelper().getModels().get(child.getClass());

              resolveLinks(child, modelHelper.getFields());
            }

            // Put into cache
            cache.put(child.remoteId(), child);
          }
        }
        if (child != null) {
          targets.add(child);
        }
      }
    }

    Object result = null;
    if (field.isArray()) {
      if (targets == null) {
        result = Collections.emptyList();
      } else {
        result = Collections.unmodifiableList(targets);
      }
    } else if (targets != null && targets.size() > 0) {
      result = targets.get(0);
    }
    if  (result != null) {
      ModelHelper modelHelper = sqliteHelper.getSpaceHelper().getModels().get(resource.getClass());
      modelHelper.setField(resource, field.name(), result);
    }
  }

  private List<Link> fetchLinks(String parent, FieldMeta field) {
    StringBuilder builder = new StringBuilder()
        .append("SELECT `child`, `child_content_type` FROM ")
        .append(SpaceHelper.TABLE_LINKS)
        .append(" WHERE parent = ? AND field = ? AND `child_content_type` IS ");

    boolean linksToAssets = CDAResourceType.Asset.toString().equals(field.linkType()) ||
        Asset.class.getName().equals(field.arrayType());

    if (!linksToAssets) {
      builder.append("NOT ");
    }
    builder.append("NULL;");

    String[] args = new String[] { parent, field.name() };

    List<Link> result;
    SQLiteDatabase db = sqliteHelper.getReadableDatabase();
    Cursor cursor = db.rawQuery(builder.toString(), args);
    result = new ArrayList<Link>();
    try {
      if (cursor.moveToFirst()) {
        do {
          String child = cursor.getString(0);
          String childContentType = cursor.getString(1);
          result.add(new Link(parent, child, field.name(), childContentType));
        } while (cursor.moveToNext());
      }
    } finally {
      cursor.close();
    }
    return result;
  }
}
