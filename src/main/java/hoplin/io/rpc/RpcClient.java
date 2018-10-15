package hoplin.io.rpc;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import hoplin.io.*;
import hoplin.io.json.JsonCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * RPC Client
 * Request/Response messaging pattern.
 *
 * @param <I> the request type (IN)
 * @param <O> the response type (OUT)
 */
public class RpcClient<I, O>
{
    private static final Logger log = LoggerFactory.getLogger(RpcClient.class);

    private final DefaultRabbitMQClient client;

    private final Binding binding;

    private final Channel channel;

    private JsonCodec codec;

    // Queue where we will listen for our RPC replies
    private String replyQueue;

    private String requestQueueName = "rpc_queue";

    private RpcCallerConsumer consumer;

    public RpcClient(final RabbitMQOptions options, final Binding binding)
    {
        Objects.requireNonNull(options);
        Objects.requireNonNull(binding);

        this.client = RabbitMQClient.create(options);
        this.channel = client.channel();
        this.binding = binding;
        this.codec = new JsonCodec();

        bind();
        consume();
    }

    /**
     *
     * @param request
     * @return
     */
    public CompletableFuture<O> requestAsync(final I request)
    {
        final CompletableFuture<O> promise = new CompletableFuture<>();

        try
        {
            log.info("Publishing reply_queue, msg > {}, {}", replyQueue, request);

            final UUID uuid = UUID.randomUUID();
            final String messageIdentifier = uuid.toString();

            final AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(messageIdentifier)
                    .replyTo(replyQueue)
                    .build();

            consumer.bind(messageIdentifier, promise);
            channel.basicPublish("", requestQueueName, props, createRequestPayload(request));
        }
        catch (final IOException e)
        {
            promise.completeExceptionally(e);
            log.error("Unable to send request", e);
        }

        return promise;
    }

    public O request(final I request)
    {
        try
        {
            final CompletableFuture<O> response = requestAsync(request);
            return response.get();
        }
        catch (final InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
        catch (final ExecutionException e)
        {
            log.error("Execution error", e);
        }

        return null;
    }

    public O request(final I request, long timeout, TimeUnit unit)
    {
        try
        {
            final CompletableFuture<O> response = requestAsync(request);
            return response.get(timeout, unit);
        }
        catch (final InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
        catch (final ExecutionException e)
        {
            log.error("Execution error", e);
        }
        catch (final TimeoutException e)
        {
            log.error("Timeout", e);
        }

        return null;
    }

    private byte[] createRequestPayload(final I request)
    {
        final MessagePayload<I> msg = new MessagePayload<>(request);
        msg.setType(request.getClass());
        return codec.serialize(msg);
    }

    /**
     * Declare the request queue where the responder will be waiting for the RPC requests
     * Create a temporary, private, autodelete reply queue
     */
    private void bind()
    {
        try
        {
            // Declare the request queue where the responder will be waiting for the RPC requests
            final Channel channel = client.channel();
            channel.queueDeclare(requestQueueName, false, false, false, null);
            replyQueue = channel.queueDeclare().getQueue();
            consumer = new RpcCallerConsumer(channel);
        }
        catch (final Exception e)
        {
            throw new HoplinRuntimeException("Unable to bind queue", e);
        }
    }

    private void consume()
    {
        try
        {
            final Channel channel = client.channel();
            channel.basicConsume(replyQueue, true, consumer);
        }
        catch (final Exception e)
        {
            throw new HoplinRuntimeException("Unable to start RPC client consumer", e);
        }
    }
}
