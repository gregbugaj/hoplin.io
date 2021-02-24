package io.hoplin.metrics;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Metric tracking
 */
public interface QueueMetrics {

  /**
   * Get key for the metric
   *
   * @param exchange
   * @param routingKey
   * @return
   */
  static String getKey(String exchange, String routingKey) {
    return String.format("%s-%s", exchange, routingKey);
  }

  /**
   * Mark when a messages has been sent
   */
  long markMessageSent();

  /**
   * Get count of message sent
   *
   * @return
   */
  long getMessageSent();

  /**
   * Mark when message has been received
   */
  long markMessageReceived();

  /**
   * Get count of message received
   *
   * @return
   */
  long getMessageReceived();

  /**
   * Message publishing failed
   *
   * @return
   */
  long markMessagePublishFailed();

  /**
   * Increment received total message size by dataSizeInBytes
   *
   * @param dataSizeInBytes the amount to increment by
   */
  void incrementReceived(long dataSizeInBytes);

  /**
   * Increment sent total message size by dataSizeInBytes
   *
   * @param dataSizeInBytes the amount to increment by
   */
  void incrementSend(long dataSizeInBytes);

  /**
   * Get message sent size in bytes
   *
   * @return
   */
  long getSentSize();

  /**
   * Get message received size in bytes
   *
   * @return
   */
  long getReceivedSize();

  /**
   * Reset underlying statistics
   */
  void reset();

  class Factory {

    private static final Logger log = LoggerFactory.getLogger(QueueMetrics.class);

    private static final ConcurrentHashMap<String, QueueMetrics> metrics = new ConcurrentHashMap<>();

    public static QueueMetrics getInstance(final String key) {
      final QueueMetrics metric = metrics.get(key);
      if (metric != null) {
        return metric;
      }

      // attempt to store
      final QueueMetrics existing = metrics.putIfAbsent(key, new DefaultQueueMetrics());
      if (existing == null) {
        // we won the race so retrieve it from  cache
        return metrics.get(key);
      }

      return existing;
    }

    public static Map<String, QueueMetrics> getMetrics() {
      return Collections.unmodifiableMap(metrics);
    }
  }

  class DefaultQueueMetrics implements QueueMetrics {

    private final AtomicLong sent = new AtomicLong();

    private final AtomicLong received = new AtomicLong();

    private final AtomicLong sentFailed = new AtomicLong();

    private final AtomicLong sentData = new AtomicLong();

    private final AtomicLong receivedData = new AtomicLong();

    @Override
    public long markMessageSent() {
      return sent.incrementAndGet();
    }

    @Override
    public long getMessageSent() {
      return sent.get();
    }

    @Override
    public long markMessageReceived() {
      return received.incrementAndGet();
    }

    @Override
    public long getMessageReceived() {
      return received.get();
    }

    @Override
    public long markMessagePublishFailed() {
      return sentFailed.incrementAndGet();
    }

    @Override
    public void incrementReceived(long dataSizeInBytes) {
      receivedData.addAndGet(dataSizeInBytes);
    }

    @Override
    public void incrementSend(long dataSizeInBytes) {
      sentData.addAndGet(dataSizeInBytes);
    }

    @Override
    public long getSentSize() {
      return sentData.get();
    }

    @Override
    public long getReceivedSize() {
      return receivedData.get();
    }

    @Override
    public synchronized void reset() {
      sent.set(0);
      received.set(0);
      sentData.set(0);
      receivedData.set(0);
      sentFailed.set(0);
    }

    @Override
    public String toString() {
      return "QueueMetrics{" +
          "sent=" + sent +
          ", received=" + received +
          ", sentFailed=" + sentFailed +
          ", sentData=" + sentData +
          ", receivedData=" + receivedData +
          '}';
    }
  }
}
