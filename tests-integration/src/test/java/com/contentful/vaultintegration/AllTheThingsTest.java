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

import com.contentful.vault.Asset;
import com.contentful.vault.Vault;
import com.contentful.vaultintegration.lib.allthethings.AllTheThingsResource;
import com.contentful.vaultintegration.lib.allthethings.AllTheThingsSpace;

import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static com.contentful.vault.BaseFields.REMOTE_ID;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

@Config(manifest = "src/main/AndroidManifest.xml", sdk = 23)
public class AllTheThingsTest extends BaseTest {
  @Override protected void setupVault() {
    vault = Vault.with(RuntimeEnvironment.application, AllTheThingsSpace.class);
  }

  @Test public void testAllTheThings() throws Exception {
    enqueueSync("allthethings");
    sync();
    checkEntryFoo();
    checkEntryBar();
  }

  @SuppressWarnings("unchecked")
  private void checkEntryBar() {
    AllTheThingsResource bar = vault.fetch(AllTheThingsResource.class)
        .where(REMOTE_ID + " = ?", "4EYJjgMg0oG084iuACY6Ue")
        .first();

    assertThat(bar).isNotNull();
    assertThat(bar.text()).isEqualTo("bar");
    assertThat(bar.number()).isEqualTo(1);
    assertThat(bar.decimal()).isEqualTo(2.0);
    assertThat(bar.yesno()).isFalse();
    assertThat(bar.dateTime()).isNull();
    assertThat(bar.location()).isNull();
    assertThat(bar.entry()).isNull();
    assertThat(bar.asset()).isNull();
    assertThat(bar.object()).isNull();

    assertThat(bar.entries()).isNotNull();
    assertThat(bar.entries()).isEmpty();

    assertThat(bar.assets()).isNotNull();
    assertThat(bar.assets()).isEmpty();

    assertThat(bar.symbols()).isNotNull();
    assertThat(bar.symbols()).isEmpty();
  }

  @SuppressWarnings("unchecked")
  private void checkEntryFoo() {
    AllTheThingsResource foo = vault.fetch(AllTheThingsResource.class)
        .where(REMOTE_ID + " = ?", "6K1Md1qADuOsoom2UIEKkq")
        .first();

    assertThat(foo).isNotNull();
    assertThat(foo.text()).isEqualTo("foo");
    assertThat(foo.number()).isEqualTo(31337);
    assertThat(foo.decimal()).isEqualTo(1.337);
    assertThat(foo.yesno()).isTrue();
    assertThat(foo.dateTime()).isEqualTo("2015-07-01");

    assertThat(foo.location()).isNotNull();
    assertThat(foo.location().get("lat")).isEqualTo(52.52000659999999);
    assertThat(foo.location().get("lon")).isEqualTo(13.404953999999975);

    assertThat(foo.entry()).isNotNull();
    assertThat(foo.entry().remoteId()).isEqualTo("4EYJjgMg0oG084iuACY6Ue");

    assertThat(foo.asset()).isNotNull();
    assertThat(foo.asset().remoteId()).isEqualTo("6Kknr7SqpGWs8kIKwCuQCU");

    assertThat(foo.object()).isNotNull();
    assertThat(foo.object()).hasSize(2);
    assertThat(foo.object()).containsEntry("foo", "bar");
    assertThat(foo.object()).containsEntry("bar", "foo");

    assertThat(foo.entries()).isNotNull();
    assertThat(foo.entries()).hasSize(1);
    assertThat(foo.entries().get(0).remoteId()).isEqualTo("4EYJjgMg0oG084iuACY6Ue");

    assertThat(foo.assets()).isNotNull();
    assertThat(foo.assets()).hasSize(2);
    boolean hasFoo = false, hasBar = false;
    for (Asset asset : foo.assets()) {
      if ("6Kknr7SqpGWs8kIKwCuQCU".equals(asset.remoteId())) {
        hasFoo = true;
      } else if ("w4ZPwQ0Cmko2QqyWgSEGy".equals(asset.remoteId())) {
        hasBar = true;
      } else {
        fail();
      }
    }
    assertThat(hasFoo).isTrue();
    assertThat(hasBar).isTrue();

    assertThat(foo.symbols()).isNotNull();
    assertThat(foo.symbols()).hasSize(2);
    assertThat(foo.symbols()).containsExactly("foo", "bar");
  }
}
