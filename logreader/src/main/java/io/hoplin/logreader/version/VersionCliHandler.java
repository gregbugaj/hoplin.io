package io.hoplin.logreader.version;

import io.hoplin.logreader.cli.CliHandler;
import io.hoplin.logreader.cli.ConsoleDevice;
import io.hoplin.logreader.cli.Environment;
import java.io.PrintWriter;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Print out the version information regarding our application
 * <pre>
 *  log-reader -version
 * </pre>
 */
public class VersionCliHandler implements CliHandler {

  private static final Logger log = LoggerFactory.getLogger(VersionCliHandler.class);

  private final PrintWriter out;

  public VersionCliHandler(final OptionParser parser) {
    parser.accepts("version");
    out = ConsoleDevice.console().writer();
  }

  @Override
  public boolean handle(final Environment environment, final OptionSet options) {
    if (options.has("version")) {
      log.info("cmd [version] : {}", options);
      final String buildInfo = BuildInfo.getBuildInfo();
      log.info("BuildInfo : {}", buildInfo);
      out.print(buildInfo);
    }
    return false;
  }

  @Override
  public boolean isReplEnabled() {
    return true;
  }

  @Override
  public boolean canHandle(final OptionSet options) {
    return options.has("version");
  }

  @Override
  public boolean requiresEnvironment() {
    return false;
  }


}
