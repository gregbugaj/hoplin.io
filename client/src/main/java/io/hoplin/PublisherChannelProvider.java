package io.hoplin;

import com.rabbitmq.client.Channel;

/**
 * @see Publisher
 */
public interface PublisherChannelProvider {

  /**
   * Acquire underlying {@link Channel} for publish operations.
   *
   * @return
   */
  Channel acquirePublisherChannel();
}
