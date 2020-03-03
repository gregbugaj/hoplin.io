package examples.logfanout;

import examples.BaseExample;
import io.hoplin.ExchangeClient;
import io.hoplin.FanoutExchangeClient;
import io.hoplin.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReceiveLogsFanout extends BaseExample {

  private static final Logger log = LoggerFactory.getLogger(EmitLogFanout.class);

  private static final String EXCHANGE = "fanout_logs";

  public static void main(final String... args) throws InterruptedException {
    final ExchangeClient client = FanoutExchangeClient.create(options(), EXCHANGE);

    client.subscribe("Test", String.class, ReceiveLogsFanout::handleWithContext);
    Thread.currentThread().join();
  }

  private static void handleWithContext(final String msg, final MessageContext context) {
      log.info("Incoming msg     >  {}", msg);
  }

  private static void handle(final String msg) {
    log.info("Incoming msg     >  {}", msg);
  }

}
