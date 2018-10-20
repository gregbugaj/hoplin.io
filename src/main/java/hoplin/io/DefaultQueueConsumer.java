package hoplin.io;

import com.google.common.collect.ArrayListMultimap;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import hoplin.io.json.JsonCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Default consumer with default ACK stategy
 */
public class DefaultQueueConsumer extends DefaultConsumer
{
    private static final Logger log = LoggerFactory.getLogger(DefaultQueueConsumer.class);

    private final QueueOptions queueOptions;

    private ArrayListMultimap<Class, Consumer> handlers = ArrayListMultimap.create();

    private Executor executor;

    private JsonCodec codec;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     * @param queueOptions the options to use for this queue consumer
     */
    public DefaultQueueConsumer(final Channel channel, final QueueOptions queueOptions)
    {
        super(channel);

        this.queueOptions = Objects.requireNonNull(queueOptions);
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        codec = new JsonCodec();
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
    @Override
    public void handleDelivery(final String consumerTag, final Envelope envelope, AMQP.BasicProperties properties, byte[] body)
            throws IOException
    {
        final AcknowledgmentHandler acknowledgmentHandler = new DefaultAcknowledgmentHandler();

        try
        {
            CompletableFuture.supplyAsync(()-> {

                final MessagePayload message = codec.deserialize(body, MessagePayload.class);
                final Object val = message.getPayload();
                final Class<?> typeAsClass = message.getTypeAsClass();
                final Collection<Consumer> consumers = handlers.get(typeAsClass);
                final List<Throwable> exceptions = new ArrayList<>();
                int handlersTotal = consumers.size();
                int handlersSucceeded = 0;
                int handlersFailed = 0;

                for(final Consumer handler : consumers)
                {
                    try
                    {
                        handler.accept(val);
                        ++handlersSucceeded;
                    }
                    catch (final Exception e)
                    {
                        ++handlersFailed;
                        exceptions.add(e);
                        log.error("Handler error for message  : " + message, e);
                    }
                }

                return new MessageHandlingResult(message, exceptions, handlersTotal, handlersSucceeded, handlersFailed);
            }, executor).whenComplete((result, thr)-> acknowledgmentHandler.acknowledge(getChannel(), consumerTag, envelope, result, queueOptions)
            );

        }
        catch(final Exception e)
        {
            log.error("Unable to process message", e);
        }
    }

    private static class MessageHandlingResult
    {
        private int handlersTotal = 0;
        private int handlersSucceeded = 0;
        private int handlersFailed = 0;
        private MessagePayload message;
        private List<Throwable> exceptions;

        public MessageHandlingResult(final MessagePayload message, final List<Throwable> exceptions,
                                     final int handlersTotal, final int handlersSucceeded, final int handlersFailed)
        {
            this.message = message;
            this.exceptions = exceptions;
            this.handlersTotal = handlersTotal;
            this.handlersFailed = handlersFailed;
            this.handlersSucceeded = handlersSucceeded;
        }

        /**
         * All handlers have succeeded without failures
         * @return
         */
        public boolean hasAllHandlersSucceeded()
        {
            return handlersTotal == handlersSucceeded;
        }

        /**
         * Some of the handlers have succeeded without failues
         * @return
         */
        public boolean hasPartialHandlersSucceeded()
        {
            return handlersTotal > 0 && handlersSucceeded > 0;
        }

        /**
         * Errors occured during processing
         * 
         * @return
         */
        public boolean hasErrors()
        {
            return handlersFailed > 0 ;
        }
    }

    /**
     * Add new handler bound to a specific type
     * @param clazz
     * @param handler
     * @param <T>
     */
    public <T> void addHandler(final Class<T> clazz, final Consumer<T> handler)
    {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(handler);

        handlers.put(clazz, handler);
    }

    /**
     * Acknowledgement handling strategy
     */
    @FunctionalInterface
    private interface AcknowledgmentHandler
    {
      void acknowledge(final Channel channel,
                       final String consumerTag,
                       final Envelope envelope,
                       final MessageHandlingResult result,
                       final QueueOptions queueOptions);
    }

    /**
     * Default acknowledgment strategy
     */
    private class DefaultAcknowledgmentHandler implements AcknowledgmentHandler
    {

        @Override
        public void acknowledge(final Channel channel, final String consumerTag,
                                final Envelope envelope,
                                final MessageHandlingResult result,
                                final QueueOptions queueOptions)
        {
            if(result.hasAllHandlersSucceeded())
                handleAck(channel, envelope.getDeliveryTag(), queueOptions.isAutoAck());
            else
                handleExceptionally(consumerTag, envelope, result);
        }

        private void handleAck(final Channel channel, final long deliveryTag, final boolean autoAck)
        {
            if(autoAck)
                return;

            // manual acknowledgments
            // ACK that the message have been delivered and processed successfully
            try
            {
                channel.basicAck(deliveryTag, false);
            }
            catch(final Exception e)
            {
                try
                {
                    channel.basicNack(deliveryTag, false, true);
                }
                catch (Exception e1)
                {
                    log.error("Unable to NACK", e1);
                }
            }
        }

        private void handleExceptionally(final String consumerTag, final Envelope envelope, final MessageHandlingResult message)
        {
            try
            {
                // TODO : Handle exception handling
            }
            catch (final Exception e)
            {
                log.error("exception handling error : " +consumerTag, e);
            }
        }

    }
}
