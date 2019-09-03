package io.hoplin;

import static java.nio.charset.Charset.defaultCharset;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ReturnListener;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle un-routable messages. This handler will simply output incoming message to the unroutable
 * directory determined from the current path.
 * <p>
 * Unroutable messages are silently dropped by a broker
 * <code>mandatory</code> flag needs to be set while calling channel.basicPublish
 */
public class UnroutableMessageReturnListener implements ReturnListener {

  private static final Logger log = LoggerFactory.getLogger(UnroutableMessageReturnListener.class);

  private final RabbitMQOptions options;

  private Path unroutableDirectory;

  public UnroutableMessageReturnListener(final RabbitMQOptions options) {
    this.options = Objects.requireNonNull(options);
    this.unroutableDirectory = options.getUnroutableDirectory();

  }

  @Override
  public void handleReturn(int replyCode,
      String replyText,
      String exchange,
      String routingKey,
      AMQP.BasicProperties properties,
      byte[] body) {
    log.warn("Message not delivered : {}, {}, {}", options.isKeepUnroutableMessages(), exchange,
        routingKey);
    if (!options.isKeepUnroutableMessages()) {
      return;
    }

    final File out = new File(unroutableDirectory.toFile(), System.currentTimeMillis() + ".msg");

    try (final BufferedWriter writer = Files.newBufferedWriter(out.toPath(), defaultCharset())) {
      final StringBuilder debugProps = new StringBuilder();
      properties.appendPropertyDebugStringTo(debugProps);

      writer.write(replyCode);
      writer.write(System.lineSeparator());
      writer.write(replyText);
      writer.write(System.lineSeparator());
      writer.write(exchange);
      writer.write(System.lineSeparator());
      writer.write(routingKey);
      writer.write(System.lineSeparator());
      writer.write(debugProps.toString());
      writer.write(System.lineSeparator());
      writer.write("------------------------");
      writer.write(new String(body, Charset.defaultCharset()));
    } catch (final Exception e) {
      log.error("Unable to store un-routable message");
    }
  }
}
