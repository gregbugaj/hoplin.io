package examples.customexception;

import examples.BaseExample;
import io.hoplin.FanoutExchangeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReceiveLogsFanoutWithCustomException extends BaseExample
{
    private static final Logger log = LoggerFactory.getLogger(ReceiveLogsFanoutWithCustomException.class);

    private static final String EXCHANGE = "fanout_logs";

    public static void main(final String... args) throws InterruptedException
    {
        final FanoutExchangeClient client = FanoutExchangeClient.subscriber(options(), EXCHANGE);

        client.subscribe(String.class, ReceiveLogsFanoutWithCustomException::handle);
        Thread.currentThread().join();
    }

    private static void handle(final String msg)
    {
        log.info("Incoming msg : {}", msg);
    }
}
