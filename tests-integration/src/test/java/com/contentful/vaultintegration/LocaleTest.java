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

import com.contentful.vault.SyncConfig;
import com.contentful.vault.Vault;
import com.contentful.vaultintegration.lib.demo.Cat;
import com.contentful.vaultintegration.lib.demo.DemoSpace;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertEquals;

public class LocaleTest extends BaseTest {
  @Override protected void setupVault() {
    vault = Vault.with(RuntimeEnvironment.application, DemoSpace.class);
  }

  @Test public void testLocale() throws Exception {
    checkDefaultLocale();
    checkCustomLocale();
  }

  private void checkCustomLocale() throws Exception {
    // Klingon
    enqueue("demo/demo_space.json");
    enqueue("demo/demo_initial.json");
    sync(SyncConfig.builder().setClient(client).setLocale("tlh").build());
    Cat cat = vault.fetch(Cat.class).first();
    assertEquals("Quch vIghro'", cat.name);
  }

  private void checkDefaultLocale() throws Exception {
    // Default locale
    enqueue("demo/demo_space.json");
    enqueue("demo/demo_initial.json");
    sync();
    Cat cat = vault.fetch(Cat.class).first();
    assertEquals("Happy Cat", cat.name);
  }
}
