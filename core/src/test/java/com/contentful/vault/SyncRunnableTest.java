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

import static com.contentful.vault.SyncRunnable.requiresFullResync;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Locale-mode changes must wipe and fully re-sync, so no locale table keeps stale rows. */
public class SyncRunnableTest {
  @Test public void emptyDatabaseNeverNeedsReset() {
    assertFalse(requiresFullResync(null, false, true));
    assertFalse(requiresFullResync(true, false, false));
    assertFalse(requiresFullResync(false, false, true));
  }

  @Test public void unchangedModeKeepsData() {
    assertFalse(requiresFullResync(true, true, true));
    assertFalse(requiresFullResync(false, true, false));
  }

  @Test public void changedModeResets() {
    assertTrue(requiresFullResync(false, true, true));
    assertTrue(requiresFullResync(true, true, false));
  }

  @Test public void unknownModeNeverResets() {
    // Right after upgrading Vault the mode is unknown: keep the offline data, just record the mode.
    assertFalse(requiresFullResync(null, true, true));
    assertFalse(requiresFullResync(null, true, false));
  }
}
