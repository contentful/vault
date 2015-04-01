package com.contentful.sqlite;

final class LinkInfo {
  final String parent;
  final String child;
  final String field;
  final String childName;

  public LinkInfo(String parent, String child, String field, String childName) {
    this.parent = parent;
    this.child = child;
    this.field = field;
    this.childName = childName;
  }
}
