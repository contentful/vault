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
    enqueue("demo_space.json");
    enqueue("demo_initial.json");
    sync(SyncConfig.builder().setClient(client).setLocale("tlh").build());
    Cat cat = vault.fetch(Cat.class).first();
    assertEquals("Quch vIghro'", cat.name);
  }

  private void checkDefaultLocale() throws Exception {
    // Default locale
    enqueue("demo_space.json");
    enqueue("demo_initial.json");
    sync();
    Cat cat = vault.fetch(Cat.class).first();
    assertEquals("Happy Cat", cat.name);
  }
}
