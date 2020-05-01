package io.hoplin;

import java.util.Map;

/**
 * A binding is an association between a queue and an exchange. A queue must be bound to at least
 * one exchange in order to receive messages create publishers
 */
public class Binding {

  // Exchange we are binding to
  private final String exchange;
  private final Map<String, Object> arguments;
  private final String routingKey;
  // The queue we are binding
  private String queue;

  private QueueOptions options;

  public Binding(final String queue, final String exchange, final Map<String, Object> arguments) {
    this(queue, exchange, null, arguments, buildDefaultOptions());
  }

  public Binding(final String queue, final String exchange, final String routingKey,
      final Map<String, Object> arguments, final QueueOptions options) {
    this.queue = queue;
    this.exchange = exchange;
    this.arguments = arguments;
    this.routingKey = routingKey;
    this.options = options;
  }

  private static QueueOptions buildDefaultOptions() {
    final QueueOptions options = new QueueOptions();
    options.setPublisherConfirms(true);
    options.setPrefetchCount(1);
    options.setAutoAck(false);
    options.setKeepMostRecent(true);
    return options;
  }

  public String getQueue() {
    return queue;
  }

  public Binding setQueue(String queue) {
    this.queue = queue;
    return this;
  }

  public String getExchange() {
    return exchange;
  }

  public Map<String, Object> getArguments() {
    return arguments;
  }

  public String getRoutingKey() {
    return routingKey;
  }

  @Override
  public String toString() {
    return exchange + ":" + queue + ":" + routingKey + ":" + arguments;
  }

  public QueueOptions getOptions() {
    return options;
  }
}
