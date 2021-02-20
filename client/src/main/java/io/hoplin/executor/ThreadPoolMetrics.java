package io.hoplin.executor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Metrics tracking for our {@link ThreadPoolExecutor}
 */
public interface ThreadPoolMetrics {

  /**
   * Mark when a thread begins executing a command.
   */
  void markThreadExecution();

  /**
   * Mark when a thread completes executing a command.
   */
  void markThreadCompletion();

  /**
   * Mark when a command gets rejected from the threadpool
   */
  void markThreadRejection();

  /**
   * Get number of currently running tasks
   *
   * @return
   */
  long getRunningTaskCount();

  /**
   * Current size of {@link BlockingQueue} used by the thread-pool
   *
   * @return Number
  long getCurrentQueueSize();
   */

  /**
   * Value from {@link ThreadPoolExecutor#getCompletedTaskCount()}
   *
   * @return long
   */
  long getCurrentCompletedTaskCount();

  /**
   * Get total task time
   *
   * @return
   */
  long getTotalTaskTime();

  /**
   * Add time to counter to get total time of task execution
   *
   * @param taskTime
   */
  void addTaskTime(long taskTime);

  class ThreadPoolMetricsDefault implements ThreadPoolMetrics {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPoolMetrics.class);

    private final AtomicInteger runningTasks = new AtomicInteger();

    private final AtomicInteger completedTasks = new AtomicInteger();

    private final AtomicLong totalTime = new AtomicLong();

    @Override
    public void markThreadExecution() {
      runningTasks.incrementAndGet();
    }

    @Override
    public void markThreadCompletion() {
      runningTasks.decrementAndGet();
      completedTasks.incrementAndGet();
    }

    @Override
    public void markThreadRejection() {
      runningTasks.decrementAndGet();
    }

    @Override
    public long getRunningTaskCount() {
      return runningTasks.get();
    }

    @Override
    public long getCurrentCompletedTaskCount() {
      return completedTasks.get();
    }

    @Override
    public long getTotalTaskTime() {
      return totalTime.get();
    }

    @Override
    public void addTaskTime(long taskTime) {
      totalTime.addAndGet(taskTime);
    }

    @Override
    public String toString() {
      long numTasks = getCurrentCompletedTaskCount();
      long totalTime = getTotalTaskTime();
      long avg = numTasks == 0 ? 0 : (totalTime / numTasks);

      return String.format(
          "RunningTasks = %s, CompletedTaskCount = %s, TotalTaskTime(ns) = %s, AvgTaskTime(ns) = %s",
          getRunningTaskCount(),
          numTasks, totalTime, avg);
    }
  }
}
