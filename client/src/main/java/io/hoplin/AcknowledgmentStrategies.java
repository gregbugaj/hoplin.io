package io.hoplin;

/**
 * Default implementations of acknowledgment strategies
 * <p>
 * https://www.rabbitmq.com/confirms.html#acknowledgement-modes
 * </p>
 */
public enum AcknowledgmentStrategies {

  /**
   * NOOP strategy, should be used with AutoAck
   */
  NOOP {
    @Override
    public AckStrategy strategy() {
      return (channel, tag) -> {
      };
    }
  },

  /**
   * Acknowledge one received messages.
   *
   * @see com.rabbitmq.client.AMQP.Basic.Ack
   */
  BASIC_ACK {
    @Override
    public AckStrategy strategy() {
      return (channel, tag) -> channel.basicAck(tag, false);
    }
  },

  /**
   * Reject one received messages with requeue.
   *
   * @see com.rabbitmq.client.AMQP.Basic.Nack
   */
  NACK_WITH_REQUEUE {
    @Override
    public AckStrategy strategy() {
      return (channel, tag) -> channel.basicNack(tag, false, true);
    }
  },

  /**
   * Reject one received messages without requeue.
   *
   * @see com.rabbitmq.client.AMQP.Basic.Nack
   */
  NACK_WITHOUT_REQUEUE {
    @Override
    public AckStrategy strategy() {
      return (channel, tag) -> channel.basicNack(tag, false, false);
    }
  },

  REJECT {
    @Override
    public AckStrategy strategy() {
      return (channel, tag) -> channel.basicReject(tag, false);
    }
  };

  /**
   * The acknowledgement strategy
   *
   * @return
   */
  public abstract AckStrategy strategy();

}
