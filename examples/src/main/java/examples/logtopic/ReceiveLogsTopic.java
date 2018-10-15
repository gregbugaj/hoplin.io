package examples.logtopic;

import examples.BaseExample;
import examples.LogDetail;
import examples.logdirect.EmitLogDirect;
import hoplin.io.TopicExchangeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReceiveLogsTopic extends BaseExample
{
    private static final Logger log = LoggerFactory.getLogger(EmitLogDirect.class);

    private static final String EXCHANGE = "topic_logs";

    public static void main(final String... args) throws InterruptedException
    {
        final TopicExchangeClient client = all();
        client.subscribe(LogDetail.class, msg-> log.info("Message received [{}]", msg));

        Thread.currentThread().join();
    }

    private static TopicExchangeClient critical()
    {
        return TopicExchangeClient
                .subscriberWithQueue(options(), EXCHANGE, "log.critical","log.critical.*");
    }

    private static TopicExchangeClient informative()
    {
        return TopicExchangeClient
                .subscriberWithQueue(options(), EXCHANGE, "log.informative","log.info.*");
    }

    private static TopicExchangeClient all()
    {
        return TopicExchangeClient
                .subscriberWithQueue(options(), EXCHANGE, "log.all","log.*.*");
    }

}
