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

import com.contentful.vault.BaseFields;
import com.contentful.vault.SyncResult;
import com.contentful.vault.Vault;
import com.contentful.vaultintegration.lib.demo.Cat;
import com.contentful.vaultintegration.lib.demo.Cat$Fields;
import com.contentful.vaultintegration.lib.demo.DemoSpace;
import java.util.List;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import rx.observers.TestSubscriber;

import static com.google.common.truth.Truth.assertThat;

public class ObserveTest extends BaseTest {
  @Override protected void setupVault() {
    vault = Vault.with(RuntimeEnvironment.application, DemoSpace.class);
  }

  @Test public void observeQuery() throws Exception {
    enqueue("demo/space.json");
    enqueue("demo/types.json");
    enqueue("demo/initial.json");
    sync();

    TestSubscriber<Cat> subscriber = new TestSubscriber<>();
    vault.observe(Cat.class)
        .where(BaseFields.REMOTE_ID + " IN(?, ?, ?)", "happycat", "nyancat", "garfield")
        .limit(2)
        .order(Cat$Fields.UPDATED_AT + " DESC")
        .all()
        .subscribe(subscriber);

    subscriber.assertNoErrors();
    subscriber.assertCompleted();

    List<Cat> cats = subscriber.getOnNextEvents();
    assertThat(cats).hasSize(2);
    assertThat(cats.get(0).updatedAt()).isEqualTo("2013-11-18T15:58:02.018Z");
    assertThat(cats.get(1).updatedAt()).isEqualTo("2013-09-04T09:19:39.027Z");
  }

  @Test public void observeSyncResults() throws Exception {
    enqueue("demo/space.json");
    enqueue("demo/types.json");
    enqueue("demo/initial.json");

    TestSubscriber<SyncResult> subscriber = new TestSubscriber<>();
    Vault.observeSyncResults().subscribe(subscriber);

    subscriber.assertNoValues();
    sync();
    subscriber.assertNoTerminalEvent();

    List<SyncResult> events = subscriber.getOnNextEvents();
    assertThat(events).hasSize(1);
    assertThat(events.get(0).isSuccessful()).isTrue();
    assertThat(events.get(0).spaceId()).isEqualTo("cfexampleapi");
  }
}
