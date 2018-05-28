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

import com.contentful.vault.Vault;
import com.contentful.vaultintegration.lib.escape.SqliteEscapeModel;
import com.contentful.vaultintegration.lib.escape.SqliteEscapeModel$Fields;
import com.contentful.vaultintegration.lib.escape.SqliteEscapeSpace;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import static com.google.common.truth.Truth.assertThat;

public class SqliteEscapeTest extends BaseTest {
  @Override protected void setupVault() {
    vault = Vault.with(RuntimeEnvironment.application, SqliteEscapeSpace.class);
  }

  @Test public void testSync() throws Exception {
    enqueueSync("escape");
    sync();

    SqliteEscapeModel item = vault.fetch(SqliteEscapeModel.class).first();
    assertItem(item);

    item = vault.fetch(SqliteEscapeModel.class)
        .where("`" + SqliteEscapeModel$Fields.ORDER + "` = ?", "foo")
        .first();

    assertItem(item);
  }

  private void assertItem(SqliteEscapeModel first) {
    assertThat(first).isNotNull();
    assertThat(first.order).isEqualTo("foo");
  }
}
