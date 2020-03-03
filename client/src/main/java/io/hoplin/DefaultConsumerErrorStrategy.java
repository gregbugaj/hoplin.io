package io.hoplin;

import static io.hoplin.json.JsonMessagePayloadCodec.serializeWithDefaults;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default consumer error handler Messages are put in dedicated error queues.
 * <p>
 * This is not this same as Dead Letter Queue, but rather error handling queue that handles errors
 * that can be scheduled for retry based on the RetryPolicy
 */
public class DefaultConsumerErrorStrategy implements ConsumerErrorStrategy {

  private static final Logger log = LoggerFactory.getLogger(DeadLetterErrorStrategy.class);

  private final Channel channel;

  private final Publisher publisher;

  public DefaultConsumerErrorStrategy(final Channel channel) {
    this.channel = Objects.requireNonNull(channel);
    this.publisher = new Publisher(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
  }


  @SuppressWarnings("unchecked")
  @Override
  public AckStrategy handleConsumerError(final MessageContext context, final Throwable throwable) {

    if (!channel.isOpen()) {
      log.error("Channel already disposed, unable to publish message");
      return AcknowledgmentStrategies.NACK_WITH_REQUEUE.strategy();
    }

    if (context == null) {
      log.warn("Message context is null while handling consumer error");
      return AcknowledgmentStrategies.NACK_WITH_REQUEUE.strategy();
    }

    final MessageReceivedInfo info = context.getReceivedInfo();
    final BasicProperties properties = publisher.createBasisProperties(Collections.emptyMap());
    final byte[] message = createMessage(context, throwable);

    if (log.isDebugEnabled()) {
      log.debug("Error message : {}", new String(message));
    }

    try {
      final String dlqExchangeName = ConsumerErrorStrategy
          .createDlqExchangeName(info.getExchange());
      final String routingKey = info.getRoutingKey();

      publisher
          .basicPublishAsync(channel, dlqExchangeName, routingKey, properties, message);

      return AcknowledgmentStrategies.BASIC_ACK.strategy();
    } catch (final Exception e) {
      log.error("Unable to handle consumer error", e);
    }

    return AcknowledgmentStrategies.NACK_WITHOUT_REQUEUE.strategy();
  }

  private byte[] createMessage(final MessageContext context, final Throwable throwable) {
    Objects.requireNonNull(context);
    Objects.requireNonNull(throwable);

    final ErrorMessage error = new ErrorMessage();
    final MessageReceivedInfo info = context.getReceivedInfo();

    error.setExchange(info.getExchange())
        .setQueue(info.getQueue())
        .setRoutingKey(info.getRoutingKey())
        .setCreationTime(System.currentTimeMillis())
        .setBody(new String(context.getBody(), StandardCharsets.UTF_8))
        .setException(toString(throwable))
        .setProperties(context.getProperties());

    return serializeWithDefaults(error);
  }

  private String toString(final Throwable throwable) {
    if (throwable == null) {
      return "";
    }
    final StackTraceElement[] elements = throwable.getStackTrace();
    if (elements.length > 0) {
      final StringWriter sw = new StringWriter();
      final PrintWriter pw = new PrintWriter(sw);
      pw.println(elements[0]);
      return sw.toString();
    }
    return "";
  }
}
