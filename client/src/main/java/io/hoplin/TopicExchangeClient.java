package io.hoplin;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Topic exchanges route messages to queues based on wildcard matches between the routing key and
 * routing pattern specified by the queue binding. Messages are routed to one or many queues based
 * on a matching between a message routing key and this pattern.
 */
public class TopicExchangeClient extends AbstractExchangeClient {

  private static final Logger log = LoggerFactory.getLogger(TopicExchangeClient.class);

  public TopicExchangeClient(final RabbitMQOptions options, final Binding binding) {
    super(options, binding);
    bind("topic");
  }

  /**
   * Create new {@link TopicExchangeClient}
   *
   * @param options the connection options to use
   * @param binding the {@link Binding} to use
   * @return new Topic Exchange client
   */
  public static ExchangeClient create(final RabbitMQOptions options, final Binding binding) {
    Objects.requireNonNull(options);
    Objects.requireNonNull(binding);

    return new TopicExchangeClient(options, binding);
  }

  /**
   * Create new {@link TopicExchangeClient}
   *
   * @param options  the connection options to use
   * @param exchange the exchange to use
   * @return
   */
  public static ExchangeClient create(final RabbitMQOptions options, final String exchange) {
    Objects.requireNonNull(options);
    Objects.requireNonNull(exchange);
    // Producer does not bind to the queue only to the exchange when using TopicExchangeClient
    // no-routing key required for publishers, but in publish/subscribe mode we want get all messages
    return create(options, createSensibleBindings(exchange, "", "#"));
  }

  /**
   * Create new {@link TopicExchangeClient} client, this will create default RabbitMQ queues. That
   * will be in the format of <strong>Queue amq.gen-n47yBmzL1UODRg7obXV7UQ</strong>
   *
   * @param options      the options used for connection
   * @param exchangeName the exchangeName to use
   * @param bindingKey   the bindingKey to bind to the exchange
   * @return new TopicExchangeClient
   */
  public static ExchangeClient create(final RabbitMQOptions options, final String exchangeName,
      final String bindingKey) {
    return create(options, exchangeName, "", bindingKey);
  }

  /**
   * Create new {@link TopicExchangeClient} client, this will create default RabbitMQ queues.
   *
   * @param options      the options used for connection
   * @param exchangeName
   * @param exchangeName the exchangeName to use
   * @param bindingKey   the bindingKey to bind to the exchange
   * @return new TopicExchangeClient
   */
  public static ExchangeClient create(final RabbitMQOptions options, final String exchangeName,
      final String queue, final String bindingKey) {
    Objects.requireNonNull(options);
    return new TopicExchangeClient(options,
        createSensibleBindings(exchangeName, queue, bindingKey));
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
