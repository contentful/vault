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

package com.contentful.vault;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import io.reactivex.subscribers.TestSubscriber;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/test/resources/AndroidManifest.xml")
public class ObserveTest {
  @Test public void observeOnError() throws Exception {
    Vault vault = mock(Vault.class);
    Throwable throwable = new RuntimeException();
    when(vault.fetch(Asset.class)).thenThrow(throwable);

    TestSubscriber<Asset> subscriber = new TestSubscriber<Asset>();
    new ObserveQuery<Asset>(Asset.class, vault).all().subscribe(subscriber);

    subscriber.assertError(throwable);
  }
}
