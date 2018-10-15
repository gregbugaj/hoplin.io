package examples.logfanout;

import examples.BaseExample;
import hoplin.io.Binding;
import hoplin.io.BindingBuilder;
import hoplin.io.FanoutExchange;
import hoplin.io.FanoutExchangeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is example of a Fanout Exchange
 *
 * Log producer
 */
public class EmitLogFanout extends BaseExample
{
    private static final Logger log = LoggerFactory.getLogger(EmitLogFanout.class);

    private static final String EXCHANGE = "fanout_logs";

    public static void main(final String... args) throws InterruptedException
    {
        final Binding binding = bind();
        log.info("Binding : {}", binding);

        final FanoutExchangeClient client = FanoutExchangeClient.publisher(options(), binding);

        while(true)
        {
            client.publish("Msg : " + System.currentTimeMillis());
            Thread.sleep(1000L);
        }

        //        Thread.currentThread().join();
    }

    private static Binding bind()
    {
        return BindingBuilder
                .bind()
                .to(new FanoutExchange(EXCHANGE));
    }

}

