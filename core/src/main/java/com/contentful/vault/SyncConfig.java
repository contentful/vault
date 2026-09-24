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
  /** Environment used when only an access token and a space id are set (the CDA default). */
  public static final String DEFAULT_ENVIRONMENT = "master";

  /** Smallest page size accepted by {@link Builder#setLimit(Integer)}. */
  public static final int MIN_LIMIT = 1;

  /** Largest page size accepted by the Sync API and {@link Builder#setLimit(Integer)}. */
  public static final int MAX_LIMIT = 1000;

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

    /**
     * Sets the page size of the <em>initial</em> sync (the {@code limit} parameter of the Sync API).
     * Later pages and subsequent syncs use the API default.
     *
     * @param limit between {@value #MIN_LIMIT} and {@value #MAX_LIMIT}, or {@code null} for the API
     *              default.
     * @throws IllegalArgumentException if {@code limit} is out of range.
     */
    public Builder setLimit(Integer limit) {
      if (limit != null && (limit < MIN_LIMIT || limit > MAX_LIMIT)) {
        throw new IllegalArgumentException(
            String.format("Sync limit must be between %d and %d, but was %d.",
                MIN_LIMIT, MAX_LIMIT, limit));
      }
      this.limit = limit;
      return this;
    }

    /**
     * Stores only the space's default locale. Queries for any other locale return no results.
     * <p>
     * Changing this value for an existing database triggers a full re-sync on the next
     * {@link Vault#requestSync(SyncConfig)}, so stored data always matches the mode.
     */
    public Builder setSingleLocale(boolean singleLocale) {
      this.singleLocale = singleLocale;
      return this;
    }

    public SyncConfig build() {
      if (client == null && (accessToken == null || spaceId == null)) {
        throw new IllegalStateException(
            "Either a client, or an access token and a space id must be set.");
      }
      if (client == null) {
        this.client = CDAClient
                .builder()
                .setToken(accessToken)
                .setSpace(spaceId)
                // Same default as the CDA SDK when no environment is given.
                .setEnvironment(environment == null ? DEFAULT_ENVIRONMENT : environment)
                .setIntegration("Vault", PROJECT_VERSION)
                .build();
      }
      return new SyncConfig(this);
    }
  }
}
