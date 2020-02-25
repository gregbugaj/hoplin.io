package io.hoplin;

import static io.hoplin.json.JsonMessagePayloadCodec.serializeWithDefaults;
import static io.hoplin.metrics.QueueMetrics.Factory.getInstance;
import static io.hoplin.metrics.QueueMetrics.getKey;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import io.hoplin.metrics.QueueMetrics;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for publishing messages, that does not require a client
 *
 * @see DefaultRabbitMQClient
 * @see DefaultQueueConsumer
 * @see RetryPolicy
 */
public class Publisher {

  private static final Logger log = LoggerFactory.getLogger(Publisher.class);

  /***
   * Publish message to a specific exchange.
   *
   * @param channel
   * @param exchange
   * @param routingKey
   * @param message
   * @param headers
   * @param <T>
   */
  public <T> void basicPublish(final Channel channel, final String exchange,
      final String routingKey, final T message,
      final Map<String, Object> headers) {
    final QueueMetrics metrics = getInstance(getKey(exchange, routingKey));

    try {
      final BasicProperties props = createBasisProperties(headers);
      final String messageId = props.getMessageId();

      log.info("Publishing [exchange, routingKey, id] : {}, {}, {}", exchange, routingKey,
          messageId);
      final byte[] body = serializeWithDefaults(message);
      channel.basicPublish(exchange, routingKey, props, body);

      // mark
      metrics.markMessageSent();
      metrics.incrementSend(body.length);
    } catch (final IOException e) {
      metrics.markMessagePublishFailed();
      throw new HoplinRuntimeException("Unable to publish message", e);
    }
  }

  /**
   * Publish message to a specific exchange
   *
   * @param channel
   * @param exchange
   * @param routingKey
   * @param props
   * @param body
   * @param <T>
   */
  public <T> void basicPublish(final Channel channel, final String exchange,
      final String routingKey, BasicProperties props, byte[] body) {

    final QueueMetrics metrics = getInstance(getKey(exchange, routingKey));
    try {
      log.info("Publishing [exchange, routingKey, id] : {}, {}, {}", exchange, routingKey,
          props.getMessageId());

      channel.basicPublish(exchange, routingKey, props, body);

      // mark
      metrics.markMessageSent();
      metrics.incrementSend(body.length);
    } catch (final IOException e) {
      metrics.markMessagePublishFailed();
      throw new HoplinRuntimeException("Unable to publish message", e);
    }
  }

  /**
   * Create {@link com.rabbitmq.client.BasicProperties}
   *
   * @param headers
   * @return
   */
  public BasicProperties createBasisProperties(
      final Map<String, Object> headers) {
    return new BasicProperties.Builder()
        .contentType("text/json")
        .contentEncoding("UTF-8")
        .messageId(UUID.randomUUID().toString())
        .deliveryMode(2)
        .headers(headers)
        .build();
  }
}
