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

package com.contentful.vault;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Regression tests for VAULT-003: {@link Vault#releaseAll()} used to close every
 * registered space's database connection, globally, even ones unrelated to the
 * calling instance. {@link Vault#release()} fixes this by scoping the release to
 * this instance's own space only.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23)
public class VaultTest {

  static class SpaceA {
  }

  static class SpaceB {
  }

  /** Minimal hand-written {@link SpaceHelper}, standing in for generated code. */
  static class FakeSpaceHelper extends SpaceHelper {
    private final String databaseName;

    FakeSpaceHelper(String databaseName) {
      this.databaseName = databaseName;
    }

    @Override public String getDatabaseName() {
      return databaseName;
    }

    @Override public int getDatabaseVersion() {
      return 1;
    }

    @Override public Map<Class<?>, ModelHelper<?>> getModels() {
      return Collections.emptyMap();
    }

    @Override public Map<String, Class<? extends Resource>> getTypes() {
      return Collections.emptyMap();
    }

    @Override public String getCopyPath() {
      return null;
    }

    @Override public String getSpaceId() {
      return databaseName;
    }

    @Override public List<String> getLocales() {
      return Collections.singletonList("en-US");
    }
  }

  @After public void tearDown() {
    Vault.SQLITE_HELPERS.clear();
  }

  @Test public void releaseOnlyRemovesOwnSpace() {
    SqliteHelper helperA =
        new SqliteHelper(RuntimeEnvironment.application, new FakeSpaceHelper("space_a.db"));
    SqliteHelper helperB =
        new SqliteHelper(RuntimeEnvironment.application, new FakeSpaceHelper("space_b.db"));
    Vault.SQLITE_HELPERS.put(SpaceA.class, helperA);
    Vault.SQLITE_HELPERS.put(SpaceB.class, helperB);

    Vault vaultA = Vault.with(RuntimeEnvironment.application, SpaceA.class);
    vaultA.release();

    assertFalse("SpaceA's helper should be removed after release()",
        Vault.SQLITE_HELPERS.containsKey(SpaceA.class));
    assertTrue("SpaceB's helper must be untouched by SpaceA's release()",
        Vault.SQLITE_HELPERS.containsKey(SpaceB.class));
    assertTrue("SpaceB's helper instance must be unchanged",
        Vault.SQLITE_HELPERS.get(SpaceB.class) == helperB);
  }

  @Test public void releaseIsSafeToCallTwice() {
    SqliteHelper helperA =
        new SqliteHelper(RuntimeEnvironment.application, new FakeSpaceHelper("space_a2.db"));
    Vault.SQLITE_HELPERS.put(SpaceA.class, helperA);

    Vault vaultA = Vault.with(RuntimeEnvironment.application, SpaceA.class);
    vaultA.release();
    vaultA.release(); // must not throw when the entry is already gone

    assertFalse(Vault.SQLITE_HELPERS.containsKey(SpaceA.class));
  }

  @Test public void releaseAllStillClosesEveryRegisteredSpace() {
    SqliteHelper helperA =
        new SqliteHelper(RuntimeEnvironment.application, new FakeSpaceHelper("space_a3.db"));
    SqliteHelper helperB =
        new SqliteHelper(RuntimeEnvironment.application, new FakeSpaceHelper("space_b3.db"));
    Vault.SQLITE_HELPERS.put(SpaceA.class, helperA);
    Vault.SQLITE_HELPERS.put(SpaceB.class, helperB);

    Vault vaultA = Vault.with(RuntimeEnvironment.application, SpaceA.class);
    vaultA.releaseAll();

    assertTrue("releaseAll() must keep its legacy, global behavior for backward compatibility",
        Vault.SQLITE_HELPERS.isEmpty());
  }
}
