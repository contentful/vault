package com.contentful.vault;

public final class Asset extends Resource {
  private String url;

  private String mimeType;

  private Asset(Builder builder) {
    this.url = builder.url;
    this.mimeType = builder.mimeType;
  }

  public String url() {
    return url;
  }

  public String mimeType() {
    return mimeType;
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {
    private Builder() {
    }

    private String url;

    private String mimeType;

    public Builder setUrl(String url) {
      this.url = url;
      return this;
    }

    public Builder setMimeType(String mimeType) {
      this.mimeType = mimeType;
      return this;
    }

    public Asset build() {
      return new Asset(this);
    }
  }
}
