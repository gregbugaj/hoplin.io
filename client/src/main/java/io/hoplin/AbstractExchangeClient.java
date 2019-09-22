package io.hoplin;

import com.google.common.base.Strings;
import com.rabbitmq.client.AMQP;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base exchange client
 */
abstract class AbstractExchangeClient implements ExchangeClient {

  private static final Logger log = LoggerFactory.getLogger(AbstractExchangeClient.class);

  private static String DEFAULT_ERROR_EXCHANGE = "hoplin_default_error_exchange";

  Binding binding;

  RabbitMQClient client;

  AbstractExchangeClient(final RabbitMQOptions options, final Binding binding,
      final ExecutorService executor) {
    Objects.requireNonNull(options);
    Objects.requireNonNull(binding);
    Objects.requireNonNull(executor);
    this.client = RabbitMQClient.create(options, executor);
    this.binding = binding;
    setupErrorHandling();
  }

  /**
   * setup error handling queues
   */
  private void setupErrorHandling() {
    final String exchangeName = DEFAULT_ERROR_EXCHANGE;
    // survive a server restart
    final boolean durable = true;
    // keep it even if not in user
    final boolean autoDelete = false;
    final String type = "direct";

    try {
      // Make sure that the Exchange is declared
      client.exchangeDeclare(exchangeName, type, durable, autoDelete);
    } catch (final Exception e) {
      log.error("Unable to declare error exchange", e);
      throw new HoplinRuntimeException("Unable to declare error exchange", e);
    }
  }

  <T> SubscriptionResult subscribe(final SubscriptionConfig config, final Class<T> clazz) {
    Objects.requireNonNull(config, "Config can't be null");
    Objects.requireNonNull(clazz, "Handler can't be null");

    final String subscriberId = config.getSubscriberId();
    final String exchangeName = binding.getExchange();
    final Map<String, Object> arguments = binding.getArguments();

    String queueName = binding.getQueue();
    String routingKey = binding.getRoutingKey();

    try {
      // binding
      String bindingKey = routingKey;
      if (routingKey == null) {
        bindingKey = "";
      }

      boolean autoDelete = false;
      // we did not get a explicit queue name to bind to exchange so here we will determine that from the
      // supplied class name
      if (Strings.isNullOrEmpty(queueName)) {
        queueName = getQueueNameFromHandler(subscriberId, exchangeName, clazz);
        binding.setQueue(queueName);
      }

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
    } catch (final Exception e) {
      throw new HoplinRuntimeException("Unable to setup subscription consumer", e);
    }
  }

  /**
   * Generate queue name from supplied parameters Default format
   *
   * <pre>
   *     subscriber:exchange:class
   * </pre>
   *
   * @param subscriberId
   * @param exchange
   * @param clazz
   * @param <T>
   * @return
   */
  private <T> String getQueueNameFromHandler(final String subscriberId, final String exchange,
      final Class<T> clazz) {
    return String.format("%s:%s:%s", subscriberId, exchange, clazz.getName());
  }

  /**
   * This will actively declare:
   * <p>
   * a durable, non-autodelete exchange of "direct" type a durable, non-exclusive, non-autodelete
   * queue with a well-known name
   * </p>
   */
  void bind(final String type) {
    Objects.requireNonNull(type);
    final String exchangeName = binding.getExchange();

    // prevent changing default queues
    if (Strings.isNullOrEmpty(exchangeName)) {
      throw new IllegalArgumentException("Exchange name can't be empty");
    }

    try {
      // survive a server restart
      final boolean durable = true;
      // keep it even if not in use
      final boolean autoDelete = false;

      final Map<String, Object> arguments = new HashMap<>();
      // Make sure that the Exchange is declared
      client.exchangeDeclare(exchangeName, type, durable, autoDelete, arguments);
    } catch (final Exception e) {
      throw new HoplinRuntimeException("Unable to bind to queue", e);
    }
  }

  @Override
  public <T> SubscriptionResult subscribe(final String subscriberId, final Class<T> clazz,
      final Consumer<T> handler) {
    return subscribe(clazz, handler, cfg -> cfg.withSubscriberId(subscriberId));
  }

  @Override
  public <T> SubscriptionResult subscribe(final Class<T> clazz, final Consumer<T> handler,
      final Consumer<SubscriptionConfigurator> cfg) {
    // wrap handler into our BiFunction
    final BiFunction<T, MessageContext, Reply<?>> consumer = (msg, context) -> {
      handler.accept(msg);
      return Reply.withEmpty();
    };
    return subscribe(clazz, consumer, cfg);
  }

