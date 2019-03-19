package io.hoplin.rpc;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import io.hoplin.HoplinRuntimeException;
import io.hoplin.MessagePayload;
import io.hoplin.json.JsonCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Consumer responsible for receiving and handling RPC replies created by the server
 */
public class RpcCallerConsumer extends DefaultConsumer
{
    private static final Logger log = LoggerFactory.getLogger(RpcCallerConsumer.class);

    private ConcurrentHashMap<String, CompletableFuture> bindings = new ConcurrentHashMap<>();

    private JsonCodec codec;

    private final Executor executor;

    private boolean strictAction = true;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     */
    public RpcCallerConsumer(final Channel channel, final Executor executor)
    {
        super(channel);
        this.codec = new JsonCodec();
        this.executor = Objects.requireNonNull(executor);
    }

    public RpcCallerConsumer(final Channel channel)
    {
        this(channel, Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleDelivery(final String consumerTag,
                               final Envelope envelope,
                               final AMQP.BasicProperties properties,
                               final byte[] body)
    {
        log.info("RPC handleDelivery Envelope  : {}", envelope);
        log.info("RPC handleDelivery Properties: {}", properties);

        final String messageIdentifier = properties.getCorrelationId();
        final CompletableFuture<Object> action = bindings.remove(messageIdentifier);

        if(strictAction && action == null)
            throw new HoplinRuntimeException("Reply received without corresponding action : " + messageIdentifier);
        else if(!strictAction && action == null)
            return;
        else
            handleReply(body, action);
    }

    private void handleReply(final byte[] body, final CompletableFuture<Object> action)
    {
        CompletableFuture.runAsync(()->
        {
            if(log.isDebugEnabled())
                log.debug("reply body : {}", new String(body));

            try
            {
                final MessagePayload<?> reply = deserializeReplyPayload(body);
                if(reply.isFailure())
                     action.complete(null);

                action.complete(reply.getPayload());
            }
            catch (final Exception e)
            {
                log.error("Unable to complete reply action", e);
                action.completeExceptionally(e);
            }

        }, executor);
    }

    /**
     * Bind new message to specific {@link CompletableFuture}
     *
     * @param correlationId the id to bind reply message to
     * @param promise the future to complete
     */
    public void bind(final String correlationId, final CompletableFuture<?> promise)
    {
        Objects.requireNonNull(correlationId);
        Objects.requireNonNull(promise);

        bindings.put(correlationId, promise);
    }

    private MessagePayload<?> deserializeReplyPayload(final byte[] payload)
    {
        return codec.deserialize(payload, MessagePayload.class);
    }
}
