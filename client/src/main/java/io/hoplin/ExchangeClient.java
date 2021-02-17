package io.hoplin;

import io.hoplin.util.ClassUtil;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Exchange client interface
 * <p>
 * Primary operations provided by this interface include client creation {@link
 * #create(RabbitMQOptions, Binding)}, subscription {@link #subscribe(String, Class, Consumer)} and
 * publishing {@link #publish(Object)}. Most methods have both synchronous and asynchronous
 * versions
 *
 * <h1>Client subscriptions</h1>
 *
 * <ul>
 *  <li>{@link #subscribe(String, Class, Consumer)}</li>
 *  <li>{@link #subscribe(String, Class, Function)}</li>
 *  <li>{@link #subscribe(String, Class, BiFunction)}</li>
 * </ul>
 *
 * @see io.hoplin.TopicExchangeClient
 * @see io.hoplin.DirectExchangeClient
 * @see io.hoplin.FanoutExchangeClient
 * @see io.hoplin.HeaderExchangeClient
 */
public interface ExchangeClient {

  /**
   * Create instance of {@link ExchangeClient}
   *
   * @param options the connection options
   * @param binding the binding to use
   * @return
   */
  static ExchangeClient create(final RabbitMQOptions options, final Binding binding) {
    final String exchange = binding.getExchange();
    switch (ExchangeType.fromValue(exchange)) {
      case DIRECT:
        return DirectExchangeClient.create(options, binding);
      case FANOUT:
        return FanoutExchangeClient.create(options, binding);
      case TOPIC:
        return TopicExchangeClient.create(options, binding);
      case HEADER:
        return HeaderExchangeClient.create(options, binding);
    }

    throw new HoplinRuntimeException("Unhandled exchange type : " + exchange);
  }

  /**
   * Create new {@link TopicExchangeClient}
   *
   * @see TopicExchangeClient#create(RabbitMQOptions, String, String, String)
   */
  static ExchangeClient topic(final RabbitMQOptions options, final Binding binding) {
    return TopicExchangeClient.create(options, binding);
  }

  /**
   * Create new {@link TopicExchangeClient}
   *
   * @see TopicExchangeClient#create(RabbitMQOptions, String)}
   */
  static ExchangeClient topic(final RabbitMQOptions options, final String exchange) {
    return TopicExchangeClient.create(options, exchange);
  }

  /**
   * Create new {@link TopicExchangeClient} client, this will create default RabbitMQ queues.
   *
   * @see TopicExchangeClient#topic(RabbitMQOptions, Binding)
   */
  static ExchangeClient topic(final RabbitMQOptions options, final String exchangeName,
      final String bindingKey) {
    return topic(options, exchangeName, "", bindingKey);
  }

  /**
   * Create new {@link TopicExchangeClient} client, this will create default RabbitMQ queues.
   *
   * @see TopicExchangeClient#topic(RabbitMQOptions, Binding)
   */
  static ExchangeClient topic(final RabbitMQOptions options, final String exchangeName,
      final String queue, final String bindingKey) {
    Objects.requireNonNull(options);
    return new TopicExchangeClient(options,
        TopicExchangeClient.createSensibleBindings(exchangeName, queue, bindingKey));
  }

  /**
   * Create new {@link DirectExchangeClient}
   *
   * @see DirectExchangeClient#create(RabbitMQOptions, String, String, String)
   */
  static ExchangeClient direct(final RabbitMQOptions options, final Binding binding) {
    return DirectExchangeClient.create(options, binding);
  }

  /**
   * Create new {@link TopicExchangeClient}
   *
   * @see DirectExchangeClient#create(RabbitMQOptions, String)}
   */
  static ExchangeClient direct(final RabbitMQOptions options, final String exchange) {
    return DirectExchangeClient.create(options, exchange);
  }

  /**
   * Create new {@link DirectExchangeClient} client, this will create default RabbitMQ queues.
   *
   * @see DirectExchangeClient#create(RabbitMQOptions, Binding)
   */
  static ExchangeClient direct(final RabbitMQOptions options, final String exchangeName,
      final String bindingKey) {
    return direct(options, exchangeName, "", bindingKey);
  }

  /**
   * Create new {@link DirectExchangeClient} client, this will create default RabbitMQ queues.
   *
   * @see DirectExchangeClient#create(RabbitMQOptions, Binding)
   */
  static ExchangeClient direct(final RabbitMQOptions options, final String exchangeName,
      final String queue, final String bindingKey) {
    Objects.requireNonNull(options);
    return new DirectExchangeClient(options,
        DirectExchangeClient.createSensibleBindings(exchangeName, queue, bindingKey));
  }

  /**
   * Create new {@link FanoutExchangeClient}
   *
   * @see FanoutExchangeClient#create(RabbitMQOptions, String)}
   */
  static ExchangeClient fanout(final RabbitMQOptions options, final String exchange) {
    return FanoutExchangeClient.create(options, exchange);
  }

  /**
   * Create new {@link FanoutExchangeClient} client, this will create default RabbitMQ queues.
   *
   * @see FanoutExchangeClient#topic(RabbitMQOptions, Binding)
   */
  static ExchangeClient fanout(final RabbitMQOptions options, final Binding binding) {
    return FanoutExchangeClient.create(options, binding);
  }

  /**
   * Create new {@link TopicExchangeClient}, exchange name will be determined based on caller class
   * name
   */
  static ExchangeClient topic(final RabbitMQOptions options) {
    final String caller = ClassUtil.getRootPackageName();
    return topic(options, caller);
  }

  /**
   * Create new {@link TopicExchangeClient}
   *
   * @see HeaderExchangeClient#create(RabbitMQOptions, Binding)
   */
  static ExchangeClient header(final RabbitMQOptions options, final Binding binding) {
    return HeaderExchangeClient.create(options, binding);
  }

  /**
   * Create new {@link TopicExchangeClient}
   *
   * @see TopicExchangeClient#create(RabbitMQOptions, String)}
   */
  static ExchangeClient header(final RabbitMQOptions options, final String exchange) {
    return HeaderExchangeClient.create(options, exchange);
  }

  /**
   * Get the underlying RabbitMq client
   *
   * @return
   */
  RabbitMQClient getMqClient();

  /**
   * Publish message to the queue with default routing key
   *
   * @param message the message to publish
   * @param <T>
   */
  <T> void publish(final T message);

  /**
   * Publish message to the queue with defined routingKey
   *
   * @param message    the message to publish
   * @param routingKey the routing key to associate the message with
   * @param <T>        the type of the message to publish
   */
  <T> void publish(final T message, final String routingKey);

  /**
   * Publish message to the queue with default routing key and supplied {@link
   * MessageConfiguration}
   *
   * @param message the message to publish
   * @param cfg     the configurations associated with each message
   * @param <T>
   */
  <T> void publish(final T message, final Consumer<MessageConfiguration> cfg);

  /**
   * Publish message to the queue with defined routingKey
   *
   * @param message    the message to publish
   * @param routingKey the routing key to associate the message with
   * @param cfg        the configurations associated with each message
   * @param <T>        the type of the message to publish
   */
  <T> void publish(final T message, final String routingKey,
      final Consumer<MessageConfiguration> cfg);

  /**
   * Publish message to the queue with default routing key and supplied {@link
   * MessageConfiguration}
   *
   * @param message the message to publish
   * @param <T>     the type of the message to publish
   */
  <T> CompletableFuture<Void> publishAsync(final T message);

  /**
   * Publish message to the queue with defined routingKey
   *
   * @param message    the message to publish
   * @param routingKey the routing key to associate the message with
   * @param <T>        the type of the message to publish
   */
  <T> CompletableFuture<Void> publishAsync(final T message, final String routingKey);

  /**
   * Publish message to the queue with default routing key and supplied {@link
   * MessageConfiguration}
   *
   * @param message the message to publish
   * @param cfg     the configurations associated with each message
   * @param <T>     the type of the message to publish
   */
  <T> CompletableFuture<Void> publishAsync(final T message,
      final Consumer<MessageConfiguration> cfg);

  /**
   * Publish message to the queue with defined routingKey
   *
   * @param message    the message to publish
   * @param routingKey the routing key to associate the message with
   * @param cfg        the configurations associated with each message
   * @param <T>        the type of the message to publish
   */
  <T> CompletableFuture<Void> publishAsync(final T message, final String routingKey,
      final Consumer<MessageConfiguration> cfg);

  /**
   * Add subscription and consume messages from the queue Calling this method repeatably will only
   * initialize consumer once to make sure that the Consumer is setup. After that this method  will
   * only add the handlers
   * <p>
   * Handlers should not block.
   *
   * @param subscriberId the unique id of the subscriber
   * @param clazz        the class type that we are interested in receiving messages for
   * @param handler      the Consumer that will handle the message
   * @param <T>          the type this Consumer will handle
   */
  <T> SubscriptionResult subscribe(final String subscriberId, final Class<T> clazz,
      final Consumer<T> handler);

  <T> SubscriptionResult subscribe(final String subscriberId, final Class<T> clazz,
      final BiConsumer<T, MessageContext> handler);

  /**
   * Add subscription and consume messages from the queue Calling this method repeatably will only
   * initialize consumer once to make sure that the Consumer is setup. After that this method will
   * only add the handlers.
   * <p>
   * Handlers should not block, and should execute on configured {@link
   * java.util.concurrent.ExecutorService}
   * </p>
   *
   * @param subscriberId the unique id of the subscriber
   * @param clazz        the class type that we are interested in receiving messages for
   * @param handler      the Consumer that will handle the message
   * @param <T>          the type this Consumer will handle
   * @return SubscriptionResult the result of subscription
   */
  <T> SubscriptionResult subscribe(final String subscriberId, final Class<T> clazz,
      final Function<T, Reply<?>> handler);

  /**
   * Add subscription and consume messages from the queue Calling this method repeatably will only
   * initialize consumer once to make sure that the Consumer is setup. After that this method will
   * only add the handlers.
   * <p>
   * Handlers should not block, and should execute on configured {@link
   * java.util.concurrent.ExecutorService}
   * </p>
   *
   * @param subscriberId the unique id of the subscriber
   * @param clazz        the class type that we are interested in receiving messages for
   * @param handler      the Consumer that will handle the message
   * @param <T>          the type this Consumer will handle
   * @return SubscriptionResult the result of subscription
   */
  <T> SubscriptionResult subscribe(final String subscriberId, final Class<T> clazz,
      final BiFunction<T, MessageContext, Reply<?>> handler);

  /**
   * Add subscription and consume messages from the queue Calling this method repeatably will only
   * initialize consumer once to make sure that the Consumer is setup. After that this method will
   * only add the handlers
   * <p>
   * Handlers should not block.
   *
   * @param clazz   the class type that we are interested in receiving messages for
   * @param handler the Consumer that will handle the message
   * @param config  the subscription configuration
   * @param <T>     the type this Consumer will handle
   * @return SubscriptionResult the result of subscription
   */
  <T> SubscriptionResult subscribe(final Class<T> clazz,
      final BiFunction<T, MessageContext, Reply<?>> handler,
      final Consumer<SubscriptionConfigurator> config);

  /**
   * Add subscription and consume messages from the queue Calling this method repeatably will only
   * initialize consumer once to make sure that the Consumer is setup. After that this method  will
   * only add the handlers
   * <p>
   * Handlers should not block.
   *
   * @param clazz   the class type that we are interested in receiving messages for
   * @param handler the Consumer that will handle the message
   * @param config  the subscription configuration
   * @param <T>     the type this Consumer will handle
   */
  <T> SubscriptionResult subscribe(final Class<T> clazz, final Consumer<T> handler,
      final Consumer<SubscriptionConfigurator> config);

  /**
   * Wait for all pending tasks to complete
   */
  void awaitQuiescence();

  /**
   * Wait for all pending tasks to complete within specified timeout
   *
   * @param time the time to wait
   * @param unit the time unit to wait
   */
  void awaitQuiescence(long time, TimeUnit unit);

  /**
   * Closes associated connections, handlers, executors
   */
  void close();

  /**
   * Wrap existing client in {@link AutoCloseable}
   *
   * @return io.hoplin.ClosableExchangeClient
   */
  CloseableExchangeClient asClosable();
}