  @Override
  public <T> SubscriptionResult subscribe(final String subscriberId, final Class<T> clazz,
      final BiConsumer<T, MessageContext> handler) {

    final BiFunction<T, MessageContext, Reply<?>> consumer = (msg, context) -> {
      handler.accept(msg, context);
      return Reply.withEmpty();
    };

    return subscribe(clazz, consumer, cfg -> cfg.withSubscriberId(subscriberId));
  }

  @Override
  public <T> SubscriptionResult subscribe(final String subscriberId, final Class<T> clazz,
      final Function<T, Reply<?>> handler) {

    final BiFunction<T, MessageContext, Reply<?>> consumer = (msg, context) -> handler.apply(msg);
    return subscribe(clazz, consumer, cfg -> cfg.withSubscriberId(subscriberId));
  }

  @Override
  public <T> SubscriptionResult subscribe(final String subscriberId, final Class<T> clazz,
      final BiFunction<T, MessageContext, Reply<?>> handler) {

    return subscribe(clazz, handler, cfg -> cfg.withSubscriberId(subscriberId));
  }

  @Override
  public <T> SubscriptionResult subscribe(final Class<T> clazz,
      final BiFunction<T, MessageContext, Reply<?>> handler,
      final Consumer<SubscriptionConfigurator> cfg) {

    final SubscriptionConfigurator configurator = new SubscriptionConfigurator();
    cfg.accept(configurator);

    final SubscriptionResult subscription = subscribe(configurator.build(), clazz);
    log.info("Subscription Exchange : {}", subscription.getExchange());
    log.info("Subscription Queue    : {}", subscription.getQueue());

    client.basicConsume(binding.getQueue(), clazz, handler);
    return subscription;
  }

  @Override
  public RabbitMQClient getMqClient() {
    return client;
  }

  @Override
  public <T> void publish(final T message) {
    publish(message, "", createDefaultConfiguration());
  }

  @Override
  public <T> void publish(final T message, final String routingKey) {
    publish(message, routingKey, createDefaultConfiguration());
  }

  /**
   * Create default configuration for a message
   *
   * @return
   */
  private Consumer<MessageConfiguration> createDefaultConfiguration() {
    return cfg -> {
      // TODO : Default values
    };
  }

  @Override
  public <T> void publish(final T message, final Consumer<MessageConfiguration> cfg) {
    publish(message, "", cfg);
  }

  @Override
  public <T> void publish(final T message, final String routingKey,
      final Consumer<MessageConfiguration> cfg) {
    _publish(message, routingKey, cfg);
  }

  public <T> void _publish(final T message, final String routingKey,
      final Consumer<MessageConfiguration> cfg) {

    Objects.requireNonNull(message);
    Objects.requireNonNull(routingKey);
    Objects.requireNonNull(cfg);

    // populate our configurations with default etc...
    final MessageConfiguration conf = new MessageConfiguration();
    final Consumer<MessageConfiguration> composite = cfg.andThen(after -> {
      after.setNativeMessageFormat(true);
    });

    composite.accept(conf);
    Object val;

    if (conf.isNativeMessageFormat()) {
      val = message;
    } else {
      // Wrap our message original message
      final MessagePayload<T> payload = new MessagePayload<>(message);
      payload.setType(message.getClass());
      val = payload;
    }

    client.basicPublish(binding.getExchange(), routingKey, val);
  }

  @Override
  public <T> CompletableFuture<Void> publishAsync(T message) {
    return publishAsync(message, "", createDefaultConfiguration());
  }

  @Override
  public <T> CompletableFuture<Void> publishAsync(final T message, final String routingKey) {
    return publishAsync(message, routingKey, createDefaultConfiguration());
  }

  @Override
  public <T> CompletableFuture<Void> publishAsync(final T message,
      final Consumer<MessageConfiguration> cfg) {
    return publishAsync(message, "", cfg);
  }

  @Override
  public <T> CompletableFuture<Void> publishAsync(final T message, final String routingKey,
      final Consumer<MessageConfiguration> cfg) {
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

  @Override
  public void awaitQuiescence() {

  }

  @Override
  public void awaitQuiescence(long time, TimeUnit unit) {

  }
}
