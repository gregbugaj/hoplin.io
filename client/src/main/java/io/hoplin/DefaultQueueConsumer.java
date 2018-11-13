package io.hoplin;

import com.google.common.collect.ArrayListMultimap;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import io.hoplin.json.JsonCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Default consumer
 */
public class DefaultQueueConsumer extends DefaultConsumer
{
    private static final Logger log = LoggerFactory.getLogger(DefaultQueueConsumer.class);

    private final QueueOptions queueOptions;

    private ArrayListMultimap<Class, Consumer> handlers = ArrayListMultimap.create();

    private Executor executor;

    private JsonCodec codec;

    private final ConsumerErrorStrategy consumerErrorStrategy;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     * @param queueOptions the options to use for this queue consumer
     */
    public DefaultQueueConsumer(final Channel channel, final QueueOptions queueOptions)
    {
        this(channel, queueOptions, Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
    }

    /**
     * Construct a new instance of queue consumer
     * @param channel
     * @param queueOptions
     * @param executor
     */
    public DefaultQueueConsumer(final Channel channel, final QueueOptions queueOptions, final Executor executor)
    {
        super(channel);

        this.queueOptions = Objects.requireNonNull(queueOptions);
        this.executor = Objects.requireNonNull(executor);
        codec = new JsonCodec();
        consumerErrorStrategy = new DefaultConsumerErrorStrategy(channel);
    }

    /**
     * If you don't send the ack back, the consumer continues to fetch subsequent messages;
     * however, when you disconnect the consumer, all the messages will still be in the queue.
     * Messages are not consumed until RabbitMQ receives the corresponding ack.
     *
     * Note: A message must be acknowledged only once;
     *
     * @param consumerTag
     * @param envelope
     * @param properties
     * @param body
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    @Override
    public void handleDelivery(final String consumerTag, final Envelope envelope, AMQP.BasicProperties properties, byte[] body)
            throws IOException
    {
        AckStrategy ack;
        final ConsumerExecutionContext context = ConsumerExecutionContext.create(consumerTag, envelope, properties);

        try
        {
            ack = ackFromOptions(queueOptions);

            final MessagePayload message = codec.deserialize(body, MessagePayload.class);
            final Object val = message.getPayload();
            final Class<?> typeAsClass = message.getTypeAsClass();
            final Collection<Consumer> consumers = handlers.get(typeAsClass);
            final List<Throwable> exceptions = new ArrayList<>();
            final int handlerSize = consumers.size();

            if(handlerSize == 0)
            {
                throw new HoplinRuntimeException("No handlers defined for type : " + typeAsClass);
            }
            else if(handlerSize == 1)
            {
                final Map.Entry<Class, Consumer> entry = handlers.entries().iterator().next();
                if(!isExpectedType(entry.getKey(), typeAsClass))
                {
                    throw new IllegalArgumentException("Expected type does not match handler type : "+ entry.getKey()+", " + typeAsClass);
                }
            }

            for(final Consumer handler : consumers)
            {
                try
                {
                    handler.accept(val);
                }
                catch (final Exception e)
                {
                    exceptions.add(e);
                    log.error("Handler error for message  : " + message, e);
                }
            }
        }
        catch(final Exception e)
        {
            log.error("Unable to process message", e);
            try
            {
                ack = consumerErrorStrategy.handleConsumerError(context, e);
            }
            catch (final Exception ex2)
            {
                log.error("Exception in error strategy", ex2);
                ack = AcknowledgmentStrategies.BASIC_ACK.strategy();
            }
        }

        acknowledge(getChannel(), context, ack);
    }

    /**
     * Acknowledge given message
     *
     * @param channel the channel to send acknowledgment on
     * @param context the context to use for ack
     * @param ack the {@link AckStrategy} to use
     */
    private void acknowledge(final Channel channel,
                             final ConsumerExecutionContext context,
                             final AckStrategy ack)
    {
        try
        {
            log.info("Acking : {}" , context.getReceivedInfo().getDeliveryTag());
            ack.accept(channel, context.getReceivedInfo().getDeliveryTag());
        }
        catch (final Exception e)
        {
            log.error("Unable to ACK ", e);
        }
    }

    private AckStrategy ackFromOptions(final QueueOptions queueOptions)
    {
        if(queueOptions.isAutoAck())
            return AcknowledgmentStrategies.NOOP.strategy();

        return AcknowledgmentStrategies.BASIC_ACK.strategy();
    }

    /**
     * Check if given handled is of the correct type
     *
     * @param handler
     * @param typeAsClass
     * @return
     */
    private boolean isExpectedType(final Class<?> handler, final Class<?> typeAsClass)
    {
        Objects.requireNonNull(handler);
        Objects.requireNonNull(typeAsClass);

        return true;
    }

    /**
     * Add new handler bound to a specific type
     *
     * @param clazz
     * @param handler
     * @param <T>
     */
    public synchronized <T> void addHandler(final Class<T> clazz, final Consumer<T> handler)
    {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(handler);

        handlers.put(clazz, handler);
    }

    @Override
    public void handleCancel(final String consumerTag) throws IOException
    {
        // consumer has been cancelled unexpectedly
        throw new HoplinRuntimeException("Not yet implemented");
    }
}
