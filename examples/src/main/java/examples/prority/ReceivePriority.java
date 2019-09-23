package examples.prority;

import examples.BaseExample;
import examples.LogDetail;
import examples.logdirect.EmitLogDirect;
import io.hoplin.ExchangeClient;
import io.hoplin.SubscriptionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ReceivePriority extends BaseExample {

  private static final Logger log = LoggerFactory.getLogger(EmitLogDirect.class);

  private static final String EXCHANGE = "direct_logs";

  public static void main(final String... args) throws InterruptedException {

    final ExchangeClient client = informative();
    final SubscriptionResult subscription = client
        .subscribe("test", LogDetail.class, msg -> {log.info("Message received [{}]", msg);});

    info(subscription);
    Thread.currentThread().join();
  }

  private static ExchangeClient critical() {
    return ExchangeClient
        .direct(options(), EXCHANGE, "log.critical", "error");
  }

  private static ExchangeClient informative() {
    return ExchangeClient
        .direct(options(), EXCHANGE, "log.informative", "info");
  }
}
