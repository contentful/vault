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

import java.io.Serializable;

public abstract class Resource implements Serializable {
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
