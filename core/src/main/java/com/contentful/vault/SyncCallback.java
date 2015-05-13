package com.contentful.vault;

public abstract class SyncCallback {
  private boolean cancel;

  public abstract void onComplete(boolean success);

  boolean isCancelled() {
    return cancel;
  }

  public void cancel() {
    this.cancel = true;
  }
}
