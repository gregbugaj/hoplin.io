package io.hoplin;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Topic exchanges route messages to queues based on wildcard matches between the routing key and
 * routing pattern specified by the queue binding. Messages are routed to one or many queues based
 * on a matching between a message routing key and this pattern.
 */
public class TopicExchangeClient extends AbstractExchangeClient {

  private static final Logger log = LoggerFactory.getLogger(TopicExchangeClient.class);

  public TopicExchangeClient(final RabbitMQOptions options, final Binding binding,
      final ExecutorService executor) {
    super(options, binding, executor);
    bind("topic");
  }

  /**
   * Create new {@link TopicExchangeClient}
   *
   * @param options  the connection options to use
   * @param binding  the {@link Binding} to use
   * @param executor the {@link ExecutorService} to use
   * @return new Topic Exchange client
   */
  public static ExchangeClient create(final RabbitMQOptions options, final Binding binding,
      final ExecutorService executor) {
    Objects.requireNonNull(options);
    Objects.requireNonNull(binding);
    Objects.requireNonNull(executor);
    return new TopicExchangeClient(options, binding, executor);
  }

  /**
   * Create new {@link TopicExchangeClient}
   *
   * @param options  the connection options to use
   * @param exchange the exchange to use
   * @return
   */
  public static ExchangeClient create(final RabbitMQOptions options, final String exchange,
      final ExecutorService executor) {
    Objects.requireNonNull(options);
    Objects.requireNonNull(exchange);
    Objects.requireNonNull(executor);
    // no-routing key required for publishers, but in publish/subscribe mode we want get all messages
    return create(options, createSensibleBindings(exchange, "", "#"), executor);
  }

  /**
   * Create new {@link TopicExchangeClient} client, this will create default RabbitMQ queues. That
   * will be in the format of <strong>Queue amq.gen-n47yBmzL1UODRg7obXV7UQ</strong>
   *
   * @param options      the options used for connection
   * @param exchangeName the exchangeName to use
   * @param bindingKey   the bindingKey to bind to the exchange
   * @param executor     the the {@link ExecutorService} to use
   * @return new TopicExchangeClient
   */
  public static ExchangeClient create(final RabbitMQOptions options, final String exchangeName,
      final String bindingKey, final ExecutorService executor) {
    return create(options, exchangeName, "", bindingKey, executor);
  }

  /**
   * Create new {@link TopicExchangeClient} client, this will create default RabbitMQ queues.
   *
   * @param options      the options used for connection
   * @param exchangeName
   * @param exchangeName the exchangeName to use
   * @param bindingKey   the bindingKey to bind to the exchange
   * @param executor     the {@link ExecutorService} to use
   * @return new TopicExchangeClient
   */
  public static ExchangeClient create(final RabbitMQOptions options, final String exchangeName,
      final String queue, final String bindingKey, final ExecutorService executor) {
    Objects.requireNonNull(options);
    return new TopicExchangeClient(options,
        createSensibleBindings(exchangeName, queue, bindingKey), executor);
  }

  static Binding createSensibleBindings(final String exchangeName,
      final String queue,
      final String bindingKey) {
    Objects.requireNonNull(exchangeName);
    Objects.requireNonNull(bindingKey);

    return BindingBuilder
        .bind(new Queue(queue))
        .to(new TopicExchange(exchangeName))
        .withAutoAck(true)
        .withPrefetchCount(1)
        .withPublisherConfirms(true)
        .with(bindingKey)
        .build();
  }
}
