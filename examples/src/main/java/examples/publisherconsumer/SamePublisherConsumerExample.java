package examples.publisherconsumer;

import examples.BaseExample;
import examples.LogDetail;
import io.hoplin.ExchangeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example of Publisher/Consumer
 */
public class SamePublisherConsumerExample extends BaseExample
{
    private static final Logger log = LoggerFactory.getLogger(SamePublisherConsumerExample.class);

    private static final String EXCHANGE = "topic_logs";

    public static void main(final String... args) throws InterruptedException
    {
        log.info("Starting producer/consumer for exchange : {}", EXCHANGE);
        final ExchangeClient client = ExchangeClient.topic(options(), EXCHANGE);
        client.subscribe("Test", LogDetail.class, SamePublisherConsumerExample::handle);

        for(int i = 0; i < 5; ++i)
        {
            client.publish(createMessage("info"), "log.info.info");
            client.publish(createMessage("debug"), "log.info.debug");

            Thread.sleep(1000L);
        }
    }

    private static void handle(final LogDetail msg)
    {
        log.info("Incoming msg : {}", msg);
    }

    private static LogDetail createMessage(final String level)
    {
      return new LogDetail("Msg : " + System.nanoTime(), level);
    }

}

