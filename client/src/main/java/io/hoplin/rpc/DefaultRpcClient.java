package io.hoplin.rpc;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import io.hoplin.*;
import io.hoplin.json.JsonCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Default implementation of RPC client
 *
 * @param <I> the request type
 * @param <O> the response type
 */
public class DefaultRpcClient<I, O> implements RpcClient <I, O>
{
    private static final Logger log = LoggerFactory.getLogger(DefaultRpcClient.class);

    private final RabbitMQClient client;

    private JsonCodec codec;

    /** Channel we are communicating on*/
    private Channel channel;

    /** Queue where we will listen for our RPC replies */
    private String replyToQueueName;

    /** Exchange to send requests to */
    private final String exchange;

    private RpcCallerConsumer consumer;

    private boolean directReply;

    public DefaultRpcClient(final RabbitMQOptions options, final Binding binding)
    {
        Objects.requireNonNull(options);
        Objects.requireNonNull(binding);

        this.client = RabbitMQClient.create(options);
        this.codec = new JsonCodec();
        this.exchange = binding.getExchange();
        this.replyToQueueName = binding.getQueue();
        this.channel = client.channel();

        setupChannel();
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
                final Channel channel = channel();
                channel.exchangeDeclare(exchange, "direct",false, true, null);
                channel.queueDeclare(replyToQueueName, false, false, true, null);
            }
        }
        catch (final Exception e)
        {
            throw new HoplinRuntimeException("Unable to bind queue", e);
        }
    }

    private void setupChannel()
    {
        channel.addShutdownListener(sse->
        {
            log.info("Channel Shutdown, reacquiring : channel #{}", channel.getChannelNumber());
            channel = client.channel();

            if(channel != null)
            {
                log.info("New channel #{}", channel, channel.isOpen());
                reInitHandler();
            }
        });
    }

    private void reInitHandler()
    {
        log.info("Reinitializing topology & handler");

        setupChannel();
        bind();
        consumeReply();
    }

    private Channel channel()
    {
        final Channel channel = client.channel();
        if(false)
        {
            channel.addShutdownListener(sse->
            {
                final boolean initiatedByApplication = sse.isInitiatedByApplication();
                final boolean hardError = sse.isHardError();
                System.out.println("KILLED BY : " + sse);
                System.out.println("initiatedByApplication : " + initiatedByApplication);
                System.out.println("hardError : " + hardError);
            });

            System.out.println("HELLO OPEN ME : " + channel.isOpen());
            try
            {
                channel.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            System.out.println("HELLO OPEN ME2 : " + channel.isOpen());
        }
        return channel;
    }

    private void consumeReply()
    {
        try
        {
            final Channel channel = channel();
            consumer = new RpcCallerConsumer(channel);
            channel.basicConsume(replyToQueueName, true, consumer);
        }
        catch (final Exception e)
        {
            throw new HoplinRuntimeException("Unable to start RPC client reply consumer", e);
        }
    }

    @Override
    public O request(I request)
    {
       return request(request, "", Duration.ZERO);
    }

    @Override
    public O request(I request, String routingKey)
    {
        return request(request, routingKey, Duration.ZERO);
    }

    @Override
    public O request(I request, Duration timeout)
    {
        return request(request, "", timeout);
    }

    @Override
    public O request(I request, String routingKey, Duration timeout)
    {
        try
        {
            return requestAsync(request, routingKey, timeout).get();
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
        return requestAsync(request, "");
    }

    @Override
    public CompletableFuture<O> requestAsync(final I request, final String routingKey)
    {
        return requestAsync(request, routingKey, Duration.ZERO);
    }

    @Override
    public CompletableFuture<O> requestAsync(I request, Duration timeout)
    {
        return requestAsync(request, "", timeout);
    }

    @Override
    public CompletableFuture<O> requestAsync(I request, String routingKey, Duration timeout)
    {
        if(routingKey == null)
            throw new IllegalArgumentException("routingKey should not be null");

        final CompletableFuture<O> promise = new CompletableFuture<>();
        try
        {
            log.info("Publishing to Exchange = {}, RoutingKey = {} , ReplyTo = {}", exchange, routingKey, replyToQueueName);
            final String messageIdentifier =  UUID.randomUUID().toString();

            final AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(messageIdentifier)
                    .replyTo(replyToQueueName)
                    .build();

            consumer.bind(messageIdentifier, promise);
            channel().basicPublish(exchange, routingKey, props, createRequestPayload(request));
        }
        catch (final IOException e)
        {
            promise.completeExceptionally(e);
            log.error("Unable to send request", e);
        }

        return promise;
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
