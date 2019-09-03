package io.hoplin;

public enum ExchangeType {
  // Direct exchanges are often used to distribute tasks between multiple workers
  DIRECT, //	(Empty string) and amq.direct
  // A fanout exchange routes messages to all of the queues that are bound to it and the routing key is ignored
  FANOUT, //	amq.fanout
  TOPIC,  //	amq.topic
  HEADER,
  ; //	amq.match (and amq.headers in RabbitMQ)

  public static ExchangeType fromValue(final String exchange) {
    if (exchange == null || exchange.isEmpty()) {
      throw new IllegalArgumentException("Expected exchange name but got null or empty");
    }

    for (final ExchangeType type : values()) {
      if (type.name().equalsIgnoreCase(exchange)) {
        return type;
      }
    }

    throw new IllegalArgumentException("Unknown exchange type : " + exchange);
  }
}
