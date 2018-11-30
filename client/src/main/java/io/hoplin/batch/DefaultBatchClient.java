package io.hoplin.batch;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class DefaultBatchClient implements BatchClient
{

    @Override
    public UUID startNew(final Consumer<BatchContext> consumer)
    {
        Objects.requireNonNull(consumer);
        final UUID batchId = UUID.randomUUID();
        final BatchContext context = new BatchContext();
        consumer.accept(context);

        return batchId;
    }

    @Override
    public void cancel(UUID batchId)
    {

    }
}
