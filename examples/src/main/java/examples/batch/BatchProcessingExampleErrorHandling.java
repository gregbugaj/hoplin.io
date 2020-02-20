package examples.batch;

import examples.BaseExample;
import examples.LogDetail;
import io.hoplin.Binding;
import io.hoplin.BindingBuilder;
import io.hoplin.DirectExchange;
import io.hoplin.batch.BatchClient;
import io.hoplin.batch.DefaultBatchClient;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Batch processing error handling
 */
public class BatchProcessingExampleErrorHandling extends BaseExample {

  private static final Logger log = LoggerFactory
      .getLogger(BatchProcessingExampleErrorHandling.class);

  public static void main(final String... args) throws IOException, InterruptedException {
    final BatchClient client = new DefaultBatchClient(options(), bind());

    client.startNew(context ->
    {
      context.enqueue(() -> new LogDetail("Msg >> " + System.nanoTime(), "info"));
//      context.enqueue(() -> new LogDetail("Msg >> " + System.nanoTime(), "warn"));
    })
        .whenComplete((context, throwable) ->
        {
          log.info("Batch completed in : {}", context.duration());
        });

    Thread.currentThread().join();
  }

  private static Binding bind() {
    return BindingBuilder
        .bind("batch.documents")
        .to(new DirectExchange("exchange.batch"))
        .build()
        ;
  }
}
