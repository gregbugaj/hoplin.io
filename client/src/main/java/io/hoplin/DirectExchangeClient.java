package io.hoplin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 *  Direct exchange client
 */
public class DirectExchangeClient extends AbstractExchangeClient
{
    private static final Logger log = LoggerFactory.getLogger(DirectExchangeClient.class);

    public DirectExchangeClient(final RabbitMQOptions options, final Binding binding)
    {
        this(options, binding, false);
    }

    public DirectExchangeClient(final RabbitMQOptions options, final Binding binding, boolean consume)
    {
        super(options, binding);
        bind(consume, "direct");
    }

    /**
     * Create new {@link DirectExchangeClient}
     *
     * @param options the connection options to use
     * @param binding the {@link Binding} to use
     * @return new Direct Exchange client setup in server mode
     */
    public static ExchangeClient publisher(final RabbitMQOptions options, final Binding binding)
    {
        Objects.requireNonNull(options);
        Objects.requireNonNull(binding);

        return new DirectExchangeClient(options, binding);
    }

    /**
     * Create new {@link DirectExchangeClient}
     *
     * @param options the connection options to use
     * @param exchange the exchange to use
     * @return
     */
    public static ExchangeClient publisher(final RabbitMQOptions options, final String exchange)
    {
        Objects.requireNonNull(options);
        Objects.requireNonNull(exchange);

        // Producer does not bind to the queue only to the exchange when using DirectExchange
        final Binding binding = BindingBuilder
                .bind()
                .to(new DirectExchange(exchange))
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
    public static ExchangeClient subscriber(final RabbitMQOptions options, final String exchangeName, final String... bindingKeys)
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
    public static ExchangeClient subscriberWithQueue(final RabbitMQOptions options, final String exchangeName, final String queue, final String... bindingKeys)
    {
        Objects.requireNonNull(options);
        Objects.requireNonNull(exchangeName);
        Objects.requireNonNull(bindingKeys);

        final Binding binding = BindingBuilder
                .bind(new Queue(queue))
                .to(new DirectExchange(exchangeName))
                .withAutoAck(true)
                .with(String.join(",", bindingKeys));

        return new DirectExchangeClient(options, binding, true);
    }
}
