package com.contentful.vault;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import static java.lang.String.format;
import static java.util.Locale.getDefault;

/**
 * This exporter takes a given space and syncs it's Contentful resources to a sqlite database.
 */
public class VaultDatabaseExporter {
  boolean successful;

  /**
   * Use this method to export Contentful spaces into sqlite3 databases which can be imported later.
   *
   * @param context     Android (Robolectric?) Context for creating the sqlite database.
   * @param spaceClass  Configured {@link Space} containing the name and the id of the space.
   * @param accessToken a CDA access token, used for retrieving the resources.
   * @return true in case of success. On error, please read System.err output.
   */
  public boolean export(Context context, Class<?> spaceClass, String accessToken) {
    successful = false;
    final SpaceHelper helper = crateSpaceHelper(spaceClass);
    final String outputPath = createOutputPath(helper);

    final CountDownLatch countDownLatch = new CountDownLatch(1);

    final Vault vault = Vault.with(context, spaceClass);
    vault.requestSync(
        SyncConfig
            .builder()
            .setSpaceId(helper.getSpaceId())
            .setAccessToken(accessToken)
            .build(),
        new SyncCallback() {
          @Override public void onResult(SyncResult result) {
            try {
              successful = saveResultInDatabaseFile(result, vault, outputPath);
            } catch (Throwable t) {
              t.printStackTrace(System.err);
            } finally {
              countDownLatch.countDown();
            }
          }
        }, new Executor() {
          @Override public void execute(Runnable runnable) {
            runnable.run();
          }
        });

    try {
      countDownLatch.await();
    } catch (InterruptedException e) {
      e.printStackTrace(System.err);
    }

    return successful;
  }

  String createOutputPath(SpaceHelper helper) {
    String outputPath = "src/main/assets/";
    if (helper.getCopyPath() != null) {
      outputPath += helper.getCopyPath();
      if (!new File(outputPath).exists()) {
        final String spaceName = getSpaceClassName(helper);
        throw new IllegalStateException("Database file does not exist on the filesystem. " +
            "Please remove it's annotation from " + spaceName + " and try again.");
      }
    } else {
      // no database name given, so use a default.
      outputPath += "initial_seed.db";
    }

    return outputPath;
  }

  String getSpaceClassName(SpaceHelper helper) {
    return helper.getClass().getCanonicalName().split("\\$")[0];
  }

  SpaceHelper crateSpaceHelper(Class<?> spaceClass) {
    final Class<?> clazz;
    try {
      clazz = Class.forName(spaceClass.getName() + Constants.SUFFIX_SPACE);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Could not find class", e);
    }

    final SpaceHelper helper;
    try {
      helper = (SpaceHelper) clazz.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new IllegalStateException("Cannot create a new instance of the space helper.", e);
    }

    if (helper == null) {
      throw new IllegalStateException("Cannot find SpaceHelper. " +
          "Did you use the Vault annotations and rebuild the app?");
    }

    return helper;
  }

  boolean saveResultInDatabaseFile(SyncResult result, Vault vault, String dataBaseFilePath) {
    boolean successful = result.isSuccessful();
    if (!successful) {
      throw new IllegalStateException("Could not return a valid result.", result.error());
    } else {
      final File outputDatabase = new File(dataBaseFilePath);

      final SQLiteDatabase readableDatabase = vault.getReadableDatabase();
      System.out.println(
          format(
              getDefault(),
              "Copying from '%s' to '%s'.",
              readableDatabase.getPath(),
              outputDatabase.getAbsolutePath())
      );

      try {
        FileUtils.copyFile(new File(readableDatabase.getPath()), outputDatabase);
      } catch (IOException e) {
        e.printStackTrace(System.err);
        successful = false;
      }

      System.out.println("Copying done.");
    }

    return successful;
  }
}
