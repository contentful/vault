package com.contentful.vault;

final class Link {
  final String parent;
  final String child;
  final String field;
  final String childContentType;

  public Link(String parent, String child, String field, String childContentType) {
    this.parent = parent;
    this.child = child;
    this.field = field;
    this.childContentType = childContentType;
  }
}
