package io.hoplin;

import static io.hoplin.RetryDecision.no;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * No-op retry policy
 */
public class NoRetryPolicy implements RetryPolicy {

  private static final Logger log = LoggerFactory.getLogger(NoRetryPolicy.class);

  @Override
  public void retry(final ErrorMessage message) {
    throw new HoplinRuntimeException("Policy should not have been called");
  }

  @Override
  public RetryDecision shouldRetry(final ErrorMessage message) {
    return no();
  }
}
