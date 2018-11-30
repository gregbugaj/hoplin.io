package examples.batch;

import examples.BaseExample;
import examples.LogDetail;
import examples.rpc.RpcClientExample;
import io.hoplin.Binding;
import io.hoplin.BindingBuilder;
import io.hoplin.DirectExchange;
import io.hoplin.batch.BatchClient;
import io.hoplin.batch.DefaultBatchClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

public class BatchProcessingExample extends BaseExample
{
    private static final Logger log = LoggerFactory.getLogger(RpcClientExample.class);

    public static void main(final String... args) throws IOException
    {
        final Binding binding = bind();
        log.info("Binding : {}", binding);

        final BatchClient client = new DefaultBatchClient();
        final UUID batchId = client.startNew(context ->
        {
            context.enque(() -> new LogDetail("Msg : " + System.nanoTime(), "info"));
            context.enque(() -> new LogDetail("Msg : " + System.nanoTime(), "warn"));
        });
    }

    private static Binding bind()
    {
        return BindingBuilder
                .bind("batch.request")
                .to(new DirectExchange("exchange.batch"))
                .build()
                ;
    }
}
