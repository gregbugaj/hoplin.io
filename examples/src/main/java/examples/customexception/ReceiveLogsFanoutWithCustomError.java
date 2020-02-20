package examples.customexception;

import examples.BaseExample;
import io.hoplin.ExchangeClient;
import io.hoplin.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReceiveLogsFanoutWithCustomError extends BaseExample {

  private static final Logger log = LoggerFactory.getLogger(ReceiveLogsFanoutWithCustomError.class);

  private static final String EXCHANGE = "fanout_logs";

  public static void main(final String... args) throws InterruptedException {
    final ExchangeClient client = ExchangeClient.fanout(options(), EXCHANGE);

    client.subscribe("test", String.class, ReceiveLogsFanoutWithCustomError::handle);
    Thread.currentThread().join();
  }

  private static void handle(final String msg, MessageContext context) {
    try {

      log.info("Incoming msg : {}, {}", msg, context.getProperties());
      if (true) {
        throw new IllegalStateException("Processing error");
      }

    } finally {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        // noop
      }
    }
  }
}
