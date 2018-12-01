package io.hoplin.batch;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import io.hoplin.*;
import io.hoplin.json.JsonCodec;
import io.hoplin.rpc.DefaultRpcClient;
import io.hoplin.rpc.RpcCallerConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class DefaultBatchClient implements BatchClient
{
    private static final Logger log = LoggerFactory.getLogger(DefaultRpcClient.class);

    private final RabbitMQClient client;

    private JsonCodec codec;

    /** Channel we are communicating on */
    private final Channel channel;

    /** Queue where we will listen for our RPC replies */
    private String replyToQueueName;

    /** Exchange to send requests to */
    private final String exchange;

    private boolean directReply;

    public DefaultBatchClient(final RabbitMQOptions options, final Binding binding)
    {
        Objects.requireNonNull(options);
        Objects.requireNonNull(binding);

        this.client = RabbitMQClient.create(options);
        this.channel = client.channel();
        this.codec = new JsonCodec();

        this.exchange = binding.getExchange();
        this.replyToQueueName = binding.getQueue();

        bind();
        //consumeReply();
    }

    /**
     * Declare the request queue where the responder will be waiting for the RPC requests
     * Create a temporary, private, autodelete reply queue
     */
    private void bind()
    {
        if(replyToQueueName == null ||
                replyToQueueName.isEmpty() ||
                "amq.rabbitmq.reply-to".equalsIgnoreCase(replyToQueueName))
        {
            replyToQueueName =  "amq.rabbitmq.reply-to";
            directReply = true;
        }
        else
        {
            replyToQueueName = replyToQueueName+".reply-to." + UUID.randomUUID();
        }

        log.info("Param Exchange    : {}", exchange);
        log.info("Param ReplyTo     : {}", replyToQueueName);
        log.info("Param directReply : {}", directReply);

        try
        {
            if(!directReply)
            {
                channel.exchangeDeclare(exchange, "direct",true, false, null);
                channel.queueDeclare(replyToQueueName, false, false, true, null);
            }
        }
        catch (final Exception e)
        {
            throw new HoplinRuntimeException("Unable to bind queue", e);
        }
    }


    @Override
    public UUID startNew(final Consumer<BatchContext> consumer)
    {
        Objects.requireNonNull(consumer);
        final UUID batchId = UUID.randomUUID();
        final BatchContext context = new BatchContext();
        consumer.accept(context);

        final List<Object> tasks = context.getSubmittedTasks();
        if(tasks != null)
        {
            for (final Object task : tasks)
            {
                log.info("Task : {}", task);
                basicPublish(task, "", Duration.ZERO);
            }
        }

        return batchId;
    }

    public CompletableFuture basicPublish(Object request, String routingKey, Duration timeout)
    {
        if(routingKey == null)
            throw new IllegalArgumentException("routingKey should not be null");

        final CompletableFuture promise = new CompletableFuture<>();
        try
        {
            log.info("Publishing to Exchange = {}, RoutingKey = {} , ReplyTo = {}", exchange, routingKey, replyToQueueName);
            final String messageIdentifier =  UUID.randomUUID().toString();

            final AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(messageIdentifier)
                    .replyTo(replyToQueueName)
                    .build();

            channel.basicPublish(exchange, routingKey, props, createRequestPayload(request));
        }
        catch (final IOException e)
        {
            promise.completeExceptionally(e);
            log.error("Unable to send request", e);
        }

        return promise;
    }

    @Override
    public UUID continueWith(final UUID batchId, Consumer<BatchContext> context)
    {
        return null;
    }

    @Override
    public void cancel(final UUID batchId)
    {

    }

    private <I> byte[] createRequestPayload(final I request)
    {
        return codec.serialize(new MessagePayload<>(request));
    }

}
