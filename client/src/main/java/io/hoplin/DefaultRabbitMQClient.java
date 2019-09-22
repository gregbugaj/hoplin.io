package io.hoplin;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link RabbitMQClient} https://www.rabbitmq.com/consumer-prefetch.html
 */
public class DefaultRabbitMQClient implements RabbitMQClient {

  private static final Logger log = LoggerFactory.getLogger(DefaultRabbitMQClient.class);

  private final ExecutorService executor;

  private RabbitMQOptions options;

  private Channel channel;

  private ConnectionProvider provider;

  private DefaultQueueConsumer consumer;

  public DefaultRabbitMQClient(final RabbitMQOptions options, final ExecutorService executor) {
    this.executor = Objects.requireNonNull(executor, "Executor can't be null");
    this.options = Objects.requireNonNull(options, "Options are required and can't be null");
    this.provider = create();
    this.channel = provider.acquire();
    channel.addReturnListener(new UnroutableMessageReturnListener(options));
  }

  private ConnectionProvider create() {
    try {
      final ConnectionProvider provider = ConnectionProvider.create(options);
      if (!provider.connect()) {
        throw new IllegalStateException("Unable to connect to broker : " + options);
      }

      return provider;
    } catch (final IOException | TimeoutException e) {
      throw new HoplinRuntimeException("Unable to connect to broker", e);
    }
  }

  @Override
  public <T> void basicConsume(final String queue, final Class<T> clazz,
      final java.util.function.Consumer<T> handler) {
    basicConsume(queue, QueueOptions.of(true), clazz, handler);
  }

  @Override
  public <T> void basicConsume(final String queue, final Class<T> clazz,
      final BiFunction<T, MessageContext, Reply<?>> handler) {
    basicConsume(queue, QueueOptions.of(true), clazz, handler);
  }


  @Override
  public synchronized <T> void basicConsume(final String queue,
      final QueueOptions options,
      final Class<T> clazz,
      final BiFunction<T, MessageContext, Reply<?>> handler) {
    Objects.requireNonNull(queue);
    Objects.requireNonNull(clazz);
    Objects.requireNonNull(handler);
    Objects.requireNonNull(options);

    try {
      if (consumer == null) {
        //basic.qos method to allow you to limit the number of unacknowledged messages
        final boolean autoAck = options.isAutoAck();
        final int prefetchCount = options.getPrefetchCount();
        final boolean publisherConfirms = options.isPublisherConfirms();

        log.info("basicConsume autoAck : {} ", autoAck);
        log.info("basicConsume prefetchCount : {} ", prefetchCount);
        log.info("basicConsume publisherConfirms : {} ", publisherConfirms);

        // Enables create acknowledgements on this channel
        if (publisherConfirms) {
          channel.confirmSelect();
          channel.addConfirmListener(this::confirmedAck, this::confirmedNack);
        }

        consumer = new DefaultQueueConsumer(queue, channel, options, executor);
        channel.basicQos(prefetchCount);

        final String consumerTag = channel.basicConsume(queue, autoAck, consumer);
        if (log.isDebugEnabled()) {
          log.debug("Assigned consumer tag : {}", consumerTag);
        }
      }

      // add the handler
      consumer.addHandler(clazz, handler);
    } catch (final IOException e) {
      log.error("Unable to subscribe messages", e);
      throw new HoplinRuntimeException("Unable to subscribe messages", e);
    }
  }

  @Override
  public synchronized <T> void basicConsume(final String queue,
      final QueueOptions options,
      final Class<T> clazz,
      final Consumer<T> handler) {

    // wrap handler into our BiFunction
    final BiFunction<T, MessageContext, Reply<?>> consumer = (msg, context) -> {
      handler.accept(msg);
      return Reply.withEmpty();
    };

    basicConsume(queue, options, clazz, consumer);
  }


  private void confirmedAck(long deliveryTag, boolean multiple) {
    log.info("Confirmed ACK :: {}", deliveryTag);
  }

  private void confirmedNack(long deliveryTag, boolean multiple) {
    log.info("Confirmed NACK :: {}", deliveryTag);
  }

  @Override
  public void exchangeDeclare(final String exchange,
      final String type,
      final boolean durable,
      final boolean autoDelete) {
    exchangeDeclare(exchange, type, durable, autoDelete, Collections.emptyMap());
  }

  @Override
  public void exchangeDeclare(final String exchange,
      final String type,
      final boolean durable,
      final boolean autoDelete,
      final Map<String, Object> arguments) {
    with((channel) -> {
      channel.exchangeDeclare(exchange, type, durable, autoDelete, arguments);
      return null;
    });
  }

  @Override
  public void queueDeclare(final String queue,
      final boolean durable,
      final boolean exclusive,
      final boolean autoDelete) {
    queueDeclare(queue, durable, exclusive, autoDelete, Collections.emptyMap());
  }

  @Override
  public AMQP.Queue.DeclareOk queueDeclare(final String queue,
      final boolean durable,
      final boolean exclusive,
      final boolean autoDelete,
      final Map<String, Object> arguments) {
    return with(
        (channel) -> channel.queueDeclare(queue, durable, exclusive, autoDelete, arguments));
  }

  @Override
  public void queueBind(final String queue, final String exchange, final String routingKey) {
    with(channel -> {
      channel.queueBind(queue, exchange, routingKey);
      return null;
    });
  }

  @Override
  public String queueDeclareTemporary() {
    return with(channel -> channel.queueDeclare().getQueue());
  }

  @Override
  public void disconnect() throws IOException {
    if (provider != null) {
      provider.disconnect();
    }
  }

  private <T> T with(final ThrowableChannel<T> handler) {
    try {
      return handler.handle(channel);
    } catch (final Exception e) {
      log.error("Unable to execute operation on  channel", e);
    }
    return null;
  }

  @Override
  public boolean isConnected() {
    return false;
  }

  @Override
  public boolean isOpenChannel() {
    return false;
  }

  @Override
  public int messageCount(final String queue) {
    try {
      return messageCountAsync(queue).get();
    } catch (final ExecutionException | InterruptedException e) {
      log.error("Unable to get message count", e);
    }

    return -1;
  }

  @Override
  public CompletableFuture<Integer> messageCountAsync(final String queue) {
    return null;
  }

  @Override
  public <T> void basicPublish(final String exchange, final String routingKey, final T message) {
    basicPublish(exchange, routingKey, message, Collections.emptyMap());
  }

  @Override
  public <T> void basicPublish(final String exchange, final String routingKey, final T message,
      final Map<String, Object> headers) {

    final Publisher publisher = new Publisher();
    with(channel -> {
      publisher.basicPublish(channel, exchange, routingKey, message, headers);
      return null;
    });
  }

  @Override
  public void basicAck(final long deliveryTag, final boolean multiple) {

  }

  @Override
  public Channel channel() {
    return provider.acquire();
  }

  private interface ThrowableChannel<T> {

    T handle(Channel channel) throws Exception;
  }
}
