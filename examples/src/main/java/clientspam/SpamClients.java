package clientspam;

import examples.BaseExample;
import examples.LogDetail;
import io.hoplin.CloseableExchangeClient;
import io.hoplin.ExchangeClient;
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
// Connections will not be closed until client exits the application
//    spam();

//    spamSingleOpenClose();

    spamSingleOpenCloseAsync();

    // Connections will be closed by calling close
//     spamAndClose();

    // Connections will  be closed via try-with-resources
//    spamAndAutoClose();

//    Thread.currentThread().join();
  }

  private static void spam() {
    for (int i = 0; i < 1; ++i) {
      final ExchangeClient client = clientFromExchange();
      client.publish(createMessage("warning"), "log.spam");
    }
  }

  private static void spamAndClose() {
    for (int i = 0; i < 10; ++i) {
      final ExchangeClient client = clientFromExchange();
      client.publish(createMessage("warning"), "log.spam");
      client.close();
    }
  }

  private static void spamAndAutoClose() {
    for (int i = 0; i < 100; ++i) {
      try (final CloseableExchangeClient client = clientFromExchange().asClosable()) {
        client.publish(createMessage("warning"), "log.spam");
      }
    }
  }

  private static void spamSingleOpenClose() {
    final ExchangeClient client = clientFromExchange();
    client.publish(createMessage("warning"), "log.spam");
    client.publish(createMessage("warning"), "log.spam");
    client.close();
  }

  /**
   * Messages should have been published and close should wait before terminating
   */
  private static void spamSingleOpenCloseAsync() {
    final ExchangeClient client = clientFromExchange();
    client.publishAsync(createMessage("warning"), "log.spam");
    client.publishAsync(createMessage("warning"), "log.spam");

    client.close();
  }

  private static ExchangeClient clientFromExchange() {
    return ExchangeClient.topic(options(), EXCHANGE);
  }

  private static LogDetail createMessage(final String level) {
    return new LogDetail("Msg : " + System.nanoTime(), level);
  }
}
