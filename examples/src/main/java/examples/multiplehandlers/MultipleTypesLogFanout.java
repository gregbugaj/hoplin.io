package examples.multiplehandlers;

import examples.BaseExample;
import examples.LogDetail;
import io.hoplin.Binding;
import io.hoplin.BindingBuilder;
import io.hoplin.ExchangeClient;
import io.hoplin.FanoutExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MultipleTypesLogFanout extends BaseExample
{
    private static final Logger log = LoggerFactory.getLogger(MultipleTypesLogFanout.class);

    private static final String EXCHANGE = "mh_logs";

    public static void main(final String... args) throws InterruptedException
    {
        final Binding binding = bind();
        log.info("Binding : {}", binding);
        final ExchangeClient client = ExchangeClient.fanout(options(), binding);

        client.publish(new LogDetail("DetailType A", "info"));
//      client.publish(new LogDetailType2("DetailType B", "info"));
    }

    private static Binding bind()
    {
        return BindingBuilder
                .bind()
                .to(new FanoutExchange(EXCHANGE));
    }

}

