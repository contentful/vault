package com.contentful.vault;

public class SyncException extends RuntimeException {
  public SyncException(String message) {
    super(message);
  }

  public SyncException(String message, Throwable cause) {
    super(message, cause);
  }

  public SyncException(Throwable cause) {
    super(cause);
  }
}
