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

import android.os.Parcel;
import android.os.Parcelable;
import com.contentful.java.cda.Constants.CDAResourceType;

public final class Asset extends Resource implements Parcelable {
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

  @Override String getIdPrefix() {
    return CDAResourceType.Asset.toString();
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

  // Parcelable
  public int describeContents() {
    return 0;
  }

  public void writeToParcel(Parcel out, int flags) {
    out.writeString(remoteId());
    out.writeString(createdAt());
    out.writeString(updatedAt());
    out.writeString(url);
    out.writeString(mimeType);
  }

  public static final Parcelable.Creator<Asset> CREATOR
      = new Parcelable.Creator<Asset>() {
    public Asset createFromParcel(Parcel in) {
      return new Asset(in);
    }

    public Asset[] newArray(int size) {
      return new Asset[size];
    }
  };

  private Asset(Parcel in) {
    setRemoteId(in.readString());
    setCreatedAt(in.readString());
    setUpdatedAt(in.readString());
    this.url = in.readString();
    this.mimeType = in.readString();
  }
}
