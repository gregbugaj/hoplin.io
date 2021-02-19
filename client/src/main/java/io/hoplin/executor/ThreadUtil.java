package io.hoplin.executor;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadUtil {

  private static final Logger log = LoggerFactory.getLogger(ThreadUtil.class);

  public static void sleepUninterruptibly(final Duration duration) {
    Objects.requireNonNull(duration);
    sleepUninterruptibly(duration.toMillis(), TimeUnit.MILLISECONDS);
  }

  /**
   * Invokes {@code unit.}{@link TimeUnit#sleep(long) sleep(sleepFor)} uninterruptibly.
   */
  public static void sleepUninterruptibly(final long sleepFor, final TimeUnit unit) {
    boolean interrupted = false;
    try {
      long remainingNanos = unit.toNanos(sleepFor);
      final long end = System.nanoTime() + remainingNanos;
      while (true) {
        try {
          // TimeUnit.sleep() treats negative timeouts just like zero.
          NANOSECONDS.sleep(remainingNanos);
          return;
        } catch (final InterruptedException e) {
          interrupted = true;
          remainingNanos = end - System.nanoTime();
        }
      }
    } finally {
      if (interrupted) {
        Thread.currentThread().interrupt();
      }
    }
  }

  public static void trace() {
    trace("Trace");
  }

  public static void trace(final String message) {
    try {
      throw new Exception(message);
    } catch (final Exception e) {
      log.error("Trace", e);
    }
  }

}
