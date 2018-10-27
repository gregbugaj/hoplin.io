package hoplin.io;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Base exchange client
 */
abstract class AbstractExchangeClient
{
    private static final Logger log = LoggerFactory.getLogger(AbstractExchangeClient.class);

    private static String DEFAULT_ERROR_EXCHANGE = "hoplin_default_error_exchange";

    Binding binding;

    RabbitMQClient client;

    AbstractExchangeClient(final RabbitMQOptions options, final Binding binding)
    {
        Objects.requireNonNull(options);
        Objects.requireNonNull(binding);

        this.client = RabbitMQClient.create(options);
        this.binding = binding;

        setupErrorHandling();
    }

    /**
     * setup error handling queues
     */
    private void setupErrorHandling()
    {
        final String exchangeName = DEFAULT_ERROR_EXCHANGE;
        // survive a server restart
        final boolean durable = true;
        // keep it even if not in user
        final boolean autoDelete = false;
        final String type = "direct";

        try
        {
            // Make sure that the Exchange is declared
            client.exchangeDeclare(exchangeName, type, durable, autoDelete);
        }
        catch(final Exception e)
        {
            log.error("Unable to declare error exchange", e);
            throw new HoplinRuntimeException("Unable to declare error exchange", e);
        }
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
            // survive a server restart
            final boolean durable = true;
            // keep it even if not in user
            final boolean autoDelete = false;

            // Make sure that the Exchange is declared
            client.exchangeDeclare(exchangeName, type, durable, autoDelete);

            // setup consumer options
            if (consume)
                subscribe();
        }
        catch (final Exception e)
        {
            throw new HoplinRuntimeException("Unable to bind to queue", e);
        }
    }

    /**
     * Add subscription and consume messages from the queue
     * Calling this method repeatably will only initialize consumer once to make sure that the Consumer is setup.
     * After that this method  will only add the handlers
     *
     * Handlers should not block.
     *
     * @param clazz the class type that we are interested in receiving messages for
     * @param handler the Consumer that will handle the message
     * @param <T> the type this Consumer will handle
     */
    public <T> void subscribe(final Class<T> clazz, final Consumer<T> handler)
    {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(handler);

        client.basicConsume(binding.getQueue(), clazz, handler);
    }

    public <T, U> void subscribe(final Class<T> clazz, final  BiConsumer<T, MessageContext> handler)
    {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(handler);

        client.basicConsume(binding.getQueue(), clazz, handler);
    }
}
