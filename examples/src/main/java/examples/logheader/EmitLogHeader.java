package examples.logheader;

import examples.BaseExample;
import examples.LogDetail;
import io.hoplin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The message is routed based on the header value. All queues with a matching key will receive the message
 * Log producer
 */
public class EmitLogHeader extends BaseExample
{
    private static final Logger log = LoggerFactory.getLogger(EmitLogHeader.class);

    private static final String EXCHANGE = "header_logs";

    public static void main(final String... args) throws InterruptedException
    {
        log.info("Starting header producer for exchange : {}", EXCHANGE);
        final ExchangeClient client = ExchangeClient.header(options(), EXCHANGE);

        client.publish(createMessage("info"), cfg->
        {
            cfg.addHeader("type", "info");
            cfg.addHeader("category", "service-xyz");
        });
    }

    private static LogDetail createMessage(final String level)
    {
      return new LogDetail("Msg : " + System.nanoTime(), level);
    }
}
