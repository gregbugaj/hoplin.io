package io.hoplin.executor;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Worker thread pool that extends {@link ThreadPoolExecutor} to provide additional capabilities
 * <ul>
 *   <li>MDC context</li>
 *   <li>Thread metrics tracking information</li>
 * </ul>
 */
public class WorkerThreadPoolExecutor extends ThreadPoolExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkerThreadPoolExecutor.class);

  private final ThreadLocal<Long> startTime = new ThreadLocal<>();

  private final ThreadPoolMetrics metrics;

  public WorkerThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize,
      final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue,
      final ThreadFactory threadFactory, ThreadPoolMetrics metrics) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    this.metrics = Objects.requireNonNull(metrics);
  }

  @Override
  protected void beforeExecute(final Thread thread, final Runnable runnable) {
    try {
      // cleanup theadlocals to cleanup previously declared state
      final Field threadLocals = Thread.class.getDeclaredField("threadLocals");
      threadLocals.setAccessible(true);
      threadLocals.set(Thread.currentThread(), null);

      // This can only be set after cleanup
      metrics.markThreadExecution();
      startTime.set(System.nanoTime());
    } catch (final Exception e) {
      throw new AssertionError(e);
    } finally {
      super.beforeExecute(thread, runnable);
    }
  }

  @Override
  public void execute(final Runnable command) {
    try (final MDCContext ignored = MDCContext.create()) {
      super.execute(command);
    } catch (final Exception e) {
      LOGGER.error("Unable to clean MDC", e);
    }
  }

  @Override
  protected void afterExecute(final Runnable r, final Throwable t) {
    try {
      metrics.markThreadCompletion();
      final long taskTime = System.nanoTime() - startTime.get();
      metrics.addTaskTime(taskTime);

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(String.format("Thread %s: end %s, time = %dns  :  %dms  :  %ds",
            t,
            r,
            taskTime,
            TimeUnit.NANOSECONDS.toMillis(taskTime),
            TimeUnit.NANOSECONDS.toSeconds(taskTime)));
      }
    } catch (Exception e) {
      e.printStackTrace();
      ;
    } finally {
      super.afterExecute(r, t);
    }
  }

  @Override
  protected void terminated() {
    try {
      if (LOGGER.isDebugEnabled()) {
        long numTasks = metrics.getCurrentCompletedTaskCount();
        long totalTime = metrics.getTotalTaskTime();

        LOGGER.debug(String.format("Terminated : avg time = %dns",
            numTasks == 0 ? 0 : (totalTime / numTasks)));
      }
    } finally {
      super.terminated();
    }
  }

  private static class MDCContext implements AutoCloseable {

    private final Map<String, String> map;

    public MDCContext(final Map<String, String> copyOfContextMap) {
      this.map = copyOfContextMap;
    }

    @Override
    public void close() {
      if (map != null) {
        MDC.setContextMap(map);
      } else {
        MDC.clear();
      }
    }

    public static MDCContext create() {
      return new MDCContext(MDC.getCopyOfContextMap());
    }
  }
}
