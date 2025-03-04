/*
 * Copyright (C) 2018 Contentful GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.contentful.vault;

import com.contentful.java.cda.CDAClient;

import static com.contentful.vault.build.GeneratedBuildParameters.PROJECT_VERSION;
import static java.text.MessageFormat.format;

public final class SyncConfig {
  private final CDAClient client;

  private final boolean invalidate;

  private final Integer limit;

  private final boolean singleLocale;

  SyncConfig(Builder builder) {
    this.client = builder.client;
    this.invalidate = builder.invalidate;
    this.limit = builder.limit;
    this.singleLocale = builder.singleLocale;
  }

  public CDAClient client() {
    return client;
  }

  public boolean shouldInvalidate() {
    return invalidate;
  }

  public Integer getLimit() {
    return limit;
  }

  public boolean isSingleLocale() {
    return singleLocale;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private static final String FIELD_ALREADY_EXISTS = "Do not set {0}, when {1} is already set. " +
            "Use either {0} or a previously added {1}.";

    CDAClient client;
    boolean invalidate;
    Integer limit;
    boolean singleLocale;
    String spaceId;
    String environment;
    String accessToken;

    public Builder setAccessToken(String accessToken) {
      if (client != null) {
        throw new IllegalStateException(format(FIELD_ALREADY_EXISTS, "access token", "client"));
      }
      this.accessToken = accessToken;
      return this;
    }

    public Builder setSpaceId(String spaceId) {
      if (client != null) {
        throw new IllegalStateException(format(FIELD_ALREADY_EXISTS, "space id", "client"));
      }
      this.spaceId = spaceId;
      return this;
    }

    public Builder setEnvironment(String environment) {
      if (client != null) {
        throw new IllegalStateException(format(FIELD_ALREADY_EXISTS, "environment", "client"));
      }
      this.environment = environment;
      return this;
    }

    public Builder setClient(CDAClient client) {
      if (accessToken != null) {
        throw new IllegalStateException(format(FIELD_ALREADY_EXISTS, "client", "access token"));
      }
      if (spaceId != null) {
        throw new IllegalStateException(format(FIELD_ALREADY_EXISTS, "client", "space id"));
      }
      if (environment != null) {
        throw new IllegalStateException(format(FIELD_ALREADY_EXISTS, "client", "environment"));
      }
      this.client = client;
      return this;
    }

    public Builder setInvalidate(boolean invalidate) {
      this.invalidate = invalidate;
      return this;
    }

    public Builder setLimit(Integer limit) {
      this.limit = limit;
      return this;
    }

    public Builder setSingleLocale(boolean singleLocale) {
      this.singleLocale = singleLocale;
      return this;
    }

    public SyncConfig build() {
      if (client == null && (accessToken == null || spaceId == null || environment == null)) {
        throw new IllegalStateException("Either a client or access token, space id, and environment must be set.");
      }
      if (client == null) {
        this.client = CDAClient
                .builder()
                .setToken(accessToken)
                .setSpace(spaceId)
                .setEnvironment(environment)
                .setIntegration("Vault", PROJECT_VERSION)
                .build();
      }
      return new SyncConfig(this);
    }
  }
}