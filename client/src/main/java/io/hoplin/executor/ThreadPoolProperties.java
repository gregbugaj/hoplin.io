package io.hoplin.executor;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadPoolProperties {

  private static final Logger log = LoggerFactory.getLogger(ThreadPoolProperties.class);

  private static int DEFAULT_CORESIZE = Runtime.getRuntime()
      .availableProcessors(); // core size of thread pool

  private static int DEFAULT_MAXIMUMSIZE = Runtime.getRuntime()
      .availableProcessors(); // maximum size of thread pool

  private static int DEFAULT_KEEPALIVETIMEMINUTES = 60; // minutes to keep a thread alive

  private static int DEFAULT_MAXQUEUESIZE = 1000;

  private static boolean DEFAULT_DEFAULTTIMEOUTENABLED = false;

  /**
   * Crate configuration with default values
   *
   * <pre>
   * System.setProperty("io.faucet.pool.default", "/opt/app/default.pool.json");
   * </pre>
   *
   * @param threadPoolKey
   * @return
   */
  public static ThreadPoolConfiguration withDefaults(final String threadPoolKey) {
    Objects.requireNonNull(threadPoolKey);
    return new ThreadPoolConfiguration(threadPoolKey,
        DEFAULT_CORESIZE,
        DEFAULT_MAXIMUMSIZE,
        DEFAULT_MAXQUEUESIZE,
        DEFAULT_KEEPALIVETIMEMINUTES);
  }
}
