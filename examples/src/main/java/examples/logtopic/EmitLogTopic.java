package examples.logtopic;

import examples.BaseExample;
import examples.LogDetail;
import io.hoplin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is example of a Work Queues with routing patterns (Topic Exchange)
 * In this one we'll server a Work Queue that will be used to distribute time-consuming tasks among multiple workers.
 *
 * When you run many workers the tasks will be shared between them.
 *
 * Log producer
 */
public class EmitLogTopic extends BaseExample
{
    private static final Logger log = LoggerFactory.getLogger(EmitLogTopic.class);

    private static final String EXCHANGE = "examples.logtopic";

    public static void main(final String... args) throws InterruptedException
    {
        log.info("Starting producer for exchange : {}", EXCHANGE);
        final ExchangeClient client = clientFromBinding();
        client.publish(createMessage("warning"), "log.critical.warning");

        Thread.currentThread().join();

        client.publish(createMessage("info"), "log.info.info");
        client.publish(createMessage("debug"), "log.info.debug");
        client.publish(createMessage("warning"), "log.critical.warning");
        client.publish(createMessage("error"), "log.critical.error");


        client.awaitQuiescence();
        Thread.currentThread().join();

        while(true)
        {
            client.publish(createMessage("info"), "log.info.info");
            client.publish(createMessage("debug"), "log.info.debug");
            client.publish(createMessage("warning"), "log.critical.warning");
            client.publish(createMessage("error"), "log.critical.error");

            Thread.sleep(1000L);
        }
    }

    private static ExchangeClient clientFromExchange()
    {
        return ExchangeClient.topic(options(), EXCHANGE);
    }

    private static ExchangeClient clientFromBinding()
    {
        final Binding binding = BindingBuilder
                .bind()
                .to(new TopicExchange(EXCHANGE))
                .withAutoAck(true)
                .withPrefetchCount(1)
                .withPublisherConfirms(true)
                .build();

        return ExchangeClient.topic(options(), binding);
    }

    private static LogDetail createMessage(final String level)
    {
      return new LogDetail("Msg : " + System.nanoTime(), level);
    }
}
