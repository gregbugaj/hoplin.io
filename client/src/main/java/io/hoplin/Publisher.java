package io.hoplin;

import static io.hoplin.json.JsonMessagePayloadCodec.serializeWithDefaults;
import static io.hoplin.metrics.QueueMetrics.getKey;
import static io.hoplin.metrics.QueueMetrics.Factory.getInstance;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import io.hoplin.metrics.QueueMetrics;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for publishing messages, that does not require a full client.
 *
 * @see DefaultRabbitMQClient
 * @see DefaultQueueConsumer
 */
public class Publisher {

  private static final Logger log = LoggerFactory.getLogger(Publisher.class);

  private final ExecutorService executor;

  private final ConcurrentHashMap<String, Channel> channelsByThreadId = new ConcurrentHashMap<>();

  private boolean useChannelPerThread;

  public Publisher(final ExecutorService executor) {
    this.executor = Objects.requireNonNull(executor);
    this.useChannelPerThread = true;
  }

  public <T> CompletableFuture<Void> basicPublishAsync(final Channel channel,
      final String exchange,
      final String routingKey, final T message,
      final Map<String, Object> headers) {

    return _basicPublishAsync(() -> channel, exchange, routingKey, message, headers, true);
  }

  /***
   * Publish message asynchronously to a specific exchange.
   *
   * @param <T>
   * @param provider
   * @param exchange
   * @param routingKey
   * @param message
   * @param headers
   * @return
   */
  public <T> CompletableFuture<Void> basicPublishAsync(final PublisherChannelProvider provider,
      final String exchange,
      final String routingKey, final T message,
      final Map<String, Object> headers) {
    return _basicPublishAsync(provider, exchange, routingKey, message, headers, false);
  }

  private <T> CompletableFuture<Void> _basicPublishAsync(final PublisherChannelProvider provider,
      final String exchange,
      final String routingKey, final T message,
      final Map<String, Object> headers, boolean isWrappedChannel) {
    Objects.requireNonNull(exchange);

    return CompletableFuture.runAsync(() -> {
      final QueueMetrics metrics = getInstance(getKey(exchange, routingKey));

      if (metrics.getMessageSent() % 1000 == 0) {
        log.debug("Published message # {}", metrics.getMessageSent());
      }

      try {
        final BasicProperties props = createBasisProperties(headers);
        final String messageId = props.getMessageId();

        if (log.isTraceEnabled()) {
          log.trace("Publishing [exchange, routingKey, id] : {}, {}, {}", exchange, routingKey,
              messageId);
        }
        final byte[] body = serializeWithDefaults(message);

        if (!isWrappedChannel && useChannelPerThread) {
          final Channel channel = acquireChannel(Thread.currentThread().getName(), provider);
          if (channel == null) {
            throw new IllegalStateException("Channel should not be null");
          }
          channel.basicPublish(exchange, routingKey, props, body);
        } else if (isWrappedChannel) {
          // as this was wrapped channel we are not closing it and leave that to the caller
          final Channel channel = provider.acquirePublisherChannel();
          channel.basicPublish(exchange, routingKey, props, body);
        } else {
          try (final Channel channel = provider.acquirePublisherChannel()) {
            channel.basicPublish(exchange, routingKey, props, body);
          }
        }
        metrics.markMessageSent();
        metrics.incrementSend(body.length);
      } catch (final IOException | TimeoutException e) {
        metrics.markMessagePublishFailed();
        throw new HoplinRuntimeException("Unable to publish message", e);
      }
    }, executor);
  }


  private Channel acquireChannel(final String key, final PublisherChannelProvider provider) {
    final Channel metric = channelsByThreadId.get(key);
    if (metric != null) {
      return metric;
    }
    // attempt to store
    final Channel existing = channelsByThreadId
        .putIfAbsent(key, provider.acquirePublisherChannel());
    if (existing == null) {
      // we won the race so retrieve it from  cache
      return channelsByThreadId.get(key);
    }
    return existing;
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
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (final ExecutionException e) {
      throw new HoplinRuntimeException("Publishing exception ", e);
    }
  }

  /**
   * Publish message asynchronously to a specific exchange on given channel.
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
        .contentType("application/json")
        .contentEncoding("UTF-8")
        .messageId(UUID.randomUUID().toString())
        .deliveryMode(2)
        .headers(headers)
        .build();
  }
}
