package io.hoplin;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import io.hoplin.json.JsonMessagePayloadCodec;
import io.hoplin.metrics.QueueMetrics;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for publishing message to the queue
 *
 * @see DefaultRabbitMQClient
 * @see DefaultQueueConsumer
 */
public class Publisher {

  private static final Logger log = LoggerFactory.getLogger(Publisher.class);

  private JsonMessagePayloadCodec codec;

  public Publisher() {
    this.codec = new JsonMessagePayloadCodec();
  }

  public <T> void basicPublish(final Channel channel, final String exchange,
      final String routingKey, final T message,
      final Map<String, Object> headers) {
    final QueueMetrics metrics = QueueMetrics.Factory.getInstance(exchange + "-" + routingKey);
    try {
      final String messageId = UUID.randomUUID().toString();
      final BasicProperties props = new BasicProperties.Builder()
          .contentType("text/json")
          .contentEncoding("UTF-8")
          .messageId(messageId)
          .deliveryMode(2)
          .headers(headers)
          .build();

      log.info("Publishing [exchange, routingKey, id] : {}, {}, {}", exchange, routingKey,
          messageId);
      final byte[] body = codec.serialize(message);
      channel.basicPublish(exchange, routingKey, props, body);

      // mark
      metrics.markMessageSent();
      metrics.incrementSend(body.length);
    } catch (final IOException e) {
      metrics.markMessagePublishFailed();
      // Should try to send to the Error Handling queue ??
      throw new HoplinRuntimeException("Unable to publish message", e);
    }
  }
}
