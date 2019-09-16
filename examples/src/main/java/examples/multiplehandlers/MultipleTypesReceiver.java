package examples.multiplehandlers;

import examples.BaseExample;
import examples.LogDetail;
import examples.LogDetailType2;
import io.hoplin.ExchangeClient;
import io.hoplin.FanoutExchangeClient;
import io.hoplin.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultipleTypesReceiver extends BaseExample {

  private static final Logger log = LoggerFactory.getLogger(MultipleTypesLogFanout.class);

  private static final String EXCHANGE = "mh_logs";

  public static void main(final String... args) throws InterruptedException {
    final ExchangeClient client = FanoutExchangeClient.create(options(), EXCHANGE);

    client.subscribe("test", LogDetail.class, MultipleTypesReceiver::handle1);
    client.subscribe("test", LogDetailType2.class, MultipleTypesReceiver::handle2);

    Thread.currentThread().join();
  }

  private static void handle1(final LogDetail msg, final MessageContext context) {
    log.info("Handler-1  >  {}", msg);
  }

  private static void handle2(final LogDetailType2 msg, final MessageContext context) {
    log.info("Handler-2  >  {}", msg);
  }
}
