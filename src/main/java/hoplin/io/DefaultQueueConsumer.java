package hoplin.io;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import hoplin.io.json.JsonCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class DefaultQueueConsumer extends DefaultConsumer
{
    private final QueueOptions queueOptions;

    private Set<HandlerReference> handlers = new LinkedHashSet<>();

    private static final Logger log = LoggerFactory.getLogger(DefaultQueueConsumer.class);

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
        final String message = new String(body, "UTF-8");
        System.out.println(" [x] Received '" + message + "'" + " consumerTag = " + consumerTag + " :: " + envelope);

        final long deliveryTag = envelope.getDeliveryTag();
        final boolean autoAck = queueOptions.isAutoAck();

        try
        {
            for(final HandlerReference ref : handlers)
            {
                final Object val = codec.deserialize(body, ref.clazz);
                final Consumer<Object> handler = ref.handler;

                handler.accept(val);
            }

            // manual acknowledgments
            // ACK that the message have been delivered and processed successfully
            if(!autoAck)
                getChannel().basicAck(deliveryTag, false);
        }
        catch(final Exception e)
        {
            getChannel().basicNack(deliveryTag, false, true);
            throw e;
        }
        finally
        {
            log.info(" [x] Done");
        }
    }

    public <T> void addHandler(final Class<T> clazz, final Consumer<T> handler)
    {
        final HandlerReference ref = new HandlerReference<>();
        ref.clazz = clazz;
        ref.handler = handler;

        handlers.add(ref);
    }

    private static class HandlerReference<T>
    {
        private Class<T> clazz;

        private Consumer<T> handler;
    }
}
