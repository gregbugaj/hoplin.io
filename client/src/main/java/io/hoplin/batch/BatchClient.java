package io.hoplin.batch;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Batch processing interface
 */
public interface BatchClient {

  /**
   * Create new batch processing request
   *
   * @param context
   * @return ID associated with this request
   */
  CompletableFuture<BatchContext> startNew(final Consumer<BatchContext> context);

  /**
   * Create new batch processing request
   *
   * @param tasksToAdd
   * @return
   */
  CompletableFuture<BatchContext> startNew(final Iterable<Supplier<?>> tasksToAdd);

  /**
   * Start new batch after the previous one is finished
   * <p>
   * Batch continuation is fired when previous one has finished.
   *
   * @param batchId the batchId to continue after
   * @param context the {@link BatchContext} associated with the request
   * @return
   */
  UUID continueWith(final UUID batchId, final Consumer<BatchContext> context);

  /**
   * Cancel pending or not started batches
   *
   * @param batchId the batchId to cancel
   */
  void cancel(final UUID batchId);


}
