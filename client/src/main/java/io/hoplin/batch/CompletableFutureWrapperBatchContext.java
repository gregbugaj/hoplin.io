package io.hoplin.batch;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class CompletableFutureWrapperBatchContext {

  private CompletableFuture<BatchContext> future;

  private BatchContext context;

  public CompletableFutureWrapperBatchContext(final CompletableFuture<BatchContext> future,
      final BatchContext context) {
    this.future = Objects.requireNonNull(future);
    this.context = Objects.requireNonNull(context);
  }

  public CompletableFuture<BatchContext> getFuture() {
    return future;
  }

  public BatchContext getContext() {
    return context;
  }
}
