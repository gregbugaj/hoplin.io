package clientspam;

import examples.BaseExample;
import examples.LogDetail;
import io.hoplin.ExchangeClient;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example of BAD client usage
 */
public class SpamClients extends BaseExample {

  private static final Logger log = LoggerFactory.getLogger(SpamClients.class);

  private static final String EXCHANGE = "examples.spam";

  public static void main(final String... args) throws InterruptedException {
    log.info("Starting producer for exchange : {}", EXCHANGE);

    for(int i = 0 ; i < 1; ++i) {
      final ExchangeClient client = clientFromExchange();
      client.publish(createMessage("warning"), "log.spam");

      client.awaitQuiescence();
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
