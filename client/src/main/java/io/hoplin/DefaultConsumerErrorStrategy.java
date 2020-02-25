package io.hoplin;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import io.hoplin.json.JsonMessagePayloadCodec;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default consumer error handler
 */
public class DefaultConsumerErrorStrategy implements ConsumerErrorStrategy {

  private static final Logger log = LoggerFactory.getLogger(DeadLetterErrorStrategy.class);

  private final Channel channel;

  public DefaultConsumerErrorStrategy(final Channel channel) {
    this.channel = channel;
  }

  @SuppressWarnings("unchecked")
  @Override
  public AckStrategy handleConsumerError(final MessageContext context, final Throwable throwable) {

    if (!channel.isOpen()) {
      log.error("Channel already disposed, unable to publish message");
      return AcknowledgmentStrategies.NACK_WITH_REQUEUE.strategy();
    }

    if (context == null) {
      log.warn("Message context is null while handling consumer error", throwable);
      return AcknowledgmentStrategies.NACK_WITH_REQUEUE.strategy();
    }

    final MessageReceivedInfo info = context.getReceivedInfo();
    final Publisher publisher = new Publisher();
    final BasicProperties properties = publisher.createBasisProperties(Collections.emptyMap());
    final byte[] message = createMessage(context, throwable);

    try {
      publisher
          .basicPublish(channel, info.getExchange(), info.getRoutingKey(), properties, message);

      return AcknowledgmentStrategies.BASIC_ACK.strategy();
    } catch (final Exception e) {
      log.error("Unable to handle consumer error", e);
    }

    return AcknowledgmentStrategies.NACK_WITHOUT_REQUEUE.strategy();
  }

  /**
   * @param context
   * @param throwable
   * @return
   */
  private byte[] createMessage(final MessageContext context, final Throwable throwable) {

    Objects.requireNonNull(context);
    Objects.requireNonNull(throwable);

    final ProcessingError error = new ProcessingError();
    final MessageReceivedInfo info = context.getReceivedInfo();

    error.setExchange(info.getExchange())
        .setQueue(info.getQueue())
        .setRoutingKey(info.getRoutingKey())
        .setCreationTime(System.currentTimeMillis())
        .setBody(new String(context.getBody(), StandardCharsets.UTF_8))
        .setException(toString(throwable))
        .setProperties(context.getProperties());

    final JsonMessagePayloadCodec serializer = new JsonMessagePayloadCodec();
    return serializer.serialize(error);
  }

  private String toString(Throwable throwable) {
    if (throwable == null) {
      return "";
    }

    final StringWriter sw = new StringWriter();
    final PrintWriter pw = new PrintWriter(sw);
    throwable.printStackTrace(pw);
    return sw.toString();
  }
}
