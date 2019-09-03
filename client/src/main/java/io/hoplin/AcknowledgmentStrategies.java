package io.hoplin;

/**
 * Default implementations of acknowledgment strategies
 */
public enum AcknowledgmentStrategies {
  NOOP {
    @Override
    public AckStrategy strategy() {
      return (channel, tag) -> {
      };
    }
  },
  BASIC_ACK {
    @Override
    public AckStrategy strategy() {
      return (channel, tag) -> channel.basicAck(tag, false);
    }
  },
  NACK_WITH_REQUEUE {
    @Override
    public AckStrategy strategy() {
      return (channel, tag) -> channel.basicNack(tag, false, true);
    }
  },
  NACK_WITHOUT_REQUEUE {
    @Override
    public AckStrategy strategy() {
      return (channel, tag) -> channel.basicNack(tag, false, false);
    }
  };

  /**
   * The acknowledgement strategy
   *
   * @return
   */
  public abstract AckStrategy strategy();

}
