package examples.logdirect;

import examples.BaseExample;
import examples.LogDetail;
import io.hoplin.DirectExchangeClient;
import io.hoplin.ExchangeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReceiveLogsDirect extends BaseExample
{
    private static final Logger log = LoggerFactory.getLogger(EmitLogDirect.class);

    private static final String EXCHANGE = "direct_logs";

    public static void main(final String... args) throws InterruptedException
    {
        final ExchangeClient client = critical();
        client.subscribe(LogDetail.class, msg-> log.info("Message received [{}]", msg));

        Thread.currentThread().join();
    }

    private static ExchangeClient critical()
    {
        return ExchangeClient
                .direct(options(), EXCHANGE, "log.critical", "error");
    }

    private static ExchangeClient informative()
    {
        return ExchangeClient
                .direct(options(), EXCHANGE, "log.informative", "info");
    }
}
