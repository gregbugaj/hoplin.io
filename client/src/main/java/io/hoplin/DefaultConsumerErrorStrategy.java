package io.hoplin;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultConsumerErrorStrategy implements ConsumerErrorStrategy {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterErrorStrategy.class);

    private final Channel channel;

    public DefaultConsumerErrorStrategy(final Channel channel) {
        this.channel = channel;
    }

    @SuppressWarnings("unchecked")
    @Override
    public AckStrategy handleConsumerError(final MessageContext context, final Throwable throwable) {

        if (true) {
//      return AcknowledgmentStrategies..strategy();
//      return AcknowledgmentStrategies.BASIC_ACK.strategy();
        }

        if (context == null) {
            log.warn("Message context is null while handling consumer error", throwable);
            return AcknowledgmentStrategies.NACK_WITH_REQUEUE.strategy();
        }

        return AcknowledgmentStrategies.NACK_WITHOUT_REQUEUE.strategy();
    }

    private byte[] createMessage(MessageContext context, Throwable throwable) {

        final ProcessingError error = new ProcessingError();
        final MessageReceivedInfo receivedInfo = context.getReceivedInfo();
        return new byte[0];
    }

    private class ProcessingError {
        private String exchange;

        private String queue;

        private String routingKey;
    }
}
