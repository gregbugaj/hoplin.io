package io.hoplin;

import com.rabbitmq.client.AMQP;
import io.hoplin.util.ClassUtil;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Basic exchange client interface
 *
 * @see io.hoplin.TopicExchangeClient
 * @see io.hoplin.DirectExchangeClient
 * @see io.hoplin.FanoutExchangeClient
 * @see io.hoplin.HeaderExchangeClient
 */
public interface ExchangeClient
{
    /**
     * Publish message to the queue with default routing key
     *
     * @param message the message to publish
     * @param <T>
     */
    <T> void publish(final T message);

    /**
     * Publish message to the queue with default routing key and supplied headers
     * @param message the message to publish
     * @param cfg the configurations associated with each message
     * @param <T> the type of the message to publish
     */

    <T> void publishAsync(final T message, final Consumer<MessageConfiguration> cfg);

    /**
     * Publish message to the queue with defined routingKey
     *
     * @param message the message to publish
     * @param routingKey the routing key to associate the message with
     * @param <T> the type of the message to publish
     */
    <T> void publish(final T message, final String routingKey);

    /**
     * Publish message to the queue with defined routingKey and headers
     *
     * @param message the message to publish
     * @param routingKey the routing key to associate the message with
     * @param headers the headers to add to the {@link AMQP.BasicProperties}
     * @param <T> the type of the message to publish
     */
    <T> void publish(final T message, final String routingKey, final Map<String, Object> headers);

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
    <T> void subscribe(final Class<T> clazz, final Consumer<T> handler);

    /**
     * Add subscription and consume messages from the queue
     * Calling this method repeatably will only initialize consumer once to make sure that the Consumer is setup.
     * After that this method will only add the handlers
     *
     * Handlers should not block.
     *
     * @param clazz the class type that we are interested in receiving messages for
     * @param handler the Consumer that will handle the message
     * @param <T> the type this Consumer will handle
     */
    <T, U> void subscribe(final Class<T> clazz, final BiConsumer<T, MessageContext> handler);

    /**
     * Create instance of {@link ExchangeClient}
     *
     * @param type
     * @param options
     * @param binding
     * @return
     */
    static ExchangeClient create(final ExchangeType type, final RabbitMQOptions options, final Binding binding)
    {
        switch (type)
        {

            case DIRECT:
                return DirectExchangeClient.create(options, binding);
            case FANOUT:
                return FanoutExchangeClient.create(options, binding);
            case TOPIC:
                return TopicExchangeClient.create(options, binding);
            case HEADER:
                return null;
        }

        throw new HoplinRuntimeException("Unhandled exchange type : "+ type);
    }

    /**
     * Create new {@link TopicExchangeClient}
     * @see  TopicExchangeClient#create(RabbitMQOptions, String, String, String)
     */
    static ExchangeClient topic(final RabbitMQOptions options, final Binding binding)
    {
        return TopicExchangeClient.create(options, binding);
    }

    /**
     * Create new {@link TopicExchangeClient}
     * @see  TopicExchangeClient#create(RabbitMQOptions, String)}
     */
    static ExchangeClient topic(final RabbitMQOptions options, final String exchange)
    {
        return TopicExchangeClient.create(options, exchange);
    }

    /**
     * Create new {@link TopicExchangeClient} client, this will create default RabbitMQ queues.
     * @see TopicExchangeClient#topic(RabbitMQOptions, Binding)
     */
    static ExchangeClient topic(final RabbitMQOptions options, final String exchangeName, final String bindingKey)
    {
        return topic(options, exchangeName, "", bindingKey);
    }

    /**
     * Create new {@link TopicExchangeClient} client, this will create default RabbitMQ queues.
     * @see TopicExchangeClient#topic(RabbitMQOptions, Binding)
     */
    static ExchangeClient topic(final RabbitMQOptions options, final String exchangeName, final String queue, final String bindingKey)
    {
        Objects.requireNonNull(options);
        return new TopicExchangeClient(options, TopicExchangeClient.createSensibleBindings(exchangeName, queue, bindingKey));
    }

    /**
     * Create new {@link DirectExchangeClient}
     * @see  DirectExchangeClient#create(RabbitMQOptions, String, String, String)
     */
    static ExchangeClient direct(final RabbitMQOptions options, final Binding binding)
    {
        return DirectExchangeClient.create(options, binding);
    }

    /**
     * Create new {@link TopicExchangeClient}
     * @see  DirectExchangeClient#create(RabbitMQOptions, String)}
     */
    static ExchangeClient direct(final RabbitMQOptions options, final String exchange)
    {
        return DirectExchangeClient.create(options, exchange);
    }

    /**
     * Create new {@link DirectExchangeClient} client, this will create default RabbitMQ queues.
     * @see DirectExchangeClient#create(RabbitMQOptions, Binding)
     */
    static ExchangeClient direct(final RabbitMQOptions options, final String exchangeName, final String bindingKey)
    {
        return direct(options, exchangeName, "", bindingKey);
    }

    /**
     * Create new {@link DirectExchangeClient} client, this will create default RabbitMQ queues.
     * @see DirectExchangeClient#create(RabbitMQOptions, Binding)
     */
    static ExchangeClient direct(final RabbitMQOptions options, final String exchangeName, final String queue, final String bindingKey)
    {
        Objects.requireNonNull(options);
        return new DirectExchangeClient(options, DirectExchangeClient.createSensibleBindings(exchangeName, queue, bindingKey));
    }


    /**
     * Create new {@link FanoutExchangeClient}
     * @see  FanoutExchangeClient#create(RabbitMQOptions, String)}
     */
    static ExchangeClient fanout(final RabbitMQOptions options, final String exchange)
    {
        return FanoutExchangeClient.create(options, exchange);
    }

    /**
     * Create new {@link FanoutExchangeClient} client, this will create default RabbitMQ queues.
     * @see FanoutExchangeClient#topic(RabbitMQOptions, Binding)
     */
    static ExchangeClient fanout(final RabbitMQOptions options, final Binding binding)
    {
        return FanoutExchangeClient.create(options, binding);
    }

    /**
     * Create new {@link TopicExchangeClient}, exchange name will be determined based on caller class name
     */
    static ExchangeClient topic(final RabbitMQOptions options)
    {
        final String caller = ClassUtil.getMainClass();
        if(caller == null)
            throw new IllegalArgumentException("Unable to determine exchange name");

        return topic(options, caller);
    }
}
