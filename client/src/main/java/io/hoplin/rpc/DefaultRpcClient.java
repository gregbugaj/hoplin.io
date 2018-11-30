package io.hoplin.rpc;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import io.hoplin.*;
import io.hoplin.json.JsonCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * Default implementation of RPC client
 *
 * Todo : implement direct reply to pattern
 * https://www.rabbitmq.com/direct-reply-to.html
 *
 * @param <I> the request type
 * @param <O> the response type
 */
public class DefaultRpcClient<I, O> implements RpcClient <I, O>
{
    private static final Logger log = LoggerFactory.getLogger(DefaultRpcClient.class);

    private final RabbitMQClient client;

    /** Channel we are communicating on */
    private final Channel channel;

    private JsonCodec codec;

    /** Queue where we will listen for our RPC replies */
    private String replyToQueueName;

    /** Exchange to send requests to */
    private final String exchange;

    /** Routing key to use for requests */
    private  String routingKey;

    /** Queue name used for incoming request*/
    private String requestQueueName;

    private RpcCallerConsumer consumer;

    // executor that will acknowledgeExceptionally incoming RPC requests
    private Executor executor;

    private boolean replyConsumerInited;

    public DefaultRpcClient(final RabbitMQOptions options, final Binding binding)
    {
        Objects.requireNonNull(options);
        Objects.requireNonNull(binding);

        this.client = RabbitMQClient.create(options);
        this.channel = client.channel();
        this.codec = new JsonCodec();
        this.executor  = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        this.exchange = binding.getExchange();
        this.routingKey = binding.getRoutingKey();
        this.replyToQueueName = binding.getQueue();

        if(routingKey == null)
            routingKey = "";

        bind();
    }

    /**
     * Declare the request queue where the responder will be waiting for the RPC requests
     * Create a temporary, private, autodelete reply queue
     */
    private void bind()
    {
        boolean directReply = false;
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


        requestQueueName = replyToQueueName;

        log.info("Param ReplyTo     {}", replyToQueueName);
        log.info("Param Request     {}", requestQueueName);
        log.info("Param RoutingKey  {}", routingKey);
        log.info("Param Exchange    {}", exchange);

        try
        {
            channel.exchangeDeclare(exchange, "fanout",false, true, null);
            // Declare the request queue where the responder will be waiting for the RPC requests
            if(!directReply)
            {
                channel.queueDeclare(replyToQueueName, false, false, true, null);
                channel.queueDeclare(requestQueueName, false, false, true, null);
            }
        }

        catch (final Exception e)
        {
            throw new HoplinRuntimeException("Unable to bind queue", e);
        }
    }

    private void consumeReply()
    {
        if(replyConsumerInited)
            return;

        try
        {
            consumer = new RpcCallerConsumer(channel);
            channel.basicConsume(replyToQueueName, true, consumer);
            replyConsumerInited = true;
        }
        catch (final Exception e)
        {
            throw new HoplinRuntimeException("Unable to start RPC client consumer", e);
        }
    }

    private void consumeRequest(final Function<I, O> handler)
    {
        try
        {

            final AMQP.Queue.BindOk bindStatus = channel.queueBind(requestQueueName, exchange, routingKey);
            log.info("consumeRequest requestQueueName :: {}", requestQueueName);
            log.info("consumeRequest BindOk :: {}", bindStatus);

            channel.basicQos(1);
            channel.basicConsume(requestQueueName, false, new RpcResponderConsumer(channel, handler, executor));
        }
        catch (final Exception e)
        {
            throw new HoplinRuntimeException("Unable to start RPC server consumer", e);
        }
    }

    @Override
    public O request(I request)
    {
        try
        {
            return requestAsync(request).get();
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

    public CompletableFuture<O> requestAsync(final I request)
    {
        consumeReply();
        final CompletableFuture<O> promise = new CompletableFuture<>();
        try
        {
            log.info("Publishing to reply_to_queue, msg > {}, {}", replyToQueueName, request);
            final String messageIdentifier =  UUID.randomUUID().toString();

            final AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(messageIdentifier)
                    .replyTo(replyToQueueName)
                    .build();

            consumer.bind(messageIdentifier, promise);
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
    public void respondAsync(final Function<I, O> handler)
    {
        consumeRequest(handler);
    }

    /**
     * Create new {@link DefaultRpcClient}
     *
     * @param options the connection options to use
     * @param binding the binding to use
     * @return new Direct Exchange client setup in server mode
     */
    public static RpcClient create(final RabbitMQOptions options, final Binding binding)
    {
        Objects.requireNonNull(options);
        Objects.requireNonNull(binding);

        return new DefaultRpcClient<>(options, binding);
    }

    private byte[] createRequestPayload(final I request)
    {
        final MessagePayload<I> msg = new MessagePayload<>(request);
        msg.setType(request.getClass());
        return codec.serialize(msg);
    }

    public void close() throws IOException
    {
        if(client != null)
            client.disconnect();
    }
}
