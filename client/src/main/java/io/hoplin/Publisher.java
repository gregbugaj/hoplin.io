package io.hoplin;

import static io.hoplin.json.JsonMessagePayloadCodec.serializeWithDefaults;
import static io.hoplin.metrics.QueueMetrics.Factory.getInstance;
import static io.hoplin.metrics.QueueMetrics.getKey;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import io.hoplin.metrics.QueueMetrics;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for publishing messages, that does not require a full client
 *
 * @see DefaultRabbitMQClient
 * @see DefaultQueueConsumer
 * @see RetryPolicy
 */
public class Publisher {

  private static final Logger log = LoggerFactory.getLogger(Publisher.class);

  private final Executor executor;

  public Publisher(final Executor executor) {
    this.executor = Objects.requireNonNull(executor);
  }

  /***
   * Publish message to a specific exchange.
   *
   * @param <T>
   * @param channel
   * @param exchange
   * @param routingKey
   * @param message
   * @param headers
   * @return
   */
  public <T> CompletableFuture<Void> basicPublishAsync(final Channel channel, final String exchange,
      final String routingKey, final T message,
      final Map<String, Object> headers) {
    Objects.requireNonNull(exchange);

    return CompletableFuture.runAsync(() -> {
      final QueueMetrics metrics = getInstance(getKey(exchange, routingKey));

      try {
        final BasicProperties props = createBasisProperties(headers);
        final String messageId = props.getMessageId();

        if (log.isDebugEnabled()) {
          log.debug("Publishing [exchange, routingKey, id] : {}, {}, {}", exchange, routingKey,
              messageId);
        }

        final byte[] body = serializeWithDefaults(message);
        channel.basicPublish(exchange, routingKey, props, body);
        metrics.markMessageSent();
        metrics.incrementSend(body.length);
      } catch (final IOException e) {
        metrics.markMessagePublishFailed();
        throw new HoplinRuntimeException("Unable to publish message", e);
      }
    }, executor);
  }

  /**
   * Publish message on specific channel. This method blocks
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
    final CompletableFuture<Void> completable = basicPublishAsync(channel, exchange,
        routingKey, message, headers);
    try {
      completable.get();
    } catch (final InterruptedException | ExecutionException e) {
      throw new HoplinRuntimeException("Publishing exception ", e);
    }
  }

  /**
   * Publish message to a specific exchange
   *
   * @param <T>
   * @param channel
   * @param exchange
   * @param routingKey
   * @param props
   * @param body
   * @return
   */
  public <T> CompletableFuture<Void> basicPublishAsync(final Channel channel, final String exchange,
      final String routingKey, BasicProperties props, byte[] body) {

    Objects.requireNonNull(channel);
    Objects.requireNonNull(exchange);
    Objects.requireNonNull(body);

    return CompletableFuture.runAsync(() -> {
      final QueueMetrics metrics = getInstance(getKey(exchange, routingKey));
      try {
        if (log.isDebugEnabled()) {
          log.debug("Publishing [exchange, routingKey, id] : {}, {}, {}", exchange, routingKey,
              props.getMessageId());
        }
        channel.basicPublish(exchange, routingKey, props, body);
        metrics.markMessageSent();
        metrics.incrementSend(body.length);
      } catch (final IOException e) {
        metrics.markMessagePublishFailed();
        throw new HoplinRuntimeException("Unable to publish message", e);
      }
    }, executor);
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
