package io.hoplin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.hoplin.executor.WorkerThreadPool;
import io.hoplin.executor.ThreadPoolMetrics;
import io.hoplin.executor.ThreadUtil;
import io.hoplin.executor.WorkerExecutorService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

public class WorkerExecutorServiceTest {

  @Test
  public void testMetricsPublisher() throws InterruptedException {
    final WorkerExecutorService service = WorkerExecutorService.getInstance();
    final WorkerThreadPool publisherExecutor = service.getPublisherExecutor();

    final ExecutorService executor = publisherExecutor.getExecutor();
    final ThreadPoolMetrics metrics = publisherExecutor.getMetrics();

    for (int i = 0; i < 10; ++i) {
      final int val = i;
      CompletableFuture.supplyAsync(() -> {
        ThreadUtil.sleepUninterruptibly(10, TimeUnit.MILLISECONDS);
        return val;
      }, executor);
    }

    service.shutdownAndAwaitTermination(1, TimeUnit.SECONDS);
    assertEquals(10, metrics.getCurrentCompletedTaskCount());
  }

  @Test
  public void testMultipleExecutors() {
    final WorkerExecutorService service = WorkerExecutorService.getInstance();
    final WorkerThreadPool publisher = service.getPublisherExecutor();
    final WorkerThreadPool subscriber = service.getSubscriberExecutor();

    final ExecutorService publisherExecutor = publisher.getExecutor();
    final ExecutorService subscriberExecutor = subscriber.getExecutor();

    final ThreadPoolMetrics m1 = publisher.getMetrics();
    final ThreadPoolMetrics m2 = subscriber.getMetrics();

    for (int i = 0; i < 7; ++i) {
      final ExecutorService executor;
      if (i % 2 == 0) {
        executor = publisherExecutor;
      } else {
        executor = subscriberExecutor;
      }

      final int val = i;
      CompletableFuture.supplyAsync(() -> {
        ThreadUtil.sleepUninterruptibly(10, TimeUnit.MILLISECONDS);
        return val;
      }, executor);
    }

    service.shutdownAndAwaitTermination(1, TimeUnit.SECONDS);

    assertEquals(4, m1.getCurrentCompletedTaskCount());
    assertEquals(3, m2.getCurrentCompletedTaskCount());
  }
}
