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

package com.contentful.vaultintegration;

import android.content.res.AssetManager;
import com.contentful.vault.Asset;
import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;
import com.contentful.vault.Space;
import com.contentful.vault.Vault;
import com.contentful.vault.VaultDatabaseExporter;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowArscAssetManager;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "AndroidManifest.xml", sdk = 23, shadows = DatabaseCopyTest.TestAssetManager.class)
public class DatabaseCopyTest extends BaseTest {
  @Implements(AssetManager.class)
  public static class TestAssetManager extends ShadowArscAssetManager {
    @Implementation protected InputStream open(String fileName) throws FileNotFoundException {
      InputStream stream = DatabaseCopyTest.class.getClassLoader().getResourceAsStream("assets/" + fileName);
      if (stream == null) {
        throw new FileNotFoundException(fileName);
      }
      return stream;
    }
  }
  @Space(
      value = "cfexampleapi",
      models = Cat.class,
      locales = {"en-US", "tlh"},
      copyPath = "cfexampleapi.db"
  ) interface Sp {
    String TOKEN = "b4c0n73n7fu1";
    String ENVIRONMENT = "environment";
  }

  @ContentType("cat")
  static class Cat extends Resource {
    @Field String name;
    @Field Cat bestFriend;
    @Field Asset image;
  }

  @Override protected void setupVault() {
    vault = Vault.with(RuntimeEnvironment.application, Sp.class);
  }

  @Test public void testDatabaseCopy() throws Exception {
    List<Cat> cats = vault.fetch(Cat.class).all();
    assertThat(cats).hasSize(3);
  }

  @Ignore("Do not update db on ci.")
  @Test
  public void testSyncDBtoSqlite() throws Exception {
    assertTrue(
        new VaultDatabaseExporter()
            .export(
                RuntimeEnvironment.application,
                Sp.class,
                Sp.TOKEN,
                Sp.ENVIRONMENT)
    );
  }
}
