package io.hoplin;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Direct exchange client
 */
public class DirectExchangeClient extends AbstractExchangeClient {

  private static final Logger log = LoggerFactory.getLogger(DirectExchangeClient.class);

  public DirectExchangeClient(final RabbitMQOptions options, final Binding binding) {
    super(options, binding);
    bind("direct");
  }

  /**
   * Create new {@link DirectExchangeClient}
   *
   * @param options the connection options to use
   * @param binding the {@link Binding} to use
   * @return new Direct Exchange client setup in server mode
   */
  public static ExchangeClient create(final RabbitMQOptions options, final Binding binding) {
    Objects.requireNonNull(options);
    Objects.requireNonNull(binding);

    return new DirectExchangeClient(options, binding);
  }

  /**
   * Create new {@link DirectExchangeClient}
   *
   * @param options  the connection options to use
   * @param exchange the exchange to use
   * @return
   */
  public static ExchangeClient create(final RabbitMQOptions options, final String exchange) {
    Objects.requireNonNull(options);
    Objects.requireNonNull(exchange);
    // Producer does not bind to the queue only to the exchange when using DirectExchange
    return create(options, createSensibleBindings(exchange, "", ""));
  }

  /**
   * Create new {@link DirectExchangeClient} client, this will create default RabbitMQ queues. That
   * will be in the format of <strong>Queue amq.gen-n47yBmzL1UODRg7obXV7UQ</strong>
   *
   * @param options      the options used for connection
   * @param exchangeName the exchangeName to use
   * @param bindingKey   the bindingKeys to bind to the exchange
   * @return new DirectExchangeClient
   */
  public static ExchangeClient create(final RabbitMQOptions options, final String exchangeName,
      final String bindingKey) {
    return create(options, exchangeName, "", bindingKey);
  }

  /**
   * Create new {@link DirectExchangeClient} client, this will create default RabbitMQ queues.
   *
   * @param options    the options used for connection
   * @param exchange   the exchange to use
   * @param queue      the queue to use
   * @param routingKey the bindingKeys to bind to the exchange
   * @return new DirectExchangeClient
   */
  public static ExchangeClient create(final RabbitMQOptions options, final String exchange,
      final String queue, final String routingKey) {
    Objects.requireNonNull(options);
    Objects.requireNonNull(exchange);
    Objects.requireNonNull(routingKey);

    return new DirectExchangeClient(options, createSensibleBindings(exchange, queue, routingKey));
  }

  static Binding createSensibleBindings(final String exchange, final String queue,
      final String routingKey) {
    return BindingBuilder
        .bind(queue)
        .to(new DirectExchange(exchange))
        .withAutoAck(true)
        .withPrefetchCount(1)
        .withPublisherConfirms(true)
        .with(routingKey)
        .build();
  }
}
