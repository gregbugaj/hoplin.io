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

public class BatchProcessingExample extends BaseExample
{
    private static final Logger log = LoggerFactory.getLogger(RpcClientExample.class);

    public static void main(final String... args) throws IOException, InterruptedException
    {
        final BatchClient client = new DefaultBatchClient(options(), bind());

        client.startNew(context ->
        {
            for(int i = 0; i < 1000; ++i)
            {
                context.enque(() -> new LogDetail("Msg >> " + System.nanoTime(), "info"));
                context.enque(() -> new LogDetail("Msg  >> " + System.nanoTime(), "warn"));
            }
        })
            .whenComplete((context, throwable)-> {

                log.info("Batch completed in : {}", context.duration());
        });

        Thread.currentThread().join();
    }

    private static Binding bind()
    {
        return BindingBuilder
                .bind("batch.documents")
                .to(new DirectExchange("exchange.batch"))
                .build()
                ;
    }
}
