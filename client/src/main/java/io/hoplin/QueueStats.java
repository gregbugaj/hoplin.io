package io.hoplin;

/**
 * Queue Statistics
 */
public class QueueStats {

  private final long consumerCount;
  private final long messageCount;

  public QueueStats(long consumerCount, long messageCount) {
    this.consumerCount = consumerCount;
    this.messageCount = messageCount;
  }

  public long getConsumerCount() {
    return consumerCount;
  }

  public long getMessageCount() {
    return messageCount;
  }

  /**
   * Check is queue is empty
   *
   * @return
   */
  public boolean isEmpty() {
    return consumerCount == 0 && messageCount == 0;
  }

  @Override
  public String toString() {
    return "QueueStats{" +
        "consumerCount=" + consumerCount +
        ", messageCount=" + messageCount +
        '}';
  }
}
