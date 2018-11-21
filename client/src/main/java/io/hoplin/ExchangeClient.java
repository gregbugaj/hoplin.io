package io.hoplin;

import com.rabbitmq.client.AMQP;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Basic exchange client interface
 *
 * @see io.hoplin.TopicExchangeClient
 * @see io.hoplin.DirectExchangeClient
 * @see io.hoplin.FanoutExchangeClient
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
     * @param headers the headers to add to the {@link AMQP.BasicProperties}
     * @param <T> the type of the message to publish
     */
    <T> void publish(final T message, final Map<String, Object> headers);

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
     * After that this method  will only add the handlers
     *
     * Handlers should not block.
     *
     * @param clazz the class type that we are interested in receiving messages for
     * @param handler the Consumer that will handle the message
     * @param <T> the type this Consumer will handle
     */
    <T, U> void subscribe(final Class<T> clazz, final BiConsumer<T, MessageContext> handler);
}
