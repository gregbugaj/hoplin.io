
package io.hoplin;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retry strategy that allows for exponential backoff between messages
 */
public class ExponentialBackoffRetryPolicy implements RetryPolicy {

  private static final Logger log = LoggerFactory.getLogger(ExponentialBackoffRetryPolicy.class);

  @Override
  public void retry(final ErrorMessage message) {
    if (log.isDebugEnabled()) {
      log.debug("Exponential retry policy : {}", message);
    }
  }

  @Override
  public RetryDecision shouldRetry(ErrorMessage message) {
    return RetryDecision.retryWithDelay(Duration.of(1, ChronoUnit.SECONDS));
  }
}
