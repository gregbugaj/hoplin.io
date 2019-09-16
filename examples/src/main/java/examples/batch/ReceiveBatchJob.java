package examples.batch;

import examples.BaseExample;
import examples.LogDetail;
import io.hoplin.DirectExchangeClient;
import io.hoplin.ExchangeClient;
import io.hoplin.MessageContext;
import io.hoplin.Reply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Batch Job receiver
 */
public class ReceiveBatchJob extends BaseExample {

  private static final Logger log = LoggerFactory.getLogger(ReceiveBatchJob.class);

  private static final String EXCHANGE = "exchange.batch";


  public static void main(final String... args) throws InterruptedException {
    final ExchangeClient client = DirectExchangeClient.create(options(), EXCHANGE);

    client.subscribe("test", LogDetail.class, ReceiveBatchJob::handleWithReturn);
    Thread.currentThread().join();
  }

  private static Reply<LogDetail> handleWithReturn(final LogDetail msg,
      final MessageContext context) {
    final LogDetail reply = new LogDetail("Reply Message > " + System.currentTimeMillis(), "WARN");

    return Reply.with(reply);
  }
}
