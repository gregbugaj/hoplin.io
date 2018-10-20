package hoplin.io;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import hoplin.io.json.JsonCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Default implementation of {@link RabbitMQClient}
 */
public class DefaultRabbitMQClient implements RabbitMQClient
{
    private static final Logger log = LoggerFactory.getLogger(DefaultRabbitMQClient.class);

    private RabbitMQOptions options;

    private Channel channel;

    private ConnectionProvider provider;

    private JsonCodec codec;

    public DefaultRabbitMQClient(final RabbitMQOptions options)
    {
        this.options = Objects.requireNonNull(options, "Options are required and  can't be null");
        this.provider = create();
        this.channel = provider.acquire();
        this.codec = new JsonCodec();
    }

    private ConnectionProvider create()
    {
        try
        {
            final ConnectionProvider provider = ConnectionProvider.create(options);
            final boolean results = provider.connect();

            if(!results)
                throw new IllegalStateException("Unable to connect to broker");

            return provider;
        }
        catch (final IOException | TimeoutException  e)
        {
            throw new RuntimeException("Unable to connect to broker", e);
        }
    }

    @Override
    public <T> void basicConsume(final String queue, final Class<T> clazz, final java.util.function.Consumer<T> handler)
    {
        basicConsume(queue, QueueOptions.of(true), clazz, handler);
    }

    @Override
    public <T> void basicConsume(final String queue,
                                 final QueueOptions options,
                                 final Class<T> clazz,
                                 final java.util.function.Consumer<T> handler)
    {
        Objects.requireNonNull(queue);
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(handler);
        Objects.requireNonNull(options);

        final boolean autoAck = options.isAutoAck();
        log.info("autoAck : {} ", autoAck);

        try
        {
            channel.basicQos(1);

            final Consumer consumer = create(channel, options, clazz, handler);
            channel.basicConsume(queue, autoAck, consumer);
        }
        catch (final IOException e)
        {
            log.error("Unable to subscribe messages", e);
        }
    }

    @Override
    public void exchangeDeclare(final String exchange,
                                final String type,
                                final boolean durable,
                                final boolean autoDelete)
    {
        exchangeDeclare(exchange, type, durable, autoDelete, Collections.emptyMap());
    }

    @Override
    public void exchangeDeclare(final String exchange,
                                final String type,
                                final boolean durable,
                                final boolean autoDelete,
                                final Map<String, Object> arguments)
    {
        with((channel) -> {
            channel.exchangeDeclare(exchange, type, durable);
            return null;
        });
    }

    @Override
    public void queueDeclare(final String queue,
                             final boolean durable,
                             final boolean exclusive,
                             final boolean autoDelete)
    {
        queueDeclare(queue, durable, exclusive, autoDelete, Collections.emptyMap());
    }

    @Override
    public AMQP.Queue.DeclareOk queueDeclare(final String queue,
                                             final boolean durable,
                                             final boolean exclusive,
                                             final boolean autoDelete,
                                             final Map<String, Object> arguments)
    {
        return with((channel) -> channel.queueDeclare(queue, durable, exclusive, autoDelete, arguments));
    }

    @Override
    public void queueBind(final String queue, final String exchange, final String routingKey)
    {
        with((channel) -> {
            channel.queueBind(queue, exchange, routingKey);
            return null;
        });
    }

    @Override
    public String queueDeclareTemporary()
    {
        return with(channel -> channel.queueDeclare().getQueue());
    }

    @Override public void disconnect() throws IOException
    {
        if(provider != null)
            provider.disconnect();
    }

    private <T> T with(final ThrowableChannel<T> handler)
    {
        try
        {
           return handler.handle(channel);
        }
        catch (final Exception e)
        {
            log.error("Unable to execute operation on  channel");
        }

        return null;
    }

    private interface ThrowableChannel<T>
    {
        T handle(Channel channel) throws Exception;
    }

    /**
     * Create queue consumer
     *
     * @param channel
     * @param queueOptions
     * @param clazz
     * @param handler
     * @param <T>
     * @return
     */
    private <T> Consumer create(final Channel channel, final QueueOptions queueOptions, final Class<T> clazz, final java.util.function.Consumer<T> handler)
    {
        final DefaultQueueConsumer consumer = new DefaultQueueConsumer(channel, queueOptions);
        consumer.addHandler(clazz, handler);

        return consumer;
    }

    private void logReceived(final Object message)
    {
        if (message == null)
        {
            log.debug("Received no message");
        }
        else if (log.isDebugEnabled())
        {
            log.debug("Received: {}", message);
        }
    }

    @Override
    public boolean isConnected()
    {
        return false;
    }

    @Override
    public boolean isOpenChannel()
    {
        return false;
    }

    @Override
    public int messageCount(final String queue)
    {
        try
        {
            return messageCountAsync(queue).get();
        }
        catch (final ExecutionException | InterruptedException e)
        {
            log.error("Unable to get message count", e);
        }

        return -1;
    }

    @Override
    public CompletableFuture<Integer> messageCountAsync(final String queue)
    {
        return null;
    }

    @Override
    public <T> void basicPublish(final String exchange, final String routingKey, final T message)
    {
        try
        {
            final String messageId = UUID.randomUUID().toString();
            final BasicProperties props = new BasicProperties.Builder()
                    .contentType("text/json")
                    .contentEncoding("UTF-8")
                    .messageId(messageId)
                    .deliveryMode(2)
                    .build();

            log.info("Publishing [exchange, routingKey, id] : {}, {}, {}", exchange, routingKey, messageId);

            final byte[] body = codec.serialize(message);
            channel.basicPublish(exchange, routingKey, props, body);
        }
        catch (final IOException e)
        {
            throw new HoplinRuntimeException("Unable to request message", e);
        }
    }

    @Override
    public void basicAck(final long deliveryTag, final boolean multiple)
    {

    }

    @Override
    public Channel channel()
    {
        return provider.acquire();
    }
}
