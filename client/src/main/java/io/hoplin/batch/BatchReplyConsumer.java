package io.hoplin.batch;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import io.hoplin.HoplinRuntimeException;
import io.hoplin.MessagePayload;
import io.hoplin.json.JsonCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Consumer responsible for receiving and handling RPC replies created by the server
 */
public class BatchReplyConsumer extends DefaultConsumer
{
    private static final Logger log = LoggerFactory.getLogger(BatchReplyConsumer.class);

    private JsonCodec codec;

    private final Executor executor;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     */
    public BatchReplyConsumer(final Channel channel, final ConcurrentHashMap<UUID, BatchContext> batches, final Executor executor)
    {
        super(channel);
        codec = new JsonCodec();
        this.executor = Objects.requireNonNull(executor);
    }

    public BatchReplyConsumer(final Channel channel, final ConcurrentHashMap<UUID, BatchContext> batches)
    {
        this(channel, batches, Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
    }


    @SuppressWarnings("unchecked")
    @Override
    public void handleDelivery(final String consumerTag,
                               final Envelope envelope,
                               final AMQP.BasicProperties properties,
                               final byte[] body)
    {
        log.info("handleDelivery : {} > {}", properties , envelope);

        final Map<String, Object> headers = properties.getHeaders();
        final String batchId = headers.getOrDefault("x-batch-id", "").toString();
        final String messageIdentifier = properties.getCorrelationId();

        log.info("handleDelivery [batchId, messageIdentifier] : {} > {}", batchId , messageIdentifier);
    }


    private void handleReply(final byte[] body, final CompletableFuture<Object> action)
    {
        CompletableFuture.runAsync(()->{

            if(log.isDebugEnabled())
            {
                log.debug("reply body : {}", new String(body));
            }

            try
            {
                final MessagePayload<?> reply = deserializeReplyPayload(body);
                action.complete(reply.getPayload());
            }
            catch (final Exception e)
            {
                log.error("Unable to complete reply action", e);
                action.completeExceptionally(e);
            }

        }, executor);
    }

    private MessagePayload<?> deserializeReplyPayload(final byte[] payload)
    {
        return codec.deserialize(payload, MessagePayload.class);
    }
}
