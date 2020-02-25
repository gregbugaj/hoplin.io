package io.hoplin;

/**
 * Error handler strategy
 */
public interface ConsumerErrorStrategy {

  /**
   * Default error exchange
   */
  String DEFAULT_ERROR_EXCHANGE = "hoplin.error.exchange";

  /**
   * Handle consumer error, this can include {@link io.hoplin.DefaultQueueConsumer.MethodReference}
   * as well as {@link DefaultQueueConsumer} errors.
   *
   * @param context   the context the error is attached to
   * @param throwable the exception that we try to handle
   * @return
   */
  default AckStrategy handleConsumerError(final MessageContext context, final Throwable throwable) {
    return AcknowledgmentStrategies.NACK_WITH_REQUEUE.strategy();
  }

  /**
   * Handle message cancellation We NACK current message with requeue set to true
   *
   * @param context the message that we trying to cancel
   * @return
   */
  default AckStrategy handleConsumerCancelled(final MessageContext context) {
    return AcknowledgmentStrategies.NACK_WITH_REQUEUE.strategy();
  }

  /**
   * Create DLX exchange name
   *
   * @param exchangeName
   * @return
   */
  static String createDlqExchangeName(final String exchangeName) {
    // TODO : this should be controlled via a policy
    if (true) {
      return DEFAULT_ERROR_EXCHANGE;
    }
    final String name =
        (exchangeName == null || exchangeName.isEmpty()) ? DEFAULT_ERROR_EXCHANGE : exchangeName;
    return String.format("%s.%s", "hoplin.dead", name);
  }


  /**
   * Create DLQ queue name
   *
   * @param queueName
   * @return
   */
  static String createDlqQueueName(String queueName) {
    return queueName + ".error";
  }
}
