package io.hoplin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Binding builder between Exchange and Queue
 * <p>
 * TODO : Implement Exchange to Exchange biding
 */
public class BindingBuilder {

  private BindingBuilder() {
    super();
  }

  public static DestinationConfigurer bind(final Queue queue) {
    return new DestinationConfigurer(queue);
  }

  public static DestinationConfigurer bind() {
    return new DestinationConfigurer(new Queue(""));
  }

  public static DestinationConfigurer bind(final String queue) {
    return new DestinationConfigurer(new Queue(queue));
  }

  public static class DestinationConfigurer {

    private final Queue queue;

    public DestinationConfigurer(final Queue queue) {
      this.queue = Objects.requireNonNull(queue);
    }

    public Binding to(final FanoutExchange exchange) {
      return new Binding(queue.getName(), exchange.getName(), Collections.emptyMap());
    }

    public TopicExchangeRoutingKeyConfigurer to(final TopicExchange exchange) {
      return new TopicExchangeRoutingKeyConfigurer(this, exchange);
    }

    public DirectExchangeRoutingKeyConfigurer to(final DirectExchange exchange) {
      return new DirectExchangeRoutingKeyConfigurer(this, exchange);
    }

    public HeaderExchangeRoutingKeyConfigurer to(final HeaderExchange exchange) {
      return new HeaderExchangeRoutingKeyConfigurer(this, exchange);
    }
  }

  public abstract static class RoutingKeyConfigurer<E extends Exchange, T> {

    protected final BindingBuilder.DestinationConfigurer destination;

    protected final String exchange;

    protected boolean autoAck;

    protected int prefetchCount = 1;

    protected boolean publisherConfirms;

    protected String routingKey;

    public RoutingKeyConfigurer(final BindingBuilder.DestinationConfigurer destination,
        final String exchange) {
      this.destination = destination;
      this.exchange = exchange;
    }

    @SuppressWarnings("unchecked")
    public T withAutoAck(final boolean autoAck) {
      this.autoAck = autoAck;
      return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withPrefetchCount(final int prefetchCount) {
      this.prefetchCount = prefetchCount;
      return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withPublisherConfirms(final boolean publisherConfirms) {
      this.publisherConfirms = publisherConfirms;
      return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T with(final String routingKey) {
      this.routingKey = routingKey;
      return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T with(final Enum<?> routingKey) {
      this.routingKey = routingKey.toString();
      return (T) this;
    }

    QueueOptions buildOptions() {
      final QueueOptions options = new QueueOptions();
      options.setPublisherConfirms(publisherConfirms);
      options.setPrefetchCount(prefetchCount);
      options.setAutoAck(autoAck);
      return options;
    }

    public abstract Binding build();
  }

  public static class TopicExchangeRoutingKeyConfigurer extends
      RoutingKeyConfigurer<TopicExchange, TopicExchangeRoutingKeyConfigurer> {

    public TopicExchangeRoutingKeyConfigurer(final DestinationConfigurer configurer,
        final TopicExchange exchange) {
      super(configurer, exchange.getName());
    }

    public Binding build() {
      return new Binding(destination.queue.getName(), exchange, routingKey, Collections.emptyMap(),
          buildOptions());
    }
  }

  public static class DirectExchangeRoutingKeyConfigurer extends
      RoutingKeyConfigurer<DirectExchange, DirectExchangeRoutingKeyConfigurer> {

    public DirectExchangeRoutingKeyConfigurer(final DestinationConfigurer configurer,
        final DirectExchange exchange) {
      super(configurer, exchange.getName());
    }

    @Override
    public Binding build() {
      return new Binding(destination.queue.getName(), exchange, routingKey, Collections.emptyMap(),
          buildOptions());
    }
  }

  public static class HeaderExchangeRoutingKeyConfigurer extends
      RoutingKeyConfigurer<HeaderExchange, HeaderExchangeRoutingKeyConfigurer> {

    private final Map<String, Object> arguments = new HashMap<>();

    public HeaderExchangeRoutingKeyConfigurer(final DestinationConfigurer configurer,
        final HeaderExchange exchange) {
      super(configurer, exchange.getName());
      // setup defaults for the queue
      arguments.put("x-match", "all");
    }

    /**
     * Add binding arguments, for known property types and values x-match property  can have 2
     * values: "any" or "all"
     *
     * @param key   the key to add
     * @param value the value to add
     * @return
     */
    public HeaderExchangeRoutingKeyConfigurer withArgument(final String key, final Object value) {
      if (key == null) {
        throw new IllegalArgumentException("Key can't be null");
      }

      if ("x-match".equalsIgnoreCase(key)) {
        if (!("any".equalsIgnoreCase(value.toString()) || "all"
            .equalsIgnoreCase(value.toString()))) {
          throw new IllegalArgumentException(
              "x-match property  can have 2 values: \"any\" or \"all\" but got :" + value);
        }
      }

      arguments.put(key, value);
      return this;
    }

    /**
     * Add binding arguments
     *
     * @param arguments
     * @return
     */
    public HeaderExchangeRoutingKeyConfigurer withArguments(final Map<String, String> arguments) {
      Objects.requireNonNull(arguments);
      arguments.forEach(this::withArgument);
      return this;
    }

    @Override
    public Binding build() {
      return new Binding(destination.queue.getName(), exchange, "", arguments, buildOptions());
    }
  }
}





