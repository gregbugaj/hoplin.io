package examples.metrics;

import examples.BaseExample;
import examples.LogDetail;
import examples.logdirect.EmitLogDirect;
import io.hoplin.ExchangeClient;
import io.hoplin.SubscriptionResult;
import io.hoplin.metrics.FunctionMetricsPublisher;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Metrics usage example for consumer
 */
public class MetricsReceiver extends BaseExample {

  private static final Logger log = LoggerFactory.getLogger(EmitLogDirect.class);

  public static void main(final String... args) throws InterruptedException {

    // Setup metrics
    FunctionMetricsPublisher
        .consumer(MetricsReceiver::metrics)
        .withInterval(1, TimeUnit.SECONDS)
        .withResetOnReporting(false)
        .build()
        .start();

    // Exchange and Binding Queue will be determined based on the supplied Type of the Message
    final ExchangeClient client = ExchangeClient.topic(options());
    final SubscriptionResult sub = client
        .subscribe("test", LogDetail.class, msg -> {
          log.info("Message received [{}]", msg);
        });

    info(sub);
    Thread.currentThread().join();
  }

  private static void metrics(Object o) {
    log.info("Metrics Info : {}", o);
  }
}
