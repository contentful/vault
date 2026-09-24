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

import android.os.Build;
import android.os.Parcel;

import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Regression tests for VAULT-005: {@code Asset(Parcel)} used to call the deprecated,
 * unguarded {@code Parcel#readSerializable()} unconditionally. {@link Asset#readFileMap}
 * now prefers the typed API 33 overload (invoked reflectively, since this module compiles
 * against a pre-API-33 Android stub) and falls back safely to the legacy call otherwise.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23)
public class AssetTest {

  @Test @Config(sdk = 33) public void typedReadFailurePreservesOriginalException() {
    Parcel parcel = Parcel.obtain();
    try {
      parcel.writeSerializable("not a file map");
      HashMap<String, Object> followingValue = new HashMap<>();
      followingValue.put("url", "must not be returned");
      parcel.writeSerializable(followingValue);
      parcel.setDataPosition(0);
      try {
        Asset.readFileMap(parcel);
        fail("Expected the typed read to reject the wrong type");
      } catch (RuntimeException expected) {
        org.junit.Assert.assertTrue(expected instanceof android.os.BadParcelableException);
        org.junit.Assert.assertTrue(expected.getMessage().contains("java.util.HashMap"));
      }
    } finally {
      parcel.recycle();
    }
  }

  @Test public void readFileMapRoundTripsOnLegacySdk() {
    HashMap<String, Object> file = new HashMap<>();
    file.put("url", "//images.contentful.com/foo.png");
    file.put("size", 1024.0);

    Parcel parcel = Parcel.obtain();
    parcel.writeSerializable(file);
    parcel.setDataPosition(0);

    HashMap<String, Object> result = Asset.readFileMap(parcel);

    assertNotNull(result);
    assertEquals("//images.contentful.com/foo.png", result.get("url"));
    assertEquals(1024.0, result.get("size"));
  }

  @Test @Config(sdk = 33) public void readFileMapUsesTypedApiOnApi33() {
    HashMap<String, Object> file = new HashMap<>();
    file.put("url", "//images.contentful.com/typed.png");

    Parcel parcel = Parcel.obtain();
    parcel.writeSerializable(file);
    parcel.setDataPosition(0);

    HashMap<String, Object> result = Asset.readFileMap(parcel);
    assertNotNull(result);
    assertEquals("//images.contentful.com/typed.png", result.get("url"));
  }

  @Test public void readFileMapFallsBackSafelyWhenApi33PathIsUnavailable() {
    HashMap<String, Object> file = new HashMap<>();
    file.put("url", "//images.contentful.com/bar.png");

    Parcel parcel = Parcel.obtain();
    parcel.writeSerializable(file);
    parcel.setDataPosition(0);

    int originalSdkInt = Build.VERSION.SDK_INT;
    ReflectionHelpers.setStaticField(Build.VERSION.class, "SDK_INT", 33);
    try {
      // The test runtime (Robolectric's pre-API-33 shadow) doesn't have the 2-arg
      // Parcel#readSerializable(ClassLoader, Class) overload either, so this exercises
      // the reflective-lookup-fails fallback path rather than the typed call itself.
      HashMap<String, Object> result = Asset.readFileMap(parcel);
      assertNotNull(result);
      assertEquals("//images.contentful.com/bar.png", result.get("url"));
    } finally {
      ReflectionHelpers.setStaticField(Build.VERSION.class, "SDK_INT", originalSdkInt);
    }
  }
}
