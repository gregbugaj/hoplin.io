package io.hoplin.executor;

import io.hoplin.executor.WorkerThreadPool.Factory;
import java.util.concurrent.TimeUnit;

/**
 * This is an executor service wrapper that provides an abstraction layer to the {@link
 * java.util.concurrent.ThreadPoolExecutor}
 */
public class WorkerExecutorService {

  private static WorkerExecutorService instance = new WorkerExecutorService();

  private WorkerExecutorService() {
    // no-op
  }

  public static WorkerExecutorService getInstance() {
    return instance;
  }

  public WorkerThreadPool getPublisherExecutor() {
    return Factory.getInstance("pub");
  }

  public WorkerThreadPool getSubscriberExecutor() {
    return Factory.getInstance("sub");
  }

  public void shutdownAndAwaitTermination(long timeout, TimeUnit unit) {
    Factory.shutdown(timeout, unit);
  }
}
