package com.contentful.vaultintegration;

import com.contentful.vault.Vault;
import com.contentful.vaultintegration.lib.arraylinks.MultiLinksSpace;
import com.contentful.vaultintegration.lib.arraylinks.Product;
import com.contentful.vaultintegration.lib.arraylinks.Product$Fields;
import com.contentful.vaultintegration.lib.arraylinks.Shop;

import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class ArrayLinks extends BaseTest {

  @Override protected void setupVault() {
    vault = Vault.with(RuntimeEnvironment.application, MultiLinksSpace.class);
  }

  @Test public void testLinks() throws Exception {
    // Initial
    enqueueSync("arraylinks");
    sync();

    // fetch only one
    final List<Product> all = vault.fetch(Product.class)
        .where(Product$Fields.REMOTE_ID + " = ?", "2ZMtY36Lj2M204kesAs4CK")
        .all();

    assertThat(all.size()).isEqualTo(1);
    final Product product = all.get(0);
    assertThat(product.remoteId().equals("2ZMtY36Lj2M204kesAs4CK")).isTrue();

    final Shop shop = product.shops().get(0);
    assertThat(shop.name()).isEqualTo("Alexa");
    assertThat(shop.products().size()).isEqualTo(6);
    assertThat(shop.products().get(3).remoteId()).isEqualTo(product.remoteId());

    assertThat(shop.products().get(2).shops().size()).isEqualTo(2);
    assertThat(shop.products().get(2).shops().get(1).name()).isEqualTo("Rewe");
  }
}
