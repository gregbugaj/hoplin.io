package examples.prority;

import examples.BaseExample;
import examples.LogDetail;
import io.hoplin.ExchangeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmitPriority extends BaseExample {

  private static final Logger log = LoggerFactory.getLogger(EmitPriority.class);

  private static final String EXCHANGE = "direct_logs";

  public static void main(final String... args) throws InterruptedException {
    log.info("Starting producer on exchange : {}", EXCHANGE);
    final ExchangeClient client = ExchangeClient.direct(options(), EXCHANGE);

    client.publish(createMessage("info"), "info");

  }

  private static LogDetail createMessage(final String level) {
    return new LogDetail("Msg : " + System.nanoTime(), level);
  }
}

