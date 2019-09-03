package io.hoplin;

import java.util.function.Consumer;

/**
 * Specify queue consumer settings when calling {@link RabbitMQClient#basicConsume(String, Class,
 * Consumer)}
 */
public class QueueOptions {

  private boolean autoAck = true;

  private boolean keepMostRecent = false;

  private int maxInternalQueueSize = Integer.MAX_VALUE;

  private boolean publisherConfirms;

  private int prefetchCount = 10;

  public static QueueOptions of(boolean autoAck, boolean keepMostRecent, int maxInternalQueueSize) {
    return new QueueOptions()
        .setAutoAck(autoAck)
        .setKeepMostRecent(keepMostRecent)
        .setMaxInternalQueueSize(maxInternalQueueSize);
  }

  public static QueueOptions of(boolean autoAck) {
    return new QueueOptions()
        .setAutoAck(autoAck);
  }

  /**
   * @param maxInternalQueueSize the size of internal queue
   */
  public QueueOptions setMaxInternalQueueSize(int maxInternalQueueSize) {
    this.maxInternalQueueSize = maxInternalQueueSize;
    return this;
  }

  /**
   * @return {@code true} if the server should consider messages acknowledged once delivered; {@code
   * false}  if the server should expect explicit acknowledgements
   */
  public boolean isAutoAck() {
    return autoAck;
  }

  /**
   * @param autoAck true if the server should consider messages acknowledged once delivered; false
   *                if the server should expect explicit acknowledgements
   */
  public QueueOptions setAutoAck(boolean autoAck) {
    this.autoAck = autoAck;
    return this;
  }

  /**
   * @return the size of internal queue
   */
  public int maxInternalQueueSize() {
    return maxInternalQueueSize;
  }

  /**
   * @return {@code true} if old messages will be discarded instead of recent ones, otherwise use
   * {@code false}
   */
  public boolean isKeepMostRecent() {
    return keepMostRecent;
  }

  /**
   * @param keepMostRecent {@code true} for discarding old messages instead of recent ones,
   *                       otherwise use {@code false}
   */
  public QueueOptions setKeepMostRecent(boolean keepMostRecent) {
    this.keepMostRecent = keepMostRecent;
    return this;
  }

  public boolean isPublisherConfirms() {
    return publisherConfirms;
  }

  public void setPublisherConfirms(boolean publisherConfirms) {
    this.publisherConfirms = publisherConfirms;
  }

  public int getPrefetchCount() {
    return prefetchCount;
  }

  public void setPrefetchCount(int prefetchCount) {
    this.prefetchCount = prefetchCount;
  }
}
