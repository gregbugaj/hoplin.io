package io.hoplin;

import java.util.function.Consumer;

/**
 * Specify queue consumer settings when calling {@link RabbitMQClient#basicConsume(String, Class,
 * Consumer)}
 *
 * <p>
 * Default values
 * <pre>
 *  AutoAck = False
 *  Prefetch Count = 1
 *  Publisher Confirm = true
 * </pre>
 * <p>
 * https://www.rabbitmq.com/confirms.html#acknowledgement-modes
 */
public class QueueOptions {

  //
  private boolean autoAck = false;

  // Indicates that the incoming messages will be stored locally
  private boolean keepMostRecent = false;

  // Size of how many messages to keep internally
  private int maxInternalQueueSize = Integer.MAX_VALUE;

  private boolean publisherConfirms = true;

  private int prefetchCount = 1;

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
   * In automatic acknowledgement mode, a message is considered to be successfully delivered
   * immediately after it is sent.
   *
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
