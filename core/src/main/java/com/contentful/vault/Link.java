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
