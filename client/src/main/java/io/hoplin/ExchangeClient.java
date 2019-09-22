package io.hoplin;

import io.hoplin.util.ClassUtil;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Exchange client interface
 *
 * <p>
 * Primary operations provided by this interface include client creation {@link
 * #create(RabbitMQOptions, Binding, ExecutorService)}, subscription {@link #subscribe(String,
 * Class, Consumer)} and publishing {@link #publish(Object)}. Most methods have both synchronous and
 * asynchronous versions
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
   * @param options  the connection options
   * @param binding  the {@link Binding} to use
   * @param executor the {@link ExecutorService} to use
   * @return new {@link ExchangeClient}
   */
  static ExchangeClient create(final RabbitMQOptions options, final Binding binding,
      final ExecutorService executor) {
    final String exchange = binding.getExchange();
    switch (ExchangeType.fromValue(exchange)) {
      case DIRECT:
        return DirectExchangeClient.create(options, binding, executor);
      case FANOUT:
        return FanoutExchangeClient.create(options, binding, executor);
      case TOPIC:
        return TopicExchangeClient.create(options, binding, executor);
      case HEADER:
        return HeaderExchangeClient.create(options, binding, executor);
    }

    throw new HoplinRuntimeException("Unhandled exchange type : " + exchange);
  }

  /**
   * Create new {@link TopicExchangeClient}
   *
   * @see TopicExchangeClient#create(RabbitMQOptions, Binding, ExecutorService)
   */
  static ExchangeClient topic(final RabbitMQOptions options, final Binding binding,
      final ExecutorService executor) {
    return TopicExchangeClient.create(options, binding, executor);
  }

  /**
   * Create new {@link TopicExchangeClient}
   *
   * @see TopicExchangeClient#create(RabbitMQOptions, Binding, ExecutorService)
   */
  static ExchangeClient topic(final RabbitMQOptions options, final Binding binding) {
    return TopicExchangeClient.create(options, binding, createExecutor());
  }

  /**
   * Create new {@link TopicExchangeClient}
   *
   * @see TopicExchangeClient#create(RabbitMQOptions, String, ExecutorService)}
   */
  static ExchangeClient topic(final RabbitMQOptions options, final String exchange,
      final ExecutorService executor) {
    return TopicExchangeClient.create(options, exchange, executor);
  }

  /**
   * Create new {@link TopicExchangeClient}
   *
   * @see TopicExchangeClient#create(RabbitMQOptions, String, ExecutorService)}
   */
  static ExchangeClient topic(final RabbitMQOptions options, final String exchange) {
    return TopicExchangeClient.create(options, exchange, createExecutor());
  }

  /**
   * Create new {@link TopicExchangeClient} client, this will create default RabbitMQ queues.
   *
   * @see TopicExchangeClient#topic(RabbitMQOptions, String, String, String, ExecutorService)
   */
  static ExchangeClient topic(final RabbitMQOptions options, final String exchangeName,
      final String bindingKey) {
    return topic(options, exchangeName, "", bindingKey, createExecutor());
  }

  /**
   * Create new {@link TopicExchangeClient} client, this will create default RabbitMQ queues.
   *
   * @see TopicExchangeClient#topic(RabbitMQOptions, String, String, String, ExecutorService)
   */
  static ExchangeClient topic(final RabbitMQOptions options, final String exchangeName,
      final String bindingKey, final ExecutorService executor) {
    return topic(options, exchangeName, "", bindingKey, executor);
  }

  /**
   * Create new {@link TopicExchangeClient} client, this will create default RabbitMQ queues.
   *
   * @see TopicExchangeClient#topic(RabbitMQOptions, Binding, ExecutorService)
   */
  static ExchangeClient topic(final RabbitMQOptions options, final String exchangeName,
      final String queue, final String bindingKey, final ExecutorService executor) {
    return new TopicExchangeClient(options,
        TopicExchangeClient.createSensibleBindings(exchangeName, queue, bindingKey), executor);
  }

  /**
   * Create new {@link TopicExchangeClient} client.
   *
   * @see TopicExchangeClient#topic(RabbitMQOptions, String, String, String, ExecutorService)
   */
  static ExchangeClient topic(final RabbitMQOptions options, final String exchangeName,
      final String queue, final String bindingKey) {
    return topic(options, exchangeName, queue, bindingKey, createExecutor());
  }

  /**
   * Create new {@link DirectExchangeClient}
   *
   * @see DirectExchangeClient#create(RabbitMQOptions, String, String, String, ExecutorService)
   */
  static ExchangeClient direct(final RabbitMQOptions options, final Binding binding,
      final ExecutorService executor) {
    return DirectExchangeClient.create(options, binding, executor);
  }

  /**
   * Create new {@link DirectExchangeClient}
   *
   * @see DirectExchangeClient#create(RabbitMQOptions, String, ExecutorService)}
   */
  static ExchangeClient direct(final RabbitMQOptions options, final String exchange) {
    return DirectExchangeClient.create(options, exchange, ExchangeClient.createExecutor());
  }

  /**
   * Create new {@link DirectExchangeClient}
   *
   * @see DirectExchangeClient#create(RabbitMQOptions, String, ExecutorService)}
   */
  static ExchangeClient direct(final RabbitMQOptions options, final String exchange,
      final ExecutorService executor) {
    return DirectExchangeClient.create(options, exchange, executor);
  }

  /**
   * Create new {@link DirectExchangeClient} client.
   *
   * @see DirectExchangeClient#create(RabbitMQOptions, String, String, String, ExecutorService)
   */
  static ExchangeClient direct(final RabbitMQOptions options, final String exchangeName,
      final String bindingKey, final ExecutorService executor) {
    return direct(options, exchangeName, "", bindingKey, executor);
  }

  /**
   * Create new {@link DirectExchangeClient} client.
   *
   * @see DirectExchangeClient#create(RabbitMQOptions, Binding, ExecutorService)
   */
  static ExchangeClient direct(final RabbitMQOptions options, final String exchangeName,
      final String bindingKey) {
    return direct(options, exchangeName, "", bindingKey, createExecutor());
  }

  /**
   * Create new {@link DirectExchangeClient} client.
   *
   * @see DirectExchangeClient#create(RabbitMQOptions, Binding, ExecutorService)
   */
  static ExchangeClient direct(final RabbitMQOptions options, final String exchangeName,
      final String queue, final String bindingKey, final ExecutorService executor) {
    return new DirectExchangeClient(options,
        DirectExchangeClient.createSensibleBindings(exchangeName, queue, bindingKey), executor);
  }

  /**
   * Create new {@link DirectExchangeClient} client.
   *
   * @see DirectExchangeClient#create(RabbitMQOptions, Binding, ExecutorService)
   */
  static ExchangeClient direct(final RabbitMQOptions options, final String exchangeName,
      final String queue, final String bindingKey) {
    return direct(options, exchangeName, queue, bindingKey, createExecutor());
  }

  /**
   * Create new {@link FanoutExchangeClient}
   *
   * @param options  the {@link RabbitMQOptions} to use
   * @param exchange the exchange name to bind to
   * @param executor the the {@link ExecutorService} to use
   * @return new {@link FanoutExchangeClient}
   * @see FanoutExchangeClient#create(RabbitMQOptions, String, ExecutorService)}
   */
  static ExchangeClient fanout(final RabbitMQOptions options, final String exchange,
      final ExecutorService executor) {
    return FanoutExchangeClient.create(options, exchange, executor);
  }

  /**
   * Create new {@link FanoutExchangeClient}
   *
   * @see FanoutExchangeClient#create(RabbitMQOptions, String, ExecutorService)}
   */
  static ExchangeClient fanout(final RabbitMQOptions options, final String exchange) {
    return FanoutExchangeClient.create(options, exchange, createExecutor());
  }

  /**
   * Create new {@link FanoutExchangeClient}
   *
   * @see FanoutExchangeClient#create(RabbitMQOptions, Binding, ExecutorService)}
   */
  static ExchangeClient fanout(final RabbitMQOptions options, final Binding binding,
      final ExecutorService executor) {
    return FanoutExchangeClient.create(options, binding, executor);
  }

  /**
   * Create new {@link FanoutExchangeClient}
   *
   * @see FanoutExchangeClient#create(RabbitMQOptions, String, ExecutorService)}
   */
  static ExchangeClient fanout(final RabbitMQOptions options, final Binding binding) {
    return FanoutExchangeClient.create(options, binding, createExecutor());
  }

  /**
   * Create new {@link TopicExchangeClient} Exchange name will be determined based on caller class
   * name
   */
  static ExchangeClient topic(final RabbitMQOptions options, final ExecutorService executor) {
    return topic(options, ClassUtil.getRootPackageName(), executor);
  }

  /**
   * Create new {@link TopicExchangeClient} Exchange name will be determined based on caller class
   * name
   */
  static ExchangeClient topic(final RabbitMQOptions options) {
    return topic(options, ClassUtil.getRootPackageName(), createExecutor());
  }

  /**
   * Create new {@link HeaderExchangeClient}
   *
   * @see HeaderExchangeClient#create(RabbitMQOptions, Binding, ExecutorService)
   */
  static ExchangeClient header(final RabbitMQOptions options, final Binding binding,
      final ExecutorService executor) {
    return HeaderExchangeClient.create(options, binding, executor);
  }

  /**
   * Create new {@link HeaderExchangeClient}
   *
   * @see HeaderExchangeClient#create(RabbitMQOptions, Binding, ExecutorService)
   */
  static ExchangeClient header(final RabbitMQOptions options, final Binding binding) {
    return HeaderExchangeClient.create(options, binding, createExecutor());
  }

  /**
   * Create new {@link HeaderExchangeClient}
   *
   * @see HeaderExchangeClient#create(RabbitMQOptions, String, ExecutorService)}
   */
  static ExchangeClient header(final RabbitMQOptions options, final String exchange,
      final ExecutorService executor) {
    return HeaderExchangeClient.create(options, exchange, executor);
  }

  /**
   * Create new {@link HeaderExchangeClient}
   *
   * @see HeaderExchangeClient#create(RabbitMQOptions, String, ExecutorService)}
   */
  static ExchangeClient header(final RabbitMQOptions options, final String exchange) {
    return HeaderExchangeClient.create(options, exchange, createExecutor());
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
   * create new instance of {@link ExecutorService}
   *
   * @return
   */
  static ExecutorService createExecutor() {
    return Executors.newCachedThreadPool(new HoplinThreadFactory());
  }
}
