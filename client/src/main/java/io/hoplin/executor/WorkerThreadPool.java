package io.hoplin.executor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface WorkerThreadPool {

  /**
   * Get executor associated with this pool
   *
   * @return underlying ThreadPoolExecutor
   */
  ThreadPoolExecutor getExecutor();

  /**
   * Get current {@link ThreadPoolMetrics} tied to this executor
   *
   * @return
   */
  ThreadPoolMetrics getMetrics();

  /**
   * Get configuration used to construct this pool
   *
   * @return current {@link ThreadPoolConfiguration} configuration
   */
  ThreadPoolConfiguration getConfig();

  class Factory {

    private static final Logger LOGGER = LoggerFactory.getLogger(Factory.class);

    private static final ConcurrentHashMap<String, WorkerThreadPool> pools = new ConcurrentHashMap<>();

    public static WorkerThreadPool getInstance(final String threadPoolKey,
        ThreadPoolConfiguration configuration) {
      Objects.requireNonNull(threadPoolKey);
      final WorkerThreadPool pool = pools.get(threadPoolKey);

      if (pool != null) {
        return pool;
      }

      if (configuration == null) {
        configuration = ThreadPoolProperties.withDefaults(threadPoolKey);
      }

      // should hit only once
      final WorkerThreadPool value = pools.putIfAbsent(threadPoolKey,
          new WorkerThreadPoolDefault(configuration));

      // race condition between Thred1 and Thread2, so we simply retrieve results from the cache
      if (value == null) {
        return pools.get(threadPoolKey);
      }

      return value;
    }

    public static WorkerThreadPool getInstance(final String threadPoolKey) {
      return getInstance(threadPoolKey, null);
    }

    public static void shutdown(final long timeout, final TimeUnit unit) {
      for (final WorkerThreadPool pool : pools.values()) {
        pool.getExecutor().shutdown();
      }

      awaitTermination(timeout, unit);
    }

    public Map<String, List<Runnable>> shutdownNow() {

      final Map<String, List<Runnable>> tasks = new HashMap<>();

      for (final WorkerThreadPool pool : pools.values()) {
        final ThreadPoolConfiguration config = pool.getConfig();
        final String key = config.getThreadPoolKey();

        tasks.put(key, pool.getExecutor().shutdownNow());
      }

      awaitTermination(-1, TimeUnit.MILLISECONDS);

      return tasks;
    }

    private static void awaitTermination(final long timeout, final TimeUnit unit) {
      for (final WorkerThreadPool pool : pools.values()) {
        try {
          if (!pool.getExecutor().awaitTermination(timeout, unit)) {
            LOGGER.warn("Threads did not finish in : {} ms", unit.toMillis(timeout));
          }
        } catch (final InterruptedException e) {
          // we are shutting down so we can ignore it
        }
      }

      pools.clear();
    }

    /**
     * Reset underlying states of ALL pools
     */
    public static void reset() {
      pools.clear();
    }
  }

  class WorkerThreadPoolDefault implements WorkerThreadPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerThreadPoolDefault.class);

    private final ThreadPoolExecutor executor;

    private final ThreadPoolConfiguration config;

    private final ThreadPoolMetrics metrics;

    WorkerThreadPoolDefault(final ThreadPoolConfiguration config) {
      Objects.requireNonNull(config);
      this.config = config;
      this.metrics = ThreadPoolMetrics.Factory.getInstance(config.getThreadPoolKey());
      this.executor = createExecutorService(this.metrics);
    }

    private ThreadPoolExecutor createExecutorService(final ThreadPoolMetrics metrics) {
      Objects.requireNonNull(metrics);

      final int corePoolSize = config.getCoreSize();
      final int maximumPoolSize = config.getMaximumSize();
      final int maxQueueSize = config.getMaxQueueSize();
      final String threadPoolKey = config.getThreadPoolKey();
      final int keepAliveTimeInMinutes = config.getKeepAliveTimeInMinutes();

      return new WorkerThreadPoolExecutor(corePoolSize,
          maximumPoolSize,
          keepAliveTimeInMinutes,
          TimeUnit.MILLISECONDS,
          new LinkedBlockingDeque<>(),
          new NamedThreadFactory("hoplin-" + threadPoolKey, true), metrics);
    }

    @Override
    public ThreadPoolExecutor getExecutor() {
      return executor;
    }

    @Override
    public ThreadPoolMetrics getMetrics() {
      return metrics;
    }

    @Override
    public ThreadPoolConfiguration getConfig() {
      return config;
    }
  }
}
