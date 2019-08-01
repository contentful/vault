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

public final class SyncConfig {
  private final CDAClient client;

  private final boolean invalidate;

  SyncConfig(Builder builder) {
    this.invalidate = builder.invalidate;

    if (builder.client == null) {
      if (builder.accessToken == null) {
        throw new IllegalStateException("Cannot create a CDA client with no access token. " +
            "Please set it.");
      }

      if (builder.spaceId == null) {
        throw new IllegalStateException("Cannot create a CDA client with no space id. " +
            "Please set it.");
      }

      this.client = CDAClient
          .builder()
          .setToken(builder.accessToken)
          .setSpace(builder.spaceId)
          .setEnvironment(builder.environment)
          .setIntegration("Vault", PROJECT_VERSION)
          .build();
    } else {
      this.client = builder.client;
    }
  }

  public CDAClient client() {
    return client;
  }

  public boolean shouldInvalidate() {
    return invalidate;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    CDAClient client;
    boolean invalidate;
    String accessToken;
    String spaceId;
    String environment;

    public Builder setAccessToken(String accessToken) {
      if (client != null) {
        throw new IllegalStateException(
            "Do not set an access token, when a client is already set. Use either space id and " +
                "a token or a previously created client."
        );
      }
      this.accessToken = accessToken;
      return this;
    }

    public Builder setSpaceId(String spaceId) {
      if (client != null) {
        throw new IllegalStateException(
            "Do not set a space id, when a client is already set. Use either space id and " +
                "a token or a previously created client."
        );
      }
      this.spaceId = spaceId;
      return this;
    }

    public Builder setEnvironment(String environment) {
      if (client != null) {
        throw new IllegalStateException(
            "Do not set an environment, when a client is already set. User either environment " +
                "or a previously created client."
        );
      }
      this.environment = environment;
      return this;
    }

    public Builder setClient(CDAClient client) {
      if (accessToken != null) {
        throw new IllegalStateException(
            "Do not set a client, when an access token is already set. Use either space id and " +
                "a token or a previously created client."
        );
      }
      if (spaceId != null) {
        throw new IllegalStateException(
            "Do not set a client, when a space id is already set. Use either space id and " +
                "a token or a previously created client."
        );
      }

      if (environment != null) {
        throw new IllegalStateException(
            "Do not set a client, when an environment is already set. User either environment " +
                "or a previously created client."
        );
      }

      this.client = client;
      return this;
    }

    public Builder setInvalidate(boolean invalidate) {
      this.invalidate = invalidate;
      return this;
    }

    public SyncConfig build() {
      return new SyncConfig(this);
    }
  }
}
