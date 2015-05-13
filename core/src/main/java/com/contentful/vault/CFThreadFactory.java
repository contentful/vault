package com.contentful.vault;

import android.os.Process;
import java.util.concurrent.ThreadFactory;

final class CFThreadFactory implements ThreadFactory {
  @SuppressWarnings("NullableProblems")
  @Override public Thread newThread(Runnable r) {
    return new CFThread(r);
  }

  private static class CFThread extends Thread {
    public CFThread(Runnable target) {
      super(target);
    }

    @Override public void run() {
      Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
      super.run();
    }
  }
}
