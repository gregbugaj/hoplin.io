package io.hoplin.logreader;

import io.hoplin.logreader.cli.CliHandler;
import io.hoplin.logreader.cli.Environment;
import io.hoplin.logreader.cli.ReplCliHandler;
import io.hoplin.logreader.firehose.FirehoseCliHandler;
import io.hoplin.logreader.version.VersionCliHandler;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Connects to RabbitMQ log exchanges and logs all received messages to the console.
 * This requires that the fire hose features it turned on
 *
 * <pre>
 *     turn firehose on with "rabbitmqctrl trace_on"
 * </pre>
 */
public class ApplicationFirehose
{
    public static final Logger log = LoggerFactory.getLogger(ApplicationFirehose.class);

    public static void main(final String[] args) throws IOException
    {
        log.info("Args : {}", Arrays.toString(args));

        final OptionParser parser = new OptionParser();

        final List<CliHandler> handlers = new ArrayList<>();
        final ReplCliHandler replCliHandler = new ReplCliHandler(parser);

        handlers.add(replCliHandler);
        handlers.add(new VersionCliHandler(parser));
        handlers.add(new FirehoseCliHandler(parser));

        // Environment
        parser.accepts("conf-dir", "Location of where configs can be located").withRequiredArg();
        parser.accepts("env", "Environment [--env=dev]").withRequiredArg();

        final OptionSet options = parser.parse(args);
        final Environment env = createEnvironment(options);
        log.info("Environment : {}", env);
        env.setReplCliHandler(replCliHandler);

        parser.acceptsAll(asList("help", "?"), "show help").forHelp();
        parser.printHelpOn(System.out);

        handle(options, env, handlers);
    }

    private static Environment createEnvironment(final OptionSet options)
    {
        String envDir = null;
        String env;

        if (options.has("conf-dir"))
        {
            envDir = options.valueOf("conf-dir").toString();
            log.info("cmd [conf-dir] : {}", options);
        }

        if (options.has("env"))
        {
            env = options.valueOf("env").toString();
            System.setProperty(ConfigConstants.SYSTEM_RUNTIME_ENVIRONMENT, env);
            final String rt = System.getProperty(ConfigConstants.SYSTEM_RUNTIME_ENVIRONMENT);
            log.info("cmd [env] : {} ::: {}", env, rt);
        }
        else
        {
            throw new RuntimeException("Required 'env' is not present ex --env=dev");
        }

        return Environment.create(envDir, env);
    }

    private static void handle(final OptionSet options, final Environment env, final List<CliHandler> handlers)
    {
        for (final CliHandler handler : handlers)
        {
            handler.handle(env, options);
        }
    }
}
