package io.hoplin.logreader.cli;

import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public interface CliHandler
{
    /**
     * Check if command can be handled
     *
     * @param environment the environment where the command will be executed
     * @param options the {@link OptionSet} parsed from command line
     *
     * @return {@code false} if we want to stop processing reminder of handlers, otherwise {@code true}
     */
    boolean handle(final Environment environment, final OptionSet options);

    /**
     * Check if handler can handlePan given command
     *
     * @param options the options to check
     * @return {@code true} if the command can be handled, {@code false} otherwise
     */
    boolean canHandle(final OptionSet options);

    /**
     * Check if the the environment is required
     *
     * @return {@code true} if it is required, {@code false} otherwise
     */
    boolean requiresEnvironment();

    /**
     * Is handler capable of supporting REPL commands
     *
     * @return {@code true} if the REPL is supported, {@code false} otherwise
     */
    boolean isReplEnabled();

    /**
     * Get value or default value from the {@link OptionSet} based on the tag
     *
     * @param options
     * @param tag
     * @param defaultValue
     * @return value of default value if no value was present for the tag
     */
    default String getOrDefault(final OptionSet options, final String tag, final String defaultValue)
    {
        if (options.has(tag))
        {
            final Object val = options.valueOf(tag);
            return val == null ? defaultValue : val.toString();
        }

        return defaultValue;
    }

    /**
     * Get value from the {@code #tag} or return {@link Optional}
     * @param options the options to get value from
     * @param tag the tag to get
     * @return value in {@link Optional} or {@link Optional#empty()}
     */
    default Optional<String> getOrOptional(final OptionSet options, final String tag)
    {
        if (options.has(tag))
        {
            if(!options.hasArgument(tag))
                return Optional.empty();

            final Object val = options.valueOf(tag);
            if(val != null)
                return Optional.of(val.toString());
        }

        return Optional.empty();
    }

    /**
     * Get the String representation of the {@link OptionSet}*
     * @param options the options to convert to {@link String}
     * @return
     */
    default String toString(final OptionSet options)
    {
        if(options == null)
            return "NO_OPTIONS";

        final Map<OptionSpec<?>, List<?>> spec = options.asMap();
        return  spec.toString();
    }

    /**
     * Handle invocation of the commands
     *
     * @param options
     * @param env
     * @param handlers the handlers that can be used to handlePan this {@link OptionSet}
     * @throws  IllegalStateException if the {@link Environment} was required for a handler but not present
     */
    static void handle(final OptionSet options, final Environment env, final List<CliHandler> handlers)
    {
        for (final CliHandler handler : handlers)
        {
            if(handler.canHandle(options))
            {
                if (handler.requiresEnvironment() && !env.isAvailable())
                    throw new IllegalStateException("Handler requires environment but none is available" + handler);

                if (!handler.handle(env, options))
                {
                    LogHolder.LOGGER.info("Exit requested after handler : {},", handler);
                    break;
                }
            }
        }
    }



    /**
     * Output message out to underlying {@link PrintWriter} of our {@link ConsoleDevice}
     * @param msg the message to write out
     */
    default void println(final String msg)
    {
        Objects.requireNonNull(msg);
        final PrintWriter writer = ConsoleDevice.console().writer();
        LogHolder.LOGGER.info(msg);
        writer.printf(msg);
    }

    /**
     * Output message out to underlying {@link PrintWriter} of our {@link ConsoleDevice}
     * @param format
     * @param args
     */
    default void printf(final String format, final Object... args)
    {
        Objects.requireNonNull(format);
        final PrintWriter writer = ConsoleDevice.console().writer();
        LogHolder.LOGGER.info(format, args);
        writer.printf(format, args);
    }
}

final class LogHolder
{
    static final Logger LOGGER = LoggerFactory.getLogger(CliHandler.class);
}
