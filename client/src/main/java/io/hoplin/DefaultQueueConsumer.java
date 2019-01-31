package io.hoplin;

import com.google.common.collect.ArrayListMultimap;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import io.hoplin.json.JsonCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Default consumer
 */
public class DefaultQueueConsumer extends DefaultConsumer
{
    private static final Logger log = LoggerFactory.getLogger(DefaultQueueConsumer.class);

    private final QueueOptions queueOptions;

    private ArrayListMultimap<Class, MethodReference> handlers = ArrayListMultimap.create();

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

     * @param properties
     * @param body
     */
    @SuppressWarnings("unchecked")
    @Override
    public void handleDelivery(final String consumerTag, final Envelope envelope, AMQP.BasicProperties properties, byte[] body)
    {
        AckStrategy ack;
        final MessageContext context = MessageContext.create(consumerTag, envelope, properties);

        try
        {
            ack = ackFromOptions(queueOptions);

            final MessagePayload message = codec.deserialize(body, MessagePayload.class);
            final Object val = message.getPayload();
            final Class<?> targetClass = message.getTypeAsClass();
            final Collection<MethodReference> consumers = handlers.get(targetClass);
            final List<Throwable> exceptions = new ArrayList<>();

            int invokedHandlers = 0;

            for(final MethodReference reference : consumers)
            {
                try
                {
                    final BiConsumer handler = reference.handler;
                    final Class root = reference.root;

                    if(root == targetClass)
                    {
                        ++invokedHandlers;
                        execute(context, val, handler);
                    }
                    else // Down cast if necessary
                    {
                        final Optional<?> castedValue = safeCast(val, targetClass);
                        if(castedValue.isPresent())
                        {
                            ++invokedHandlers;
                            execute(context, castedValue.get(), handler);
                        }
                    }
                }
                catch (final Exception e)
                {
                    exceptions.add(e);
                    log.error("Handler error for message  : " + message, e);
                }
            }

            if(invokedHandlers == 0)
            {
                throw new HoplinRuntimeException("No handlers defined for type : " + targetClass);
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

    private void execute(final MessageContext context, final Object val,final BiConsumer handler)
    {
        handler.accept(val, context);
    }

    private <S, T> Optional<T> safeCast(final S candidate, Class<T>  targetClass)
    {
        return targetClass.isInstance(candidate)
            ? Optional.of(targetClass.cast(candidate))
            : Optional.empty();
    }

    /**
     * Acknowledge given message
     *
     * @param channel the channel to send acknowledgment on
     * @param context the context to use for ack
     * @param ack the {@link AckStrategy} to use
     */
    private void acknowledge(final Channel channel,
                             final MessageContext context,
                             final AckStrategy ack)
    {
        try
        {
            final String messageId = context.getProperties().getMessageId();
            final long deliveryTag = context.getReceivedInfo().getDeliveryTag();
            log.info("Acking [messageId, deliveryTag] : {}, {}" , messageId, deliveryTag);

            ack.accept(channel, deliveryTag);
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
     * Add new handler bound to a specific type
     *
     * @param clazz
     * @param handler
     * @param <T>
     */
    public synchronized <T> void addHandler(final Class<T> clazz, final BiConsumer<T, MessageContext> handler)
    {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(handler);
        Class<? super T> clz = clazz;

        while (true)
        {
            if (clz == Object.class)
                break;

            MethodReference reference = new MethodReference<>();
            reference.handler = handler;
            reference.root = clazz;
            handlers.put(clz, reference);
            clz = clz.getSuperclass();
        }
    }


    @Override
    public void handleCancel(final String consumerTag)
    {
        // TODO : consumer has been cancelled unexpectedly
        throw new HoplinRuntimeException("Not yet implemented");
    }

    private class MethodReference<T>
    {
        Class<T> root;
        BiConsumer<T, MessageContext> handler;
    }

}
