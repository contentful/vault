package com.contentful.vault;

final class Link {
  private final String parent;

  private final String child;

  private final String field;

  private final String childContentType;

  Link(String parent, String child, String field, String childContentType) {
    this.parent = parent;
    this.child = child;
    this.field = field;
    this.childContentType = childContentType;
  }

  public String parent() {
    return parent;
  }

  public String child() {
    return child;
  }

  public String field() {
    return field;
  }

  public String childContentType() {
    return childContentType;
  }
}
