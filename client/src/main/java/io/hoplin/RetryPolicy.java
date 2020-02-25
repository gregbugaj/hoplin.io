package io.hoplin;

/**
 * Interface that provides retry policy
 */
public interface RetryPolicy {

  /**
   * Retry failed {@link ErrorMessage}
   *
   * @param message
   */
  void retry(final ErrorMessage message);

  /**
   * Determine if retry should happen
   *
   * @param message
   * @return
   */
  RetryDecision shouldRetry(final ErrorMessage message);

}
