package clientspam;

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
 * Example of BAD client usage
 */
public class EmitMetrics extends BaseExample {

  private static final Logger log = LoggerFactory.getLogger(EmitMetrics.class);

  private static final String EXCHANGE = "examples.spam";

  public static void main(final String... args) throws InterruptedException {
    log.info("Starting producer for exchange : {}", EXCHANGE);

    for(int i = 0 ; i < 1000; ++i) {
      final ExchangeClient client = clientFromExchange();
      client.publish(createMessage("warning"), "log.spam");
    }

    Thread.currentThread().join();
  }

  private static void metrics(final Map<String, Map<String, String>> o) {
    log.info("Metrics Info : {}", o);
  }

  private static ExchangeClient clientFromExchange() {
    return ExchangeClient.topic(options(), EXCHANGE);
  }

  private static LogDetail createMessage(final String level) {
    return new LogDetail("Msg : " + System.nanoTime(), level);
  }
}
