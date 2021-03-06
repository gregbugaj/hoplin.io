package io.hoplin;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default header exchange client
 */
public class HeaderExchangeClient extends AbstractExchangeClient {

  private static final Logger log = LoggerFactory.getLogger(HeaderExchangeClient.class);

  public HeaderExchangeClient(final RabbitMQOptions options, final Binding binding) {
    super(options, binding);
    bind("headers");
  }

  /**
   * Create new {@link HeaderExchangeClient}
   *
   * @param options the connection options to use
   * @param binding the {@link Binding} to use
   * @return new Header Exchange client
   */
  public static ExchangeClient create(final RabbitMQOptions options, final Binding binding) {
    Objects.requireNonNull(options);
    Objects.requireNonNull(binding);
    return new HeaderExchangeClient(options, binding);
  }

  /**
   * Create new {@link HeaderExchangeClient}
   *
   * @param options  the connection options to use
   * @param exchange the exchange to use
   * @return
   */
  public static ExchangeClient create(final RabbitMQOptions options, final String exchange) {
    Objects.requireNonNull(options);
    Objects.requireNonNull(exchange);

    // Producer does not bind to the queue only to the exchange when using HeaderExchange
    final Binding binding = BindingBuilder
        .bind()
        .to(new HeaderExchange(exchange))
        .build();

    return create(options, binding);
  }
}
