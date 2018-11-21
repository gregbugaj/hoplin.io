package examples.logfanout;

import examples.BaseExample;
import io.hoplin.ExchangeClient;
import io.hoplin.FanoutExchangeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReceiveLogsFanout extends BaseExample
{
    private static final Logger log = LoggerFactory.getLogger(EmitLogFanout.class);

    private static final String EXCHANGE = "fanout_logs";

    public static void main(final String... args) throws InterruptedException
    {
        final ExchangeClient client = FanoutExchangeClient.subscriber(options(), EXCHANGE);

        client.subscribe(String.class, ReceiveLogsFanout::handle);
        Thread.currentThread().join();
    }

    private static void handle(final String msg)
    {
        log.info("Incoming msg : {}", msg);
    }
}
