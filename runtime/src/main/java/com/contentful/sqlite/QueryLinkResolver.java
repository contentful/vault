package com.contentful.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.contentful.java.cda.Constants.CDAResourceType;
import java.util.List;
import java.util.Map;

final class QueryLinkResolver {
  final PersistenceHelper spaceHelper;
  final SQLiteDatabase db;
  final FutureQuery<?> query;

  QueryLinkResolver(PersistenceHelper spaceHelper, FutureQuery<?> query) {
    this.spaceHelper = spaceHelper;
    this.db = ((SQLiteOpenHelper) spaceHelper).getReadableDatabase();
    this.query = query;
  }

  void resolveLinks(Resource resource, List<FieldMeta> fields) {
    for (FieldMeta field : fields) {
      if (!field.isLink()) {
        continue;
      }
      resolveLinkForField(resource, field);
    }
  }

  private void resolveLinkForField(Resource resource, FieldMeta field) {
    LinkInfo linkInfo = fetchLinkInfo(resource.getRemoteId(), field.name, field.linkType);
    if (linkInfo == null) {
      return;
    }
    boolean isAsset = linkInfo.childContentType == null;
    Map<String, Resource> map = isAsset ? query.getAssetsCache() : query.getEntriesCache();
    Resource target = map.get(linkInfo.child);
    boolean fromCache = false;
    if (target == null) {
      target = query.fetchResource(linkInfo);
    } else {
      fromCache = true;
    }
    if (target != null) {
      if (!fromCache) {
        map.put(target.getRemoteId(), target);
        if (!isAsset) {
          ModelHelper<?> modelHelper = spaceHelper.getModels().get(target.getClass());
          if (modelHelper != null) {
            resolveLinks(target, modelHelper.getFields());
          }
        }
      }
      setFieldValue(resource, field.name, target);
    }
  }

  private void setFieldValue(Resource t, String fieldName, Object value) {
    // TODO via generated code
    try {
      java.lang.reflect.Field f = t.getClass().getDeclaredField(fieldName);
      f.setAccessible(true);
      f.set(t, value);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private LinkInfo fetchLinkInfo(String parent, String field, String type) {
    StringBuilder builder = new StringBuilder()
        .append("SELECT `child`, `child_content_type` FROM ")
        .append(PersistenceHelper.TABLE_LINKS)
        .append(" WHERE parent = ? AND field = ? AND `child_content_type` IS ");

    if (!CDAResourceType.Asset.toString().equals(type)) {
      builder.append("NOT ");
    }
    builder.append("NULL;");

    String[] args = new String[]{
        parent,
        field
    };

    Cursor cursor = db.rawQuery(builder.toString(), args);
    LinkInfo result = null;
    try {
      if (cursor.moveToFirst()) {
        String child = cursor.getString(0);
        String childContentType = cursor.getString(1);
        result = new LinkInfo(parent, child, field, childContentType);
      }
    } finally {
      cursor.close();
    }
    return result;
  }
}
