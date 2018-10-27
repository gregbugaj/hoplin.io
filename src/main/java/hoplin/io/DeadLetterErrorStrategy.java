package hoplin.io;

import com.rabbitmq.client.Channel;

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
    public DeadLetterErrorStrategy(final Channel channel)
    {
        super(channel);
    }

    @SuppressWarnings("unchecked")
    @Override
    public AckStrategy handleConsumerError(final ConsumerExecutionContext context, final Throwable throwable)
    {
        if(context != null)
        {
            final Map<String, Object> headers = context.getProperties().getHeaders();

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

            if(retries < 3)
                return AcknowledgmentStrategies.NACK_WITHOUT_REQUEUE.strategy();
        }

        return super.handleConsumerError(context, throwable);
    }
}
