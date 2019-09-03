package examples.batch;

import com.rabbitmq.client.AMQP;
import examples.BaseExample;
import examples.LogDetail;
import io.hoplin.DirectExchangeClient;
import io.hoplin.ExchangeClient;
import io.hoplin.MessageContext;
import io.hoplin.RabbitMQClient;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Batch Job receiver
 */
public class ReceiveBatchJob extends BaseExample {

  private static final Logger log = LoggerFactory.getLogger(ReceiveBatchJob.class);

  private static final String EXCHANGE = "exchange.batch";

  private static RabbitMQClient mqClient;

  public static void main(final String... args) throws InterruptedException {
    final ExchangeClient client = DirectExchangeClient.create(options(), EXCHANGE);
    mqClient = client.getMqClient();

    client.subscribe("test", LogDetail.class, ReceiveBatchJob::handle);
    Thread.currentThread().join();
  }

  private static void handle(final LogDetail msg, final MessageContext context) {
    final AMQP.BasicProperties properties = context.getProperties();
    final String replyTo = properties.getReplyTo();
    final String correlationId = properties.getCorrelationId();
    final Map<String, Object> headers = properties.getHeaders();
    final Object batchId = headers.get("x-batch-id");
    headers.put("x-batch-correlationId", correlationId);

    log.info("Incoming context        >  {}", context);
    log.info("Incoming replyTo        >  {}", replyTo);
    log.info("Incoming msg            >  {}", msg);
    log.info("Incoming correlationId  >  {}", correlationId);
    log.info("Incoming batchId        >  {}", batchId);

    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    final LogDetail reply = new LogDetail("Reply Message", "WARN");
    mqClient.basicPublish("", replyTo, reply, headers);
  }
}
