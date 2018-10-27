package hoplin.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Topic exchanges route messages to queues based on wildcard matches
 * between the routing key and routing pattern specified by the queue binding.
 * Messages are routed to one or many queues based on a matching between a message routing key and this pattern.
 */
public class TopicExchangeClient extends  AbstractExchangeClient
{
    private static final Logger log = LoggerFactory.getLogger(TopicExchangeClient.class);

    public TopicExchangeClient(final RabbitMQOptions options, final Binding binding)
    {
        this(options, binding, false);
    }

    public TopicExchangeClient(final RabbitMQOptions options, final Binding binding, boolean consume)
    {
        super(options, binding);
        bind(consume, "topic");
    }

    /**
     * Publish message to the queue with defined routingKey
     *
     * @param message
     * @param routingKey
     */
    public <T> void publish(final T message, final String routingKey)
    {
        client.basicPublish(binding.getExchange(), routingKey, message);
    }

    /**
     * Create new {@link DirectExchangeClient}
     *
     * @param options the connection options to use
     * @param binding the {@link Binding} to use
     * @return new Direct Exchange client setup in server mode
     */
    public static TopicExchangeClient publisher(final RabbitMQOptions options, final Binding binding)
    {
        Objects.requireNonNull(options);
        Objects.requireNonNull(binding);

        return new TopicExchangeClient(options, binding);
    }

    /**
     * Create new {@link DirectExchangeClient}
     *
     * @param options the connection options to use
     * @param exchange the exchange to use
     * @return
     */
    public static TopicExchangeClient publisher(final RabbitMQOptions options, final String exchange)
    {
        Objects.requireNonNull(options);
        Objects.requireNonNull(exchange);

        // Producer does not bind to the queue only to the exchange when using DirectExchange
        final Binding binding = BindingBuilder
                .bind()
                .to(new TopicExchange(exchange))
                .with(""); // no-routing key required for publishers

        return publisher(options, binding);
    }

    /**
     * Create new {@link DirectExchangeClient} client, this will create default RabbitMQ queues.
     * That will be in the format of <strong>Queue amq.gen-n47yBmzL1UODRg7obXV7UQ</strong>
     *
     * @param options the options used for connection
     * @param exchangeName the exchangeName to use
     * @param bindingKeys the bindingKeys to bind to the exchange
     * @return new DirectExchangeClient
     */
    public static TopicExchangeClient subscriber(final RabbitMQOptions options, final String exchangeName, final String... bindingKeys)
    {
        return subscriberWithQueue(options, exchangeName, "", bindingKeys);
    }

    /**
     *Create new {@link DirectExchangeClient} client, this will create default RabbitMQ queues.
     *
     * @param options the options used for connection
     * @param exchangeName
     * @param exchangeName the exchangeName to use
     * @param bindingKeys the bindingKeys to bind to the exchange
     * @return new DirectExchangeClient
     */
    public static TopicExchangeClient subscriberWithQueue(final RabbitMQOptions options, final String exchangeName, final String queue, final String... bindingKeys)
    {
        Objects.requireNonNull(options);
        Objects.requireNonNull(exchangeName);
        Objects.requireNonNull(bindingKeys);

        final Binding binding = BindingBuilder
                .bind(new Queue(queue))
                .to(new TopicExchange(exchangeName))
                .withAutoAck(true)
                .with(String.join(",", bindingKeys));

        return new TopicExchangeClient(options, binding, true);
    }
}
