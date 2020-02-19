package io.hoplin.batch;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import io.hoplin.MessagePayload;
import io.hoplin.json.JsonMessagePayloadCodec;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consumer responsible for receiving and handling batch replies created by the server
 * <p>
 * Currently I don't think that there will be adding support reply typing. Client is responsible for handling
 * the reply them self.
 */
public class BatchReplyConsumer extends DefaultConsumer {

    private static final Logger log = LoggerFactory.getLogger(BatchReplyConsumer.class);

    private final ConcurrentHashMap<UUID, CompletableFutureWrapperBatchContext> batches;

    private final Executor executor;

    private JsonMessagePayloadCodec codec;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     */
    public BatchReplyConsumer(final Channel channel,
                              final ConcurrentHashMap<UUID, CompletableFutureWrapperBatchContext> batches,
                              final Executor executor) {
        super(channel);
        this.executor = Objects.requireNonNull(executor);
        this.batches = Objects.requireNonNull(batches);
        this.codec = new JsonMessagePayloadCodec();
    }

    public BatchReplyConsumer(final Channel channel,
                              final ConcurrentHashMap<UUID, CompletableFutureWrapperBatchContext> batches) {
        this(channel, batches,
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleDelivery(final String consumerTag,
                               final Envelope envelope,
                               final AMQP.BasicProperties properties,
                               final byte[] body) {

        if (log.isDebugEnabled()) {
            log.debug("batch reply received : {}", properties);
        }

        final Map<String, Object> headers = properties.getHeaders();
        final UUID batchId = UUID.fromString(headers.getOrDefault("x-batch-id", "").toString());
        final UUID correlationId = UUID
                .fromString(headers.getOrDefault("x-batch-correlationId", "").toString());
        final CompletableFutureWrapperBatchContext wrapperBatchContext = batches.get(batchId);
        final BatchContext context = wrapperBatchContext.getContext();
        final CompletableFuture<BatchContext> completable = wrapperBatchContext.getFuture();

        boolean found = false;
        for (final BatchContextTask task : context) {
            if (task.getTaskId().equals(correlationId)) {
                final long taskCount = context.decrementAndGetTaskCount();
                found = true;
                task.setReply(body);

                if (log.isDebugEnabled()) {
                    log.debug("Remaining task count[batch] : {} {}", batchId, taskCount);
                }
                break;
            }
        }

        if (!found) {
            final IllegalStateException ex = new IllegalStateException(
                    "not found : " + correlationId);
            completable.completeExceptionally(ex);
            throw ex;
        }

        if (context.isCompleted()) {
            completable.complete(context);
        }
    }

    private MessagePayload<?> handleReply(final byte[] body) {
        if (log.isDebugEnabled()) {
            log.debug("reply body : {}", new String(body));
        }
        try {
            return codec.deserialize(body, MessagePayload.class);
        } catch (final Exception e) {
            log.error("Unable to complete reply action", e);
        }

        return null;
    }

}
