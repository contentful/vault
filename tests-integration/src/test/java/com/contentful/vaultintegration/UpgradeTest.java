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

import com.contentful.vault.Space;
import com.contentful.vault.Vault;
import com.contentful.vaultintegration.lib.demo.Cat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml")
public class UpgradeTest extends BaseTest {
  @Space(
      value = "cfexampleapi",
      models = Cat.class,
      locales = "en-US"
  )
  static class Sp1 { }

  @Space(
      value = "cfexampleapi",
      models = Cat.class,
      locales = "en-US",
      dbVersion = 2
  )
  static class Sp2 { }

  @Override protected void setupVault() {
    vault = Vault.with(RuntimeEnvironment.application, Sp1.class);
  }

  @Test public void testUpgrade() throws Exception {
    Vault v = vault;
    v.getReadableDatabase();
    v.releaseAll();
    v = Vault.with(RuntimeEnvironment.application, Sp2.class);
    v.getReadableDatabase();
  }
}
