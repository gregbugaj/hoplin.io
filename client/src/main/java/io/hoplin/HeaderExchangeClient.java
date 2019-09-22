package io.hoplin;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default header exchange client
 */
public class HeaderExchangeClient extends AbstractExchangeClient {

  private static final Logger log = LoggerFactory.getLogger(HeaderExchangeClient.class);

  public HeaderExchangeClient(final RabbitMQOptions options, final Binding binding, final ExecutorService executor) {
    super(options, binding, executor);
    bind("headers");
  }

  /**
   * Create new {@link HeaderExchangeClient}
   *
   * @param options the connection options to use
   * @param binding the {@link Binding} to use
   * @param executor the {@link ExecutorService} to use
   * @return new Header Exchange client
   */
  public static ExchangeClient create(final RabbitMQOptions options, final Binding binding,
      final ExecutorService executor) {
    return new HeaderExchangeClient(options, binding, executor);
  }

  /**
   * Create new {@link HeaderExchangeClient}
   *
   * @param options  the connection options to use
   * @param exchange the exchange to use
   * @return
   */
  public static ExchangeClient create(final RabbitMQOptions options, final String exchange, final ExecutorService executor) {
    Objects.requireNonNull(options);
    Objects.requireNonNull(exchange);
    Objects.requireNonNull(executor);

    final Binding binding = BindingBuilder    // Producer does not bind to the queue only to the exchange when using HeaderExchange
        .bind()
        .to(new HeaderExchange(exchange))
        .build();

    return create(options, binding, executor);
  }
}
