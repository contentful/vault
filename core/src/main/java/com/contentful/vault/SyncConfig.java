package com.contentful.vault;

import com.contentful.java.cda.CDAClient;

public final class SyncConfig {
  final CDAClient client;
  final String locale;

  private SyncConfig(Builder builder) {
    this.client = builder.client;
    this.locale = builder.locale;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private CDAClient client;
    private String locale;

    private Builder() {
    }

    public Builder setClient(CDAClient client) {
      this.client = client;
      return this;
    }

    public Builder setLocale(String locale) {
      this.locale = locale;
      return this;
    }

    public SyncConfig build() {
      return new SyncConfig(this);
    }
  }
}