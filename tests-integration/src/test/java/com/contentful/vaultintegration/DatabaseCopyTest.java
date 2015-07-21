package com.contentful.vaultintegration;

import com.contentful.vault.Asset;
import com.contentful.vault.ContentType;
import com.contentful.vault.Field;
import com.contentful.vault.Resource;
import com.contentful.vault.Space;
import com.contentful.vault.Vault;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml")
public class DatabaseCopyTest extends BaseTest {
  @Space(value = "cfexampleapi", models = { Cat.class }, copyPath = "cfexampleapi.db")
  static class Sp { }

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
}
