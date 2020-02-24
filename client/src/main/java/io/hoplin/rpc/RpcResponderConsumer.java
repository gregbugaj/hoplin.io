package io.hoplin.rpc;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import io.hoplin.*;
import io.hoplin.json.JsonMessagePayloadCodec;
import io.hoplin.metrics.QueueMetrics;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * On the reception of each RPC request, this consumer will
 *
 * <ul>
 *     <li>Perform the action required in the RPC request</li>
 *     <li>Prepare the reply message Set the correlation ID in the reply properties</li>
 *     <li>Publish the answer on the reply queue</li>
 *     <li>Send the ack to the RPC request</li>
 * </ul>
 *
 * @param <I>
 * @param <O>
 */
public class RpcResponderConsumer<I, O> extends DefaultConsumer {

  private static final Logger log = LoggerFactory.getLogger(RpcResponderConsumer.class);

  private final Executor executor;

  private final Function<I, O> handler;

  private final QueueMetrics metrics;

  private JsonMessagePayloadCodec codec;

  private ConsumerErrorStrategy errorStrategy;

  /**
   * Constructs a new instance and records its association to the passed-in channel.
   *
   * @param channel  the channel to which this consumer is attached
   * @param handler
   * @param executor
   */

  public RpcResponderConsumer(final Channel channel,
      final Function<I, O> handler,
      final Executor executor, QueueMetrics metrics) {
    super(channel);

    this.executor = Objects.requireNonNull(executor);
    this.handler = Objects.requireNonNull(handler);
    this.metrics = Objects.requireNonNull(metrics);
    this.codec = new JsonMessagePayloadCodec();
    this.errorStrategy = new DefaultConsumerErrorStrategy(channel);
  }

  @Override
  public void handleDelivery(final String consumerTag,
      final Envelope envelope,
      final AMQP.BasicProperties properties,
      final byte[] body) {

    log.info("RPC handleDelivery Envelope   : {}", envelope);
    log.info("RPC handleDelivery Properties : {}", properties);

    metrics.markMessageReceived();
    metrics.incrementReceived(body.length);

    // 1 : Perform the action required in the RPC request
    CompletableFuture
        .supplyAsync(() -> dispatch(body), executor)
        .whenComplete((reply, throwable) ->
        {
          final MessageContext context = MessageContext.create(consumerTag, envelope, properties);

          try {
            byte[] replyMessage = reply;

            //0 : there was unhandled exception while processing message
            if (throwable != null) {
              log.warn("Error dispatching message : {}", context, throwable);
              replyMessage = createErrorMessage(throwable);
            }

            // 2 : Prepare the reply message Set the correlation ID in the reply properties
            final AMQP.BasicProperties replyProperties = new AMQP.BasicProperties
                .Builder()
                .correlationId(properties.getCorrelationId())
                .build();

            // 3 : Publish the answer on the reply queue
            final String replyTo = properties.getReplyTo();
            log.info("replyTo, correlationId :  {}, {}", replyTo, properties.getCorrelationId());

            getChannel().basicPublish("", replyTo, replyProperties, replyMessage);
            // 4 : Send the ack to the RPC request

            // Invoke ACK
            AckStrategy
                .acknowledge(getChannel(), context, AcknowledgmentStrategies.BASIC_ACK.strategy());

            metrics.markMessageSent();
            metrics.incrementSend(replyMessage.length);
          } catch (final Exception e1) {
            log.error("Unable to acknowledge execution", e1);
          }
        });
  }

  @SuppressWarnings("unchecked")
  private byte[] dispatch(final byte[] body) {
    try {
      final MessagePayload<?> requestMsg = codec.deserialize(body, MessagePayload.class);
      MessagePayload payload;
      try {
        final O reply = handler.apply((I) requestMsg.getPayload());
        payload = new MessagePayload(reply);
      } catch (final Exception e) {
        log.warn("Handling message error : {} ", requestMsg, e);
        payload = MessagePayload.error(e);
      }

      return codec.serialize(payload, MessagePayload.class);
    } catch (final Exception e) {
      log.error("Unable to apply reply handler", e);
      throw new HoplinRuntimeException("Unable to apply reply handler", e);
    }
  }

  private byte[] createErrorMessage(final Throwable throwable) {
    try {
      return codec.serialize(MessagePayload.error(throwable), MessagePayload.class);
    } catch (final Exception e) {
      log.error("Unable to serialize message", e);
      throw new HoplinRuntimeException("Unable to serialize message", e);
    }
  }

  @Override
  public void handleShutdownSignal(final String consumerTag, final ShutdownSignalException sig) {
    log.warn("Handle Shutdown Signal :{} , {}", consumerTag, sig);
  }
}
