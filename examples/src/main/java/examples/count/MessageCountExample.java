package examples.count;

import examples.BaseExample;
import examples.LogDetail;
import io.hoplin.ExchangeClient;
import io.hoplin.QueueStats;
import io.hoplin.RabbitMQClient;
import io.hoplin.SubscriptionResult;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example of Publisher/Consumer to show to how to get message count
 *
 * Stats collection happens every 5 seconds (5000 ms).
 * https://www.rabbitmq.com/management.html#statistics-interval
 */
public class MessageCountExample extends BaseExample {

  private static final Logger log = LoggerFactory.getLogger(MessageCountExample.class);

  private static final String EXCHANGE = "direct_logs";

  public static void main(final String... args) throws InterruptedException {
    log.info("Starting producer/consumer for exchange : {}", EXCHANGE);

    final ExchangeClient client = ExchangeClient.direct(options(), EXCHANGE);
    final SubscriptionResult subscription = client
        .subscribe("SubId-01", LogDetail.class, MessageCountExample::handle);
    log.info("subscription  : {}", subscription);

    final ScheduledExecutorService scheduler = Executors
        .newSingleThreadScheduledExecutor();

    scheduler.scheduleAtFixedRate(monitor(client.getMqClient(), subscription), 1, 500, TimeUnit.MILLISECONDS);

    for (int i = 0; i < 20; ++i) {
      client.publish(createMessage("info"), "log.info.info");
      Thread.sleep(1000L);
    }

    scheduler.shutdown();
  }

  private static Runnable monitor(RabbitMQClient mqClient, SubscriptionResult subscription) {
    return () -> {
      final String queue = subscription.getQueue();
      final QueueStats stats = mqClient.messageCount(queue);
      log.info("Stats : {}", stats);
    };
  }

  private static void handle(final LogDetail msg) {
    log.info("Incoming msg : {}", msg);
    try {
      Thread.sleep(10000L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private static LogDetail createMessage(final String level) {
    return new LogDetail("Msg : " + System.nanoTime(), level);
  }

}

