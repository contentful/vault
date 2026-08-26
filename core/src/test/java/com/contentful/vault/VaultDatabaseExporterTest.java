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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Regression tests for VAULT-004: {@link VaultDatabaseExporter#export} used to deadlock
 * (ANR) when invoked from the main thread, because its completion callback is always
 * delivered on the main thread via a {@code Handler(Looper.getMainLooper())}, so the
 * blocking {@code CountDownLatch.await()} could never be released.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23)
public class VaultDatabaseExporterTest {

  /** Any class works: the main-thread guard must fire before this is ever looked up. */
  static class FakeSpace {
  }

  @Test public void exportThrowsImmediatelyWhenCalledOnMainThread() {
    try {
      new VaultDatabaseExporter()
          .export(RuntimeEnvironment.application, FakeSpace.class, "token", "master");
      fail("Expected an IllegalStateException because export() was called on the main thread.");
    } catch (IllegalStateException e) {
      assertTrue("Guard message should mention the main-thread restriction",
          e.getMessage().contains("main thread"));
    }
  }

  @Test public void exportDoesNotHitMainThreadGuardOnBackgroundThread() throws InterruptedException {
    final Throwable[] captured = new Throwable[1];
    Thread thread = new Thread(new Runnable() {
      @Override public void run() {
        try {
          new VaultDatabaseExporter()
              .export(RuntimeEnvironment.application, FakeSpace.class, "token", "master");
        } catch (Throwable t) {
          captured[0] = t;
        }
      }
    });
    thread.start();
    thread.join();

    assertTrue("A background-thread call must still fail, since FakeSpace has no generated "
            + "space helper, but NOT because of the main-thread guard",
        captured[0] instanceof IllegalStateException);
    assertFalse("The failure must not be the main-thread guard",
        captured[0].getMessage().contains("main thread"));
  }
}
