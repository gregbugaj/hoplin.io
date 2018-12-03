package io.hoplin.batch;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import io.hoplin.*;
import io.hoplin.json.JsonCodec;
import io.hoplin.rpc.DefaultRpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

    private ConcurrentHashMap<UUID, BatchContext> batches = new ConcurrentHashMap<>();

    private BatchReplyConsumer consumer;

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
        consumeReply();
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
        final BatchContext context = new BatchContext();
        final UUID batchId = context.getBatchId();

        consumer.accept(context);
        batches.put(batchId, context);

        final List<BatchContextTask> tasks = context.getSubmittedTasks();

        if(tasks != null)
        {
            int total = tasks.size();
            int index = 0;

            for (final BatchContextTask task : tasks)
            {
                final Object message = task.getMessage();
                final Optional<UUID> taskId = basicPublish(batchId, message, "");

                if (taskId.isPresent())
                {
                    task.setTaskId(taskId.get());
                }
                ++index;
                log.info("Added task [{} of {}]: {} : {}", index, total, taskId, task);
            }
        }

        return batchId;
    }

    /**
     *
     * @param batchId
     * @param request
     * @param routingKey
     * @param <T>
     * @return
     */
    private <T> Optional<UUID> basicPublish(final UUID batchId, final T request, final String routingKey)
    {
        if(routingKey == null)
            throw new IllegalArgumentException("routingKey should not be null");

        try
        {
            log.info("Publishing to Exchange = {}, RoutingKey = {} , ReplyTo = {}", exchange, routingKey, replyToQueueName);
            final String messageIdentifier =  UUID.randomUUID().toString();
            final Map<String, Object> headers = new HashMap<>();
            headers.put("x-batch-id", batchId.toString());

            final AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(messageIdentifier)
                    .replyTo(replyToQueueName)
                    .headers(headers)
                    .build();

            channel.basicPublish(exchange, routingKey, props, createRequestPayload(request));
            return Optional.of(batchId);
        }
        catch (final IOException e)
        {
            log.error("Unable to send request", e);
        }

        return Optional.empty();
    }

    @Override
    public UUID continueWith(final UUID batchId, final Consumer<BatchContext> context)
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


    private void consumeReply()
    {
        try
        {
            consumer = new BatchReplyConsumer(channel, batches);
            channel.basicConsume(replyToQueueName, true, consumer);
        }
        catch (final Exception e)
        {
            throw new HoplinRuntimeException("Unable to start RPC client reply consumer", e);
        }
    }
}
