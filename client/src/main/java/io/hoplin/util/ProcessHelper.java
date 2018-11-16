package io.hoplin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Helper class for working with external processes
 */
public class ProcessHelper
{
    private static Logger log = LoggerFactory.getLogger(ProcessHelper.class);

    public static final int EXIT_FAILURE = 1;

    /**
     * Execute command arguments,
     *
     * @param waitForChild
     * @param command
     * @param env
     * @return
     */
    public static ProcessResponse execute(final boolean waitForChild,
                                          final List<String> command,
                                          final Map<String, String> env)
    {
        log.info("Executing : {}", command);
        Process process = null;
        ExecutorService executor = null;

        try
        {
            final ProcessBuilder builder = new ProcessBuilder(command);

            if (env != null && !env.isEmpty())
                builder.environment().putAll(env);

            if (OsUtil.isWindows())
                builder.redirectInput(ProcessBuilder.Redirect.from(new File("NUL")));

            int exitValue;
            String data;

            if (waitForChild)
            {
                executor = Executors.newFixedThreadPool(2);
                process = builder.start();

                final StringBuffer buffer = new StringBuffer();
                final InputStream fd0 = process.getInputStream();
                final InputStream fd1 = process.getErrorStream();

                executor.execute(() -> consume(buffer, fd0));
                executor.execute(() -> consume(buffer, fd1));

                exitValue = process.waitFor();
                data = buffer.toString();
            }
            else
            {
                builder.redirectErrorStream(true);
                process = builder.start();

                try (final BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream())))
                {
                    final StringBuilder output = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null)
                    {
                        output.append(line).append(System.lineSeparator());
                    }
                    // Wait for the return code
                    exitValue = process.waitFor();
                    data = output.toString();
                }
            }

            return new ProcessResponse(data, exitValue);
        }

        catch (final InterruptedException ex)
        {
            process.destroyForcibly();

            log.error("Unable to execute process within given timeframe", ex);
            return new ProcessResponse(ex.getMessage(), EXIT_FAILURE);
        }
        catch (final Exception ex)
        {
            if (process != null)
                process.destroyForcibly();

            log.error("Unable to execute process", ex);
            return new ProcessResponse(ex.getMessage(), EXIT_FAILURE);
        }
        finally
        {
            if (waitForChild && executor != null)
                executor.shutdownNow();
        }
    }

    private static void consume(final StringBuffer buffer, final InputStream stream)
    {
        Objects.requireNonNull(buffer);
        Objects.requireNonNull(stream);

        try (final BufferedReader br = new BufferedReader(new InputStreamReader(stream)))
        {
            String line;
            while ((line = br.readLine()) != null)
            {
                buffer.append(line).append(System.lineSeparator());
            }
        }
        catch (final IOException e)
        {
            log.warn("Unable to read stream", e);
        }
    }

}
