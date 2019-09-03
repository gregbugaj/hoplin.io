package io.hoplin;

import com.rabbitmq.client.Channel;

public class DefaultConsumerErrorStrategy implements ConsumerErrorStrategy {

  private final Channel channel;

  public DefaultConsumerErrorStrategy(final Channel channel) {
    this.channel = channel;
  }
}
