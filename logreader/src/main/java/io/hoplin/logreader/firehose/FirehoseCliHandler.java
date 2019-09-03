package io.hoplin.logreader.firehose;

import io.hoplin.logreader.cli.CliHandler;
import io.hoplin.logreader.cli.ConsoleDevice;
import io.hoplin.logreader.cli.Environment;
import java.io.PrintWriter;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads messages from the firehose queues
 *
 * <pre>
 *     turn firehose on with "rabbitmqctrl trace_on"
 * </pre>
 *
 * <pre>
 *  log-reader -firehose
 * </pre>
 */
public class FirehoseCliHandler implements CliHandler {

  private static final Logger log = LoggerFactory.getLogger(FirehoseCliHandler.class);

  private final PrintWriter out;

  public FirehoseCliHandler(final OptionParser parser) {
    parser.accepts("firehose");

    out = ConsoleDevice.console().writer();
  }

  @Override
  public boolean handle(final Environment environment, final OptionSet options) {
    if (options.has("firehose")) {
      log.info("cmd [firehose] : {}", options);
    }

    return false;
  }

  @Override
  public boolean isReplEnabled() {
    return true;
  }

  @Override
  public boolean canHandle(final OptionSet options) {
    return options.has("firehose");
  }

  @Override
  public boolean requiresEnvironment() {
    return true;
  }

}
