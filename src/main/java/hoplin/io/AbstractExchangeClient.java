package hoplin.io;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

abstract class AbstractExchangeClient
{
    private static final Logger log = LoggerFactory.getLogger(AbstractExchangeClient.class);

    Binding binding;

    RabbitMQClient client;

    AbstractExchangeClient(final RabbitMQOptions options, final Binding binding)
    {
        Objects.requireNonNull(options);
        Objects.requireNonNull(binding);

        this.client = RabbitMQClient.create(options);
        this.binding = binding;
    }

    void subscribe()
    {
        final String exchangeName = binding.getExchange();
        final String queueName = binding.getQueue();
        final String routingKey = binding.getRoutingKey();
        final Map<String, Object> arguments = binding.getArguments();
        final String[] bindingKeys = routingKey.split(",");

        try
        {
            client.queueDeclare(queueName, true, false, false, arguments);

            for (final String bindingKey : bindingKeys)
            {
                client.queueBind(queueName, exchangeName, bindingKey);
                log.info("Binding client [exchangeName, queueName, bindingKey] : {}, {}, {}",
                         exchangeName,
                         queueName,
                         bindingKey);
            }
        }
        catch (final Exception e)
        {
            throw new HoplinRuntimeException("Unable to setup consumer", e);
        }
    }

    /**
     * This will actively declare:
     *
     * a durable, non-autodelete exchange of "direct" type
     * a durable, non-exclusive, non-autodelete queue with a well-known name
     *
     * @param consume
     */
    void bind(final boolean consume, final String type)
    {
        Objects.requireNonNull(type);

        final String exchangeName = binding.getExchange();
        // prevent changing default queues
        if(Strings.isNullOrEmpty(exchangeName))
            throw new IllegalArgumentException("Exchange name can't be empty");

        try
        {
            // Make sure that the Exchange is declared
            client.exchangeDeclare(exchangeName, type, true, false);
            // setup consumer options
            if (consume)
                subscribe();
        }
        catch (final Exception e)
        {
            throw new HoplinRuntimeException("Unable to bind to queue", e);
        }
    }
}
