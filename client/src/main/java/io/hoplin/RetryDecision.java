package io.hoplin;

import java.time.Duration;

/**
 * {@link RetryPolicy} decision
 */
public class RetryDecision {

  // Should the retry be performed
  private final boolean shouldRetry;

  // The duration to wait before performing the retry.
  private Duration retryDelay;

  public RetryDecision(boolean shouldRetry) {
    this.shouldRetry = shouldRetry;
  }

  public RetryDecision(boolean shouldRetry, Duration retryDelay) {
    this.shouldRetry = shouldRetry;
    this.retryDelay = retryDelay;
  }

  /**
   * Should the retry be performed
   *
   * @return
   */
  public boolean shouldRetry() {
    return shouldRetry;
  }

  public Duration getRetryDelay() {
    return retryDelay;
  }

  /**
   * Create decision that will be retried after {@link #retryDelay}
   *
   * @param retryDelay
   * @return
   */
  public static RetryDecision retryWithDelay(Duration retryDelay) {
    return new RetryDecision(true, retryDelay);
  }

  public static RetryDecision yes() {
    return new RetryDecision(true);
  }

  public static RetryDecision no() {
    return new RetryDecision(false);
  }
}
