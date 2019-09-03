package io.hoplin;

/**
 * Subscription configurator
 */
public class SubscriptionConfigurator {

  private String subscriberId;

  public SubscriptionConfigurator withSubscriberId(final String subscriberId) {
    this.subscriberId = subscriberId;
    return this;
  }

  public SubscriptionConfig build() {
    final SubscriptionConfig config = new SubscriptionConfig();
    config.setSubscriberId(subscriberId);

    return config;
  }
}
