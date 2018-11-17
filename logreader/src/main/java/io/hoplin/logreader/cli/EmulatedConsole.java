package io.hoplin.logreader.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

public class EmulatedConsole extends ConsoleDevice
{
    private final PrintWriter writer;

    private final BufferedReader reader;

    public EmulatedConsole(final BufferedReader reader, final PrintWriter writer)
    {
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public ConsoleDevice printf(final String format, final Object... args)
    {
        writer.printf(format, args);
        return this;
    }

    @Override
    public String readLine() throws ConsoleException
    {
        return readLine("");
    }

    @Override
    public char[] readPassword()
    {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Reader reader()
    {
        return reader;
    }

    @Override
    public PrintWriter writer()
    {
        return writer;
    }

    @Override
    public String readLine(final String fmt, final Object... args) throws ConsoleException
    {
        try
        {
            writer.printf(fmt, args);
            return reader.readLine();
        }
        catch (final IOException e)
        {
            throw new ConsoleException(e);
        }
    }

    @Override
    public void close() throws Exception
    {
        if (reader != null)
            reader.close();

        if (writer != null)
            writer.close();
    }

}
