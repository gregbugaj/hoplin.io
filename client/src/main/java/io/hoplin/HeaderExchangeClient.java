package io.hoplin;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Default header exchange client
 */
public class HeaderExchangeClient extends AbstractExchangeClient
{
    private static final Logger log = LoggerFactory.getLogger(HeaderExchangeClient.class);

    public HeaderExchangeClient(final RabbitMQOptions options, final Binding binding)
    {
        this(options, binding, false);
    }

    public HeaderExchangeClient(final RabbitMQOptions options, final Binding binding, boolean consume)
    {
        super(options, binding);
        bind(consume, "header");
    }
    /**
     * Create new {@link HeaderExchangeClient}
     *
     * @param options the connection options to use
     * @param binding the {@link Binding} to use
     * @return new Header Exchange client setup in publisher mode
     */
    public static ExchangeClient publisher(final RabbitMQOptions options, final Binding binding)
    {
        Objects.requireNonNull(options);
        Objects.requireNonNull(binding);

        return new HeaderExchangeClient(options, binding);
    }

    /**
     * Create new {@link HeaderExchangeClient}
     *
     * @param options the connection options to use
     * @param exchange the exchange to use
     * @return
     */
    public static ExchangeClient publisher(final RabbitMQOptions options, final String exchange, Map<String, String> arguments)
    {
        Objects.requireNonNull(options);
        Objects.requireNonNull(exchange);

        // Producer does not bind to the queue only to the exchange when using HeaderExchange
        final Binding binding = BindingBuilder
                .bind()
                .to(new HeaderExchange(exchange))
                .bind();

        return publisher(options, binding);
    }

}
