/*
 * Copyright (C) 2015 Contentful GmbH
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

public final class SyncConfig {
  private final CDAClient client;

  private final boolean invalidate;

  private SyncConfig(Builder builder) {
    this.client = builder.client;
    this.invalidate = builder.invalidate;
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
    private CDAClient client;

    private boolean invalidate;

    private Builder() {
    }

    public Builder setClient(CDAClient client) {
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
