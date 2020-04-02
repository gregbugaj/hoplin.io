package examples.metrics;

import examples.BaseExample;
import examples.LogDetail;
import io.hoplin.Binding;
import io.hoplin.BindingBuilder;
import io.hoplin.ExchangeClient;
import io.hoplin.TopicExchange;
import io.hoplin.metrics.FunctionMetricsPublisher;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Metrics usage example for publisher
 */
public class EmitMetrics extends BaseExample {

  private static final Logger log = LoggerFactory.getLogger(EmitMetrics.class);

  private static final String EXCHANGE = "examples.metrics";

  public static void main(final String... args) throws InterruptedException {
    FunctionMetricsPublisher
        .consumer(EmitMetrics::metrics)
        .withInterval(1, TimeUnit.SECONDS)
        .withResetOnReporting(false)
        .build()
        .start();

    log.info("Starting producer for exchange : {}", EXCHANGE);
    final ExchangeClient client = clientFromBinding();
    client.publish(createMessage("warning"), "log.critical.warning");

    Thread.currentThread().join();

    if (true) {
      return;
    }

    client.awaitQuiescence();
    Thread.currentThread().join();

    while (true) {
      client.publish(createMessage("info"), "log.info.info");
      client.publish(createMessage("debug"), "log.info.debug");
      client.publish(createMessage("warning"), "log.critical.warning");
      client.publish(createMessage("error"), "log.critical.error");
      Thread.sleep(1000L);
    }
  }

  private static void metrics(final Map<String, Map<String, String>> o) {
    log.info("Metrics Info : {}", o);
  }

  private static ExchangeClient clientFromExchange() {
    return ExchangeClient.topic(options(), EXCHANGE);
  }

  private static ExchangeClient clientFromBinding() {
    final Binding binding = BindingBuilder
        .bind()
        .to(new TopicExchange(EXCHANGE))
        .withAutoAck(true)
        .withPrefetchCount(1)
        .withPublisherConfirms(true)
        .build();

    return ExchangeClient.topic(options(), binding);
  }

  private static LogDetail createMessage(final String level) {
    return new LogDetail("Msg : " + System.nanoTime(), level);
  }
}
