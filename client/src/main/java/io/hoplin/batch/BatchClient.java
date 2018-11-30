package io.hoplin.batch;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Batch processing interface
 */
public interface BatchClient
{
    /**
     * Create new batch processing request
     *
     * @param context
     * @return  ID associated with this request
     */
    UUID startNew(final Consumer<BatchContext> context);

    /**
     * Cancel pending or not started batches
     *
     * @param batchId the batchId to cancel
     */
    void cancel(final UUID batchId);
}
