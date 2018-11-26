package examples.logheader;

import examples.BaseExample;
import examples.LogDetail;
import io.hoplin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The message is routed based on the header value.
 * All queues with a matching key will receive the message
 *
 * Log receiver
 */
public class ReceiveLogHeader extends BaseExample
{
    private static final Logger log = LoggerFactory.getLogger(ReceiveLogHeader.class);

    private static final String EXCHANGE = "header_logs";

    public static void main(final String... args) throws InterruptedException
    {
        log.info("Starting header consumer for exchange : {}", EXCHANGE);
        final ExchangeClient client= clientFromBinding(EXCHANGE,"info", "service-xyz");
        client.subscribe(LogDetail.class, ReceiveLogHeader::handler);

        Thread.currentThread().join();
    }

    private static void handler(final LogDetail detail)
    {
        log.info("Message received :  {} ", detail);
    }

    private static ExchangeClient clientFromBinding(String exchange, String type, String category)
    {
        final Binding binding = BindingBuilder
                .bind("header_log_info_queue")
                .to(new HeaderExchange(exchange))
                .withArgument("x-match", "all")
                .withArgument("type", type)
                .withArgument("category", category)
                .build();

        return ExchangeClient.header(options(), binding);
    }
}
