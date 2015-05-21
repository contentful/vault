package com.contentful.vault;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.contentful.java.cda.Constants.CDAResourceType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

final class LinkResolver {
  private static final String LINKS_WHERE_CLAUSE = "l.parent = ? AND l.is_asset = ?";

  private static final String QUERY_ASSET_LINKS = String.format(
      "SELECT l.child, null FROM %s l WHERE %s", SpaceHelper.TABLE_LINKS, LINKS_WHERE_CLAUSE);

  private static final String QUERY_ENTRY_LINKS = String.format(
      "SELECT l.child, t.type_id FROM %s l INNER JOIN %s t ON l.child = t.remote_id WHERE %s",
      SpaceHelper.TABLE_LINKS, SpaceHelper.TABLE_ENTRY_TYPES, LINKS_WHERE_CLAUSE);

  private static final String QUERY_ENTRY_TYPE = String.format(
      "SELECT `type_id` FROM %s WHERE remote_id = ?", SpaceHelper.TABLE_ENTRY_TYPES);

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

  private List<Link> fetchLinks(String parentId, FieldMeta field) {
    List<Link> result;

    boolean linksToAssets = CDAResourceType.Asset.toString().equals(field.linkType()) ||
        Asset.class.getName().equals(field.arrayType());

    String query = linksToAssets ? QUERY_ASSET_LINKS : QUERY_ENTRY_LINKS;

    String[] args = new String[] {
        parentId,
        linksToAssets ? "1" : "0"
    };

    SQLiteDatabase db = sqliteHelper.getReadableDatabase();
    Cursor cursor = db.rawQuery(query, args);
    try {
      result = new ArrayList<Link>();
      if (cursor.moveToFirst()) {
        do {
          String childId = cursor.getString(0);
          String childContentType = cursor.getString(1);
          result.add(new Link(parentId, childId, field.name(), childContentType));
        } while (cursor.moveToNext());
      }
    } finally {
      cursor.close();
    }
    return result;
  }

  public static String fetchEntryType(SQLiteDatabase db, String remoteId) {
    String args[] = new String[]{ remoteId };
    String result = null;
    Cursor cursor = db.rawQuery(QUERY_ENTRY_TYPE, args);
    try {
      if (cursor.moveToFirst()) {
        result = cursor.getString(0);
      }
    } finally {
      cursor.close();
    }
    return result;
  }
}
