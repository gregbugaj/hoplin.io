package examples.logdirect;

import examples.BaseExample;
import examples.LogDetail;
import hoplin.io.DirectExchangeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReceiveLogsDirect extends BaseExample
{
    private static final Logger log = LoggerFactory.getLogger(EmitLogDirect.class);

    private static final String EXCHANGE = "direct_logs";

    public static void main(final String... args) throws InterruptedException
    {
        final DirectExchangeClient client = critical();
        client.subscribe(LogDetail.class, msg-> log.info("Message received [{}]", msg));

        Thread.currentThread().join();
    }

    private static DirectExchangeClient critical()
    {
        return DirectExchangeClient
                .subscriberWithQueue(options(), EXCHANGE, "log.critical","error", "warning");
    }

    private static DirectExchangeClient informative()
    {
        return DirectExchangeClient
                .subscriberWithQueue(options(), EXCHANGE, "log.informative","info", "debug");
    }

    private static DirectExchangeClient all()
    {
        return DirectExchangeClient
                .subscriberWithQueue(options(), EXCHANGE, "log.all","info", "debug", "error", "warning");
    }

}
