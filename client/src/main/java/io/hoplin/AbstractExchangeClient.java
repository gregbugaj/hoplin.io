package io.hoplin;

import static io.hoplin.ConsumerErrorStrategy.createDlqExchangeName;
import static io.hoplin.ConsumerErrorStrategy.createDlqQueueName;

import com.google.common.base.Strings;
import com.rabbitmq.client.AMQP;
import io.hoplin.util.IpUtil;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
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

  Binding binding;

  RabbitMQClient client;

  private final HostInfo hostInfo;

  private final AtomicLong pendingMessages = new AtomicLong();

  AbstractExchangeClient(final RabbitMQOptions options, final Binding binding) {
    Objects.requireNonNull(options);
    Objects.requireNonNull(binding);

    this.client = RabbitMQClient.create(options);
    this.binding = binding;
    this.hostInfo = IpUtil.getHostInfo();
    log.info("hostInfo : {}", hostInfo);
  }

  /**
   * setup error handling exchange
   *
   * @return created error exchange name
   */
  private String setupErrorHandlingExchange(final String exchangeName) {
    final String dlqExchangeName = createDlqExchangeName(exchangeName);
    // survive a server restart
    final boolean durable = true;
    // keep it even if not in use
    final boolean autoDelete = false;
    final String type = "direct";

    try {
      // Make sure that the Exchange is declared
      client.exchangeDeclare(dlqExchangeName, type, durable, autoDelete);

      return dlqExchangeName;
    } catch (final Exception e) {
      log.error("Unable to declare error exchange", e);
      throw new HoplinRuntimeException("Unable to declare error exchange", e);
    }
  }

  /**
   * Crete new error handling queue and return its name
   *
   * @param queueName
   * @param exchangeName
   * @param routingKey
   * @return
   */
  private String setupErrorHandlingQueue(final String queueName, final String exchangeName,
      final String routingKey) {
    try {
      final boolean autoDelete = false;
      final String dlqQueueName = createDlqQueueName(queueName);
      final String dlqExchangeName = createDlqExchangeName(exchangeName);

      client.queueDeclare(dlqQueueName, true, false, autoDelete, Collections.emptyMap());
      client.queueBind(dlqQueueName, dlqExchangeName, routingKey);

      log.info(
          "Binding error queue [exchangeName, queueName, bindingKey, autoDelete] : {}, {}, {}, {}",
          dlqExchangeName,
          dlqQueueName,
          routingKey,
          autoDelete
      );

      return dlqQueueName;
    } catch (final Exception e) {
      log.error("Unable to declare error handling queue", e);
      throw new HoplinRuntimeException("Unable to bind error exchange", e);
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

      final Map<String, Object> args = new HashMap<>(arguments);
//      args.put("x-dead-letter-exchange", createDlqExchangeName(exchangeName));
      // TTL in ms
      //args.put("x-message-ttl", TimeUnit.SECONDS.toMillis(2));

      if (log.isDebugEnabled()) {
        log.debug("subscription args : {}", args);
      }

      // when the queue name is empty we will create a queue dynamically and bind to that queue
      final AMQP.Queue.DeclareOk queueDeclare = client
          .queueDeclare(queueName, true, false, autoDelete, args);

      queueName = queueDeclare.getQueue();
      client.queueBind(queueName, exchangeName, bindingKey);
      log.info("Binding client [exchangeName, queueName, bindingKey, autoDelete] : {}, {}, {}, {}",
          exchangeName,
          queueName,
          bindingKey,
          autoDelete
      );

      final String errorHandlingExchange = setupErrorHandlingExchange(exchangeName);
      final String errorHandlingQueue = setupErrorHandlingQueue(queueName, exchangeName,
          bindingKey);

      return new SubscriptionResult(exchangeName, queueName, errorHandlingExchange,
          errorHandlingQueue);
    } catch (final Exception e) {
      throw new HoplinRuntimeException("Unable to setup subscription consumer", e);
    }
  }

  /**
   * Generate queue name from supplied parameters. Default queue name format
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
    return this::decorateMessageConfiguration;
  }

  private void decorateMessageConfiguration(final MessageConfiguration cfg) {
    Objects.requireNonNull(cfg);
    // TODO : Default values
    // https://tools.ietf.org/html/rfc7239
    cfg.addHeader("Forwarded", "for=" + hostInfo.getAddress());
  }

  @Override
  public <T> void publish(final T message, final Consumer<MessageConfiguration> cfg) {
    publish(message, "", cfg);
  }

  @Override
  public <T> void publish(final T message, final String routingKey,
      final Consumer<MessageConfiguration> cfg) {

    final CompletableFuture<Void> feature = _publishAsync(message, routingKey, cfg);
    try {
      feature.get();
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (final ExecutionException e) {
      throw new HoplinRuntimeException("Unable to publish", e);
    }
  }

  private <T> CompletableFuture<Void> _publishAsync(final T message, final String routingKey,
      final Consumer<MessageConfiguration> cfg) {
    Objects.requireNonNull(message);
    Objects.requireNonNull(routingKey);
    Objects.requireNonNull(cfg);
    pendingMessages.incrementAndGet();

    // populate our configurations with default etc...
    final MessageConfiguration conf = new MessageConfiguration();
    cfg.accept(conf);
    decorateMessageConfiguration(conf);
    Object val;

    if (conf.isNativeMessageFormat()) {
      val = message;
    } else {
      // Wrap our message original message
      final MessagePayload<T> payload = new MessagePayload<>(message);
      payload.setType(message.getClass());
      val = payload;
    }

    return client
        .basicPublishAsync(binding.getExchange(), routingKey, val, conf.getHeaders())
        .thenAccept((_void) -> pendingMessages.decrementAndGet());
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
    return _publishAsync(message, routingKey, cfg);
  }

  @Override
  public void awaitQuiescence() {
    // Wait for all items
    awaitQuiescence(-1, TimeUnit.MILLISECONDS);
  }

  @Override
  public void awaitQuiescence(long sleepFor, TimeUnit unit) {
    log.debug("Await Quiescence pending : {}", pendingMessages.get());
    final long start = System.nanoTime();
    final long end = System.nanoTime() + unit.toNanos(sleepFor);
    long duration = 0;
    long count;
    final long tick = TimeUnit.MILLISECONDS.toNanos(10);
    while ((count = pendingMessages.get()) > 0) {
      try {
        duration = System.nanoTime() - start;
        log.debug("Awaiting quiescence : {}, {} (ms)", count,
            TimeUnit.NANOSECONDS.toMillis(duration));
        TimeUnit.NANOSECONDS.sleep(tick);
        if (sleepFor > 0 && System.nanoTime() > end) {
          break;
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    log.debug("Quiescence after: {} (ms)", TimeUnit.NANOSECONDS.toMillis(duration));
  }

  @Override
  public void close() {
    if (log.isDebugEnabled()) {
      log.debug("Client closed : start");
    }

    awaitQuiescence(-1, TimeUnit.MILLISECONDS);
    if (client != null) {
      try {
        client.disconnect();
      } catch (IOException e) {
        log.warn("Error during close", e);
      }
    }
    if (log.isDebugEnabled()) {
      log.debug("Client closed : done");
    }
  }

  @Override
  public CloseableExchangeClient asClosable() {
    return new DefaultClosableExchangeClient(this);
  }
}
