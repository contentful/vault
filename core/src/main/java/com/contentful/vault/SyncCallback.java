package com.contentful.vault;

public abstract class SyncCallback {
  private String tag;

  public abstract void onComplete(boolean success);

  final void setTag(String tag) {
    this.tag = tag;
  }

  final String getTag() {
    return tag;
  }
}
