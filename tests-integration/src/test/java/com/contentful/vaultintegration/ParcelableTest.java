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

package com.contentful.vaultintegration;

import android.os.Parcel;
import com.contentful.vault.Asset;
import com.contentful.vault.Vault;
import com.contentful.vaultintegration.lib.demo.DemoSpace;
import java.util.List;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import static com.google.common.truth.Truth.assertThat;

public class ParcelableTest extends BaseTest {
  @Override protected void setupVault() {
    vault = Vault.with(RuntimeEnvironment.application, DemoSpace.class);
  }

  @Test public void asset() throws Exception {
    enqueue("demo/locales.json");
    enqueue("demo/types.json");
    enqueue("demo/initial.json");
    sync();

    List<Asset> assets = vault.fetch(Asset.class).all();
    assertThat(assets).isNotEmpty();

    for (Asset asset : assets) {
      checkParcelable(asset);
    }
  }

  @SuppressWarnings("unchecked")
  private void checkParcelable(Asset source) {
    Parcel parcel = Parcel.obtain();
    source.writeToParcel(parcel, 0);
    parcel.setDataPosition(0);
    Asset reconstructed = Asset.CREATOR.createFromParcel(parcel);
    assertThat(reconstructed).isNotNull();
    assertThat(source.url()).isEqualTo(reconstructed.url());
    assertThat(source.mimeType()).isEqualTo(reconstructed.mimeType());
    assertThat(source.title()).isEqualTo(reconstructed.title());
    assertThat(source.description()).isEqualTo(reconstructed.description());

    assertThat((Iterable<String>) source.file().keySet())
        .containsAllIn(reconstructed.file().keySet());

    assertThat((Iterable) source.file().keySet()).hasSize(reconstructed.file().keySet().size());

    assertThat((Iterable<Object>) source.file().values())
        .containsAllIn(reconstructed.file().values());

    assertThat((Iterable) source.file().values()).hasSize(reconstructed.file().values().size());
  }
}
