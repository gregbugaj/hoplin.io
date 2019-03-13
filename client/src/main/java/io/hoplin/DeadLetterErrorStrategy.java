package io.hoplin;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;

/**
 * Implementation of Dead Letter Strategy
 *
 * https://github.com/newcontext/rabbitmq-java-client/blob/master/test/src/com/rabbitmq/client/test/functional/DeadLetterExchange.java
 * https://www.rabbitmq.com/dlx.html
 * https://medium.com/@kiennguyen88/rabbitmq-delay-retry-schedule-with-dead-letter-exchange-31fb25a440fc
 *
 */
public class DeadLetterErrorStrategy extends DefaultConsumerErrorStrategy
{
    private static final Logger log = LoggerFactory.getLogger(DeadLetterErrorStrategy.class);

    private int maxRetries = 3;

    public DeadLetterErrorStrategy(final Channel channel)
    {
        super(channel);
    }

    @SuppressWarnings("unchecked")
    @Override
    public AckStrategy handleConsumerError(final MessageContext context, final Throwable throwable)
    {

        if(true)
            return AcknowledgmentStrategies.BASIC_ACK.strategy();

        if(context == null)
        {
            log.warn("Message context is null while handling consumer error : {}", throwable);
            return AcknowledgmentStrategies.NACK_WITH_REQUEUE.strategy();
        }

        final Map<String, Object> headers = context.getProperties().getHeaders();

        if(headers == null)
        {
            log.warn("Headers are null, NACK requeue = false");
            return AcknowledgmentStrategies.NACK_WITHOUT_REQUEUE.strategy();
        }

        if(headers.containsKey("x-death"))
            return AcknowledgmentStrategies.NACK_WITHOUT_REQUEUE.strategy();

        final ArrayList<Object> death = (ArrayList<Object>)headers.get("x-death");

        if(death == null)
            return AcknowledgmentStrategies.NACK_WITHOUT_REQUEUE.strategy();

        int retries = 0;
        for(int i = 0; i < death.size(); ++i )
        {
            final  Map<String, Object> entries = (Map<String, Object>) death.get(i);
            final String attempt = (String) entries.getOrDefault("count", "0");
            final int retry = Integer.parseInt(attempt);

            retries += retry;
        }

        log.info("DLQ retry : {} of {}", retries, maxRetries);

        if(retries < maxRetries)
            return AcknowledgmentStrategies.NACK_WITHOUT_REQUEUE.strategy();

        return AcknowledgmentStrategies.NACK_WITH_REQUEUE.strategy();
    }
}
