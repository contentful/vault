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

import com.contentful.java.cda.CDAClient;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class SyncConfigTest {
  @Test public void environmentDefaultsToMasterWhenOmitted() {
    SyncConfig config = SyncConfig.builder()
        .setAccessToken("token")
        .setSpaceId("space")
        .build();

    assertNotNull(config.client());
    assertEquals("master", SyncConfig.DEFAULT_ENVIRONMENT);
  }

  @Test public void explicitEnvironmentIsAccepted() {
    assertNotNull(SyncConfig.builder()
        .setAccessToken("token")
        .setSpaceId("space")
        .setEnvironment("staging")
        .build()
        .client());
  }

  @Test(expected = IllegalStateException.class)
  public void missingSpaceIdStillFails() {
    SyncConfig.builder().setAccessToken("token").build();
  }

  @Test(expected = IllegalStateException.class)
  public void missingAccessTokenStillFails() {
    SyncConfig.builder().setSpaceId("space").build();
  }

  @Test public void limitAcceptsBoundsAndNull() {
    CDAClient client = CDAClient.builder().setToken("token").setSpace("space").build();

    assertEquals(Integer.valueOf(SyncConfig.MIN_LIMIT),
        SyncConfig.builder().setClient(client).setLimit(SyncConfig.MIN_LIMIT).build().getLimit());
    assertEquals(Integer.valueOf(SyncConfig.MAX_LIMIT),
        SyncConfig.builder().setClient(client).setLimit(SyncConfig.MAX_LIMIT).build().getLimit());
    assertNull(SyncConfig.builder().setClient(client).setLimit(null).build().getLimit());
  }

  @Test public void limitRejectsOutOfRangeValues() {
    for (int invalid : new int[]{0, -1, SyncConfig.MAX_LIMIT + 1}) {
      try {
        SyncConfig.builder().setLimit(invalid);
        fail("Expected IllegalArgumentException for limit " + invalid);
      } catch (IllegalArgumentException expected) {
        // expected
      }
    }
  }
}
