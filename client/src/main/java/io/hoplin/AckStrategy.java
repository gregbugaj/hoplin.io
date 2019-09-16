package io.hoplin;

import com.rabbitmq.client.Channel;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Acknowledgement strategy
 */
public interface AckStrategy extends
    ThrowingBiConsumer<com.rabbitmq.client.Channel, java.lang.Long, Exception> {

  /**
   * Acknowledge given message with specific {@link AckStrategy}
   *
   * @param channel the channel to send acknowledgment on
   * @param context the context to use for ack
   * @param ack     the {@link AckStrategy} to use
   * @return {@code true} when {@link AckStrategy} succeeded {@code false} otherwise
   */
  static boolean acknowledge(final Channel channel, final MessageContext context,
      final AckStrategy ack) {
    return Util.acknowledge(channel, context, ack);
  }

  class Util {

    private static final Logger log = LoggerFactory.getLogger(Util.class);

    static boolean acknowledge(final Channel channel, final MessageContext context,
        final AckStrategy ack) {
      Objects.requireNonNull(channel);
      Objects.requireNonNull(context);
      Objects.requireNonNull(ack);

      try {
        final String messageId = context.getProperties().getMessageId();
        final long deliveryTag = context.getReceivedInfo().getDeliveryTag();
        log.info("Acknowledging  [strategy, messageId, deliveryTag] : {}, {}, {}", ack, messageId,
            deliveryTag);

        ack.accept(channel, deliveryTag);
        return true;
      } catch (final Exception e) {
        log.error("Unable to ACK context : " + context, e);
      }
      return false;
    }
  }
}

