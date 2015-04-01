package com.contentful.sqlite;

final class LinkInfo {
  final String parent;
  final String child;
  final String field;
  final String childContentType;

  public LinkInfo(String parent, String child, String field, String childContentType) {
    this.parent = parent;
    this.child = child;
    this.field = field;
    this.childContentType = childContentType;
  }
}
