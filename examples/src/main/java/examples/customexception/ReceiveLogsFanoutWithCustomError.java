package examples.customexception;

import examples.BaseExample;
import io.hoplin.ExchangeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReceiveLogsFanoutWithCustomError extends BaseExample
{
    private static final Logger log = LoggerFactory.getLogger(ReceiveLogsFanoutWithCustomError.class);

    private static final String EXCHANGE = "fanout_logs";

    public static void main(final String... args) throws InterruptedException
    {
        final ExchangeClient client = ExchangeClient.fanout(options(), EXCHANGE);

        client.subscribe("test", String.class, ReceiveLogsFanoutWithCustomError::handle);
        Thread.currentThread().join();
    }

    private static void handle(final String msg)
    {
        log.info("Incoming msg : {}", msg);
    }
}
