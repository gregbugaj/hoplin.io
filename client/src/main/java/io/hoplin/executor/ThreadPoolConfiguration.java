package io.hoplin.executor;


/**
 * Configuration for our {@link WorkerThreadPool}
 */
public class ThreadPoolConfiguration {

  private final String threadPoolKey;

  private final int coreSize;

  private final int maximumSize;

  private final int maxQueueSize;

  private final int keepAliveTimeInMinutes;

  public ThreadPoolConfiguration(String threadPoolKey,
      int coreSize,
      int maximumSize,
      int maxQueueSize,
      int keepAliveTimeInMinutes) {
    this.threadPoolKey = threadPoolKey;
    this.coreSize = coreSize;
    this.maximumSize = maximumSize;
    this.maxQueueSize = maxQueueSize;
    this.keepAliveTimeInMinutes = keepAliveTimeInMinutes;
  }

  public String getThreadPoolKey() {
    return threadPoolKey;
  }

  public int getCoreSize() {
    return coreSize;
  }

  public int getMaximumSize() {
    return maximumSize;
  }

  public int getMaxQueueSize() {
    return maxQueueSize;
  }

  public int getKeepAliveTimeInMinutes() {
    return keepAliveTimeInMinutes;
  }

  @Override
  public String toString() {
    return String.format(
        "threadPoolKey = %s, coreSize = %s, maximumSize = %s, maxQueueSize = %s, keepAliveTimeInMinutes = %s",
        threadPoolKey,
        coreSize,
        maximumSize,
        maxQueueSize,
        keepAliveTimeInMinutes);
  }
}
