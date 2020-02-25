package io.hoplin;

import java.util.Objects;

/**
 * Client subscription results, This is populated from supplied {@link Binding} and {@link
 * SubscriptionConfig}
 */
public class SubscriptionResult {

  private final String exchange;

  private final String queue;

  private final String errorExchangeName;

  private final String errorQueueName;

  public SubscriptionResult(String exchangeName, String queueName) {
    this(exchangeName, queueName, "", "");
  }

  public SubscriptionResult(String exchangeName, String queueName, String errorExchangeName,
      String errorQueueName) {

    this.exchange = Objects.requireNonNull(exchangeName);
    this.queue = Objects.requireNonNull(queueName);

    this.errorExchangeName = Objects.requireNonNull(errorExchangeName);
    this.errorQueueName = Objects.requireNonNull(errorQueueName);
  }

  public String getExchange() {
    return exchange;
  }

  public String getQueue() {
    return queue;
  }

  public String getErrorExchangeName() {
    return errorExchangeName;
  }

  public String getErrorQueueName() {
    return errorQueueName;
  }

  @Override
  public String toString() {
    return "SubscriptionResult{" +
        "exchange='" + exchange + '\'' +
        ", queue='" + queue + '\'' +
        ", errorExchangeName='" + errorExchangeName + '\'' +
        ", errorQueueName='" + errorQueueName + '\'' +
        '}';
  }
}
