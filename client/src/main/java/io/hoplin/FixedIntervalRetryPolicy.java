package io.hoplin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retry polity that performs retries attempts number of times at fixed intervals
 */
public class FixedIntervalRetryPolicy implements RetryPolicy {

  private static final Logger log = LoggerFactory.getLogger(FixedIntervalRetryPolicy.class);

  @Override
  public void retry(final ErrorMessage message) {
    if (log.isDebugEnabled()) {
      log.debug("No retry policy : {}", message);
    }
  }

  @Override
  public RetryDecision shouldRetry(ErrorMessage message) {
    return RetryDecision.yes();
  }
}





