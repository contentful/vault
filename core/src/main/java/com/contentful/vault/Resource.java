package com.contentful.vault;

public abstract class Resource {
  private String remoteId;

  private String createdAt;

  private String updatedAt;

  public String remoteId() {
    return remoteId;
  }

  public String createdAt() {
    return createdAt;
  }

  public String updatedAt() {
    return updatedAt;
  }

  void setRemoteId(String remoteId) {
    this.remoteId = remoteId;
  }

  void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  void setUpdatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
  }
}
