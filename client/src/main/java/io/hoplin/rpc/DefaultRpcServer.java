package io.hoplin.rpc;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import io.hoplin.*;
import io.hoplin.metrics.QueueMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * Default implementation of RPC server
 *
 * @param <I> the request type
 * @param <O> the response type
 */
public class DefaultRpcServer<I, O> implements RpcServer <I, O>
{
    private static final Logger log = LoggerFactory.getLogger(DefaultRpcServer.class);

    private final RabbitMQClient client;

    private final QueueMetrics metrics;

    /** Channel we are communicating on
     *  Upon disconnect this channel will be reinitialized
     **/
    private Channel channel;

    /** Exchange to send requests to */
    private final String exchange;

    /** Routing key to use for requests */
    private  String routingKey;

    /** Queue name used for incoming request*/
    private String requestQueueName;

    // executor that will process incoming RPC requests
    private Executor executor;

    private Function<I,O> handler;

    public DefaultRpcServer(final RabbitMQOptions options, final Binding binding)
    {
        Objects.requireNonNull(options);
        Objects.requireNonNull(binding);

        this.client = RabbitMQClient.create(options);
        this.channel = client.channel();
        this.executor  = createExecutor();

        this.exchange = binding.getExchange();
        this.routingKey = binding.getRoutingKey();
        this.requestQueueName = binding.getQueue();

        if(routingKey == null)
            routingKey = "";

        this.metrics = QueueMetrics.Factory.getInstance(exchange+"-"+requestQueueName);
        setupChannel();
        bind();
    }

    private Executor createExecutor()
    {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
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

    /**
     * Declare the request queue where the responder will be waiting for the RPC requests
     * Create a temporary, private, autodelete reply queue
     */
    private void bind()
    {
        log.info("Param RoutingKey  : {}", routingKey);
        log.info("Param Exchange    : {}", exchange);
        log.info("Param Request     : {}", requestQueueName);

        try
        {
            channel.exchangeDeclare(exchange, "direct",false, true, null);
            channel.queueDeclare(requestQueueName, false, false, true, null);
        }
        catch (final Exception e)
        {
            throw new HoplinRuntimeException("Unable to bind queue", e);
        }
    }

    /**
     * Setup consumer in 'server' mode
     * @param handler
     */
    @SuppressWarnings("unchecked")
    private void consumeRequest(final Function<I, O> handler)
    {
        try
        {
            final AMQP.Queue.BindOk bindStatus = channel.queueBind(requestQueueName, exchange, routingKey);
            log.info("consumeRequest requestQueueName : {}, {}", requestQueueName, bindStatus);
            channel.basicQos(1);
            channel.basicConsume(requestQueueName, false, new RpcResponderConsumer(channel, handler, executor, metrics));
        }
        catch (final Exception e)
        {
            throw new HoplinRuntimeException("Unable to start RPC server consumer", e);
        }
    }

    private void reInitHandler()
    {
        log.info("Reinitializing topology & handler");

        setupChannel();
        bind();

        if(handler != null)
            consumeRequest(handler);
    }

    @Override
    public void respondAsync(final Function<I, O> handler)
    {
        this.handler = handler;
        consumeRequest(handler);
    }

    /**
     * Create new {@link DefaultRpcServer}
     *
     * @param options the connection options to use
     * @param binding the binding to use
     * @return new Direct Exchange client setup in server mode
     */
    public static RpcServer create(final RabbitMQOptions options, final Binding binding)
    {
        Objects.requireNonNull(options);
        Objects.requireNonNull(binding);

        return new DefaultRpcServer<>(options, binding);
    }

    public void close() throws IOException
    {
        if(client != null)
            client.disconnect();
    }
}
