package io.hoplin;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Basic thread naming thread factory
 */
class HoplinThreadFactory implements ThreadFactory {

  private AtomicLong counter = new AtomicLong(1);

  @Override
  public Thread newThread(Runnable r) {
    final String name = "hoplin-" + counter.getAndIncrement();
    return new Thread(r, name);
  }
}
