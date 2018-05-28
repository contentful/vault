/*
 * Copyright (C) 2018 Contentful GmbH
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

final class Link {
  private final String parent;

  private final String child;

  private final String fieldId;

  private final String childContentType;

  Link(String parent, String child, String fieldId, String childContentType) {
    this.parent = parent;
    this.child = child;
    this.fieldId = fieldId;
    this.childContentType = childContentType;
  }

  public String parent() {
    return parent;
  }

  public String child() {
    return child;
  }

  public String fieldId() {
    return fieldId;
  }

  public String childContentType() {
    return childContentType;
  }

  public boolean isAsset() {
    return childContentType == null;
  }
}
