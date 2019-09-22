package io.hoplin;

import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Direct exchange client
 */
public class DirectExchangeClient extends AbstractExchangeClient {

  private static final Logger log = LoggerFactory.getLogger(DirectExchangeClient.class);

  /**
   * Create new Direct Exchange Client
   *
   * @param options  the options to create a client from
   * @param binding  the binding to use
   * @param executor the {@link ExecutorService} to use
   */
  public DirectExchangeClient(final RabbitMQOptions options, final Binding binding, final
  ExecutorService executor) {
    super(options, binding, executor);
    bind("direct");
  }

  /**
   * Create new Direct Exchange Client
   *
   * @param options the options to create a client from
   * @param binding the binding to use
   */
  public DirectExchangeClient(final RabbitMQOptions options, final Binding binding) {
    this(options, binding, ExchangeClient.createExecutor());
  }

  /**
   * Create new {@link DirectExchangeClient}
   *
   * @param options  the connection options to use
   * @param binding  the {@link Binding} to use
   * @param executor the {@link ExecutorService} to use
   * @return new Direct Exchange client setup in server mode
   */
  public static ExchangeClient create(final RabbitMQOptions options, final Binding binding,
      final ExecutorService executor) {
    return new DirectExchangeClient(options, binding, executor);
  }

  /**
   * Create new {@link DirectExchangeClient}
   *
   * @param options  the connection options to use
   * @param exchange the exchange to use
   * @param executor the {@link ExecutorService} to use
   * @return
   */
  public static ExchangeClient create(final RabbitMQOptions options, final String exchange,
      final ExecutorService executor) {
    return create(options, createSensibleBindings(exchange, "", ""), executor);
  }

  /**
   * Create new {@link DirectExchangeClient}
   *
   * @param options  the connection options to use
   * @param exchange the exchange to use
   * @return
   */
  public static ExchangeClient create(final RabbitMQOptions options, final String exchange) {
    return create(options, exchange, ExchangeClient.createExecutor());
  }

  /**
   * Create new {@link DirectExchangeClient} client, this will create default RabbitMQ queues. That
   * will be in the format of <strong>Queue amq.gen-n47yBmzL1UODRg7obXV7UQ</strong>
   *
   * @param options      the options used for connection
   * @param exchangeName the exchangeName to use
   * @param bindingKey   the bindingKeys to bind to the exchange
   * @param executor     the {@link ExecutorService} to use
   * @return new DirectExchangeClient
   */
  public static ExchangeClient create(final RabbitMQOptions options, final String exchangeName,
      final String bindingKey, final ExecutorService executor) {
    return create(options, exchangeName, "", bindingKey, executor);
  }

  /**
   * Create new {@link DirectExchangeClient} client, this will create default RabbitMQ queues.
   *
   * @param options    the options used for connection
   * @param exchange   the exchange to use
   * @param queue      the queue to use
   * @param routingKey the bindingKeys to bind to the exchange
   * @param executor   the {@link ExecutorService} to use
   * @return new DirectExchangeClient
   */
  public static ExchangeClient create(final RabbitMQOptions options, final String exchange,
      final String queue, final String routingKey, final ExecutorService executor) {
    return new DirectExchangeClient(options, createSensibleBindings(exchange, queue, routingKey),
        executor);
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
