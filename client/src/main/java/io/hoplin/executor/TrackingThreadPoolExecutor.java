package io.hoplin.executor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Track statistics of running threads.
 */
public class TrackingThreadPoolExecutor extends ThreadPoolExecutor {

  private static final Logger log = LoggerFactory.getLogger(TrackingThreadPoolExecutor.class);

  private final ConcurrentMap<Thread, Runnable> running = new ConcurrentHashMap<>();

  private final ThreadLocal<Long> startTime = new ThreadLocal<>();

  private final AtomicLong numTasks = new AtomicLong();

  private final AtomicLong totalTime = new AtomicLong();

  public TrackingThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
      long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
  }

  @Override
  protected void beforeExecute(final Thread t, final Runnable r) {
    running.put(t, r);
    startTime.set(System.nanoTime());
    running.remove(Thread.currentThread());
    super.beforeExecute(t, r);
  }

  @Override
  protected void afterExecute(final Runnable r, final Throwable t) {
    try {
      final long taskTime = System.nanoTime() - startTime.get();
      numTasks.incrementAndGet();
      totalTime.addAndGet(taskTime);

      if (log.isDebugEnabled()) {
        log.debug(String.format("Thread %s: end %s, time = %dns  :  %dms  :  %ds",
            t,
            r,
            taskTime,
            TimeUnit.NANOSECONDS.toMillis(taskTime),
            TimeUnit.NANOSECONDS.toSeconds(taskTime)));
      }
    } finally {
      super.afterExecute(r, t);
    }
  }

  @Override
  protected void terminated() {
    try {
      if (log.isDebugEnabled()) {
        log.debug(String.format("Terminated : avg time = %dns",
            numTasks.get() == 0 ? 0 : (totalTime.get() / numTasks.get())));
      }
    } finally {
      super.terminated();
    }
  }
}
