package io.hoplin;

import com.google.common.base.Strings;
import com.rabbitmq.client.AMQP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Base exchange client
 */
abstract class AbstractExchangeClient implements ExchangeClient
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

    SubscriptionResult subscribe()
    {
        final String exchangeName = binding.getExchange();
        String queueName = binding.getQueue();
        String routingKey = binding.getRoutingKey();
        final Map<String, Object> arguments = binding.getArguments();

        try
        {
            // binding
            String bindingKey = routingKey;
            if(routingKey == null)
                bindingKey = "";

            boolean autoDelete = false;
            if(Strings.isNullOrEmpty(queueName))
                autoDelete = true;

            // when the queue name is empty we will create a queue dynamically and bind to that queue
            final AMQP.Queue.DeclareOk queueDeclare = client
                    .queueDeclare(queueName, true, false, autoDelete, arguments);

            queueName = queueDeclare.getQueue();

            client.queueBind(queueName, exchangeName, bindingKey);
            log.info("Binding client [exchangeName, queueName, bindingKey, autoDelete] : {}, {}, {}, {}",
                     exchangeName,
                     queueName,
                     bindingKey,
                     autoDelete
                     );

            return new SubscriptionResult(exchangeName, queueName);
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
     */
    void bind(final String type)
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
            // keep it even if not in use
            final boolean autoDelete = false;

            final Map<String, Object> arguments = new HashMap<>();
            // Make sure that the Exchange is declared

            client.exchangeDeclare(exchangeName, type, durable, autoDelete, arguments);
        }
        catch (final Exception e)
        {
            throw new HoplinRuntimeException("Unable to bind to queue", e);
        }
    }

    @Override
    public <T> SubscriptionResult subscribe(final String subscriberId, final Class<T> clazz, final Consumer<T> handler)
    {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(handler);
        client.basicConsume(binding.getQueue(), clazz, handler);

        return null;
    }

    @Override
    public <T> SubscriptionResult subscribe(final String subscriberId, final Class<T> clazz, final  BiConsumer<T, MessageContext> handler)
    {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(handler);
        client.basicConsume(binding.getQueue(), clazz, handler);

        return null;
    }

    @Override
    public RabbitMQClient getMqClient()
    {
        return client;
    }

    @Override
    public <T> void publish(final T message)
    {
        publish(message, "", cfg->{});
    }

    @Override
    public <T> void publish(final T message, final String routingKey)
    {
        publish(message, routingKey, cfg->{});
    }

    @Override
    public <T> void publish(final T message, final Consumer<MessageConfiguration> cfg)
    {
        publish(message, "", cfg);
    }

    @Override
    public <T> void publish(final T message, final String routingKey, final Consumer<MessageConfiguration> cfg)
    {
        Objects.requireNonNull(message);
        Objects.requireNonNull(routingKey);
        Objects.requireNonNull(cfg);

        // Wrap our message original message
        final MessagePayload<T> payload = new MessagePayload<>(message);
        payload.setType(message.getClass());
        client.basicPublish(binding.getExchange(), routingKey, payload);
    }

    @Override
    public <T> CompletableFuture<Void> publishAsync(T message)
    {
        return publishAsync(message, "", cfg->{});
    }

    @Override
    public <T> CompletableFuture<Void> publishAsync(final T message, final String routingKey)
    {
        return publishAsync(message, routingKey, cfg->{});
    }

    @Override
    public <T> CompletableFuture<Void> publishAsync(final T message, final Consumer<MessageConfiguration> cfg)
    {
        return publishAsync(message, "", cfg);
    }

    @Override
    public <T> CompletableFuture<Void> publishAsync(final T message, final String routingKey, final Consumer<MessageConfiguration> cfg)
    {
        Objects.requireNonNull(message);
        Objects.requireNonNull(routingKey);
        Objects.requireNonNull(cfg);

        final CompletableFuture<Void> promise = new CompletableFuture<>();
        // Wrap our message original message
        final MessagePayload<T> payload = new MessagePayload<>(message);
        payload.setType(message.getClass());
        client.basicPublish(binding.getExchange(), "", payload);

        return promise;
    }
}
