package examples.logtopic;

import examples.BaseExample;
import examples.LogDetail;
import examples.logdirect.EmitLogDirect;
import io.hoplin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReceiveLogsTopic extends BaseExample
{
    private static final Logger log = LoggerFactory.getLogger(EmitLogDirect.class);

    private static final String EXCHANGE = "topic_logs";

    public static void main(final String... args) throws InterruptedException
    {
//      final ExchangeClient client = clientFromExchange(EXCHANGE, "log.critical", "log.critical.*");
        final ExchangeClient client = clientFromBinding(EXCHANGE, "log.critical", "log.critical.*");
        client.subscribe("test", LogDetail.class, msg-> log.info("Message received [{}]", msg));

        Thread.currentThread().join();
    }

    private static ExchangeClient clientFromExchange(final String exchange, final String queue, final String routingKey)
    {
        return ExchangeClient.topic(options(), exchange, queue, routingKey);
    }

    /**
     * Creating client with binding allows us for more granular control
     * @param exchange
     * @param queue
     * @param routingKey
     * @return
     */
    private static ExchangeClient clientFromBinding(final String exchange, final String queue, final String routingKey)
    {
        final Binding binding = BindingBuilder
                .bind(queue)
                .to(new TopicExchange(exchange))
                .withAutoAck(true)
                .withPrefetchCount(1)
                .withPublisherConfirms(true)
                .with(routingKey)
                .build();

        return ExchangeClient.topic(options(), binding);
    }
}
