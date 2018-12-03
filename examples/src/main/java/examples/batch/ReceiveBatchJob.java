package examples.batch;

import com.rabbitmq.client.AMQP;
import examples.BaseExample;
import examples.LogDetail;
import io.hoplin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Batch Job receiver
 *
 */
public class ReceiveBatchJob extends BaseExample
{
    private static final Logger log = LoggerFactory.getLogger(ReceiveBatchJob.class);

    private static final String EXCHANGE = "exchange.batch";

    private static RabbitMQClient mqClient;

    public static void main(final String... args) throws InterruptedException
    {
        final ExchangeClient client = DirectExchangeClient.create(options(), EXCHANGE);
        mqClient = client.getMqClient();

        client.subscribe(LogDetail.class, ReceiveBatchJob::handle);
        Thread.currentThread().join();
    }

    private static void handle(final LogDetail msg, final MessageContext context)
    {
        final AMQP.BasicProperties properties = context.getProperties();
        final String replyTo = properties.getReplyTo();
        final String correlationId = properties.getCorrelationId();
        final Object batchId = properties.getHeaders().get("x-batch-id");

        log.info("Incoming context        >  {}", context);
        log.info("Incoming replyTo        >  {}", replyTo);
        log.info("Incoming msg            >  {}", msg);
        log.info("Incoming correlationId  >  {}", correlationId);
        log.info("Incoming batchId        >  {}", batchId);

        LogDetail reply = new LogDetail("Reply Message", "WARN");
        
        mqClient.basicPublish("", replyTo, reply);
    }

}
