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
import java.util.HashMap;
import java.util.Map;

import static com.contentful.java.cda.CDAType.ASSET;

public final class Asset extends Resource implements Parcelable {
  private String url;

  private String mimeType;

  private String title;

  private String description;

  private HashMap<String, Object> file;

  private Asset(Builder builder) {
    this.url = builder.url;
    this.mimeType = builder.mimeType;
    this.title = builder.title;
    this.description = builder.description;
    this.file = builder.file;
  }

  public String url() {
    return url;
  }

  public String mimeType() {
    return mimeType;
  }

  public String title() {
    return title;
  }

  public String description() {
    return description;
  }

  public Map<String, Object> file() {
    return file;
  }

  @Override String getIdPrefix() {
    return ASSET.toString();
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {
    private Builder() {
    }

    private String url;
    private String mimeType;
    private String title;
    private String description;
    private HashMap<String, Object> file;

    public Builder setUrl(String url) {
      this.url = url;
      return this;
    }

    public Builder setMimeType(String mimeType) {
      this.mimeType = mimeType;
      return this;
    }

    public Builder setTitle(String title) {
      this.title = title;
      return this;
    }

    public Builder setDescription(String description) {
      this.description = description;
      return this;
    }

    public Builder setFile(HashMap<String, Object> file) {
      this.file = file;
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

    writeOptionalString(out, updatedAt());

    out.writeString(url);

    out.writeString(mimeType);

    writeOptionalString(out, title());

    writeOptionalString(out, description());

    if (file == null) {
      out.writeInt(-1);
    } else {
      out.writeInt(1);
      out.writeSerializable(file);
    }
  }

  private void writeOptionalString(Parcel out, String s) {
    if (s == null) {
      out.writeInt(-1);
    } else {
      out.writeInt(1);
      out.writeString(s);
    }
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

  @SuppressWarnings("unchecked")
  private Asset(Parcel in) {
    setRemoteId(in.readString());

    setCreatedAt(in.readString());

    if (in.readInt() != -1) {
      setUpdatedAt(in.readString());
    }

    this.url = in.readString();

    this.mimeType = in.readString();

    if (in.readInt() != -1) {
      this.title = in.readString();
    }

    if (in.readInt() != -1) {
      this.description = in.readString();
    }

    if (in.readInt() != -1) {
      this.file = (HashMap<String, Object>) in.readSerializable();
    }
  }

  public static final class Fields extends BaseFields {
    public static final String URL = "url";

    public static final String MIME_TYPE = "mime_type";

    public static final String TITLE = "title";

    public static final String DESCRIPTION = "description";

    public static final String FILE = "file";
  }
}
