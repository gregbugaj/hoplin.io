package io.hoplin.logreader.cli;


import io.hoplin.logreader.version.VersionCliHandler;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read Evaluate Print Loop (REPL) for commands that support it
 * <pre>
 *  --repl
 * </pre>
 */
public class ReplCliHandler implements CliHandler {

  private static final String REPL_TAG = "re :> ";

  private static final Logger LOGGER = LoggerFactory.getLogger(ReplCliHandler.class);

  public ReplCliHandler(final OptionParser parser) {
    parser.accepts("repl");
  }

  @Override
  public boolean handle(final Environment environment, final OptionSet options) {
    if (options.has("repl")) {
      // probable should use the handlers defined in the entry point
      final OptionParser parser = new OptionParser();
      final List<CliHandler> handlers = handlers(parser);
      LOGGER.info("cmd [repl] : {}", options);

      try (final ConsoleDevice console = ConsoleDevice.console()) {
        do {
          final String cmd = console.readLine(REPL_TAG);
          LOGGER.info("REPL cmd : {}", cmd);
          if (cmd == null || "exit".equals(cmd)) {
            System.exit(0);
          }

          eval(environment, handlers, cmd, parser);
        } while (true);
      } catch (final Exception e) {
        LOGGER.error("Unable to process console", e);
      }
    }

    return false;
  }

  @Override
  public boolean canHandle(final OptionSet options) {
    return options.has("repl");
  }

  @Override
  public boolean requiresEnvironment() {
    return true;
  }

  public String eval(final Environment environment, final String cmd) {
    // probable should use the handlers defined in the entry point
    final OptionParser parser = new OptionParser();
    final List<CliHandler> handlers = handlers(parser);

    eval(environment, handlers, cmd, parser);
    return "ABC";
  }

  private List<CliHandler> handlers(final OptionParser parser) {
    return Stream.of(new VersionCliHandler(parser))
        .collect(Collectors.toList());
  }

  private void eval(final Environment environment,
      final List<CliHandler> handlers,
      final String cmd,
      final OptionParser replParser) {
    try {
      final String[] args = split(cmd);
      final OptionSet options = replParser.parse(args);

      for (final CliHandler handler : handlers) {
        if (handler.isReplEnabled()) {
          handler.handle(environment, options);
        }
      }
    } catch (final Exception e) {
      LOGGER.error("Unable to process REPL cmd " + cmd, e);
    }
  }

  @Override
  public boolean isReplEnabled() {
    return false;
  }

  private String[] split(final String cmd) {
    if (cmd == null || cmd.isEmpty()) {
      return new String[0];
    }

    return cmd.split(" ");
  }

}
