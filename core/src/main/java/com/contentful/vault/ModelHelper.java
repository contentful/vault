/*
 * Copyright (C) 2015 Contentful GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.contentful.vault;

import android.database.Cursor;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public abstract class ModelHelper<T extends Resource> {
  public abstract String getTableName();

  public abstract List<FieldMeta> getFields();

  public abstract List<String> getCreateStatements();

  public abstract T fromCursor(Cursor cursor);

  protected abstract boolean setField(T resource, String name, Object value);

  protected final <E extends Serializable> E fieldFromBlob(Class<E> clazz, Cursor cursor,
      int columnIndex) {
    byte[] blob = cursor.getBlob(columnIndex);
    if (blob == null || blob.length == 0) {
      return null;
    }
    E result = null;
    Exception exception = null;
    try {
      result = BlobUtils.fromBlob(clazz, blob);
    } catch (IOException e) {
      exception = e;
    } catch (ClassNotFoundException e) {
      exception = e;
    }
    if (exception != null) {
      throw new RuntimeException(String.format("Failed creating BLOB from column %d of %s.",
          columnIndex, getTableName()), exception);
    }
    return result;
  }

  protected final void setContentType(T resource, String type) {
    resource.setContentType(type);
  }
}
