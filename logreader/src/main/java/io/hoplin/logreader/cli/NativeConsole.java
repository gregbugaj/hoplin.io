package io.hoplin.logreader.cli;

import java.io.Console;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Objects;

public class NativeConsole extends ConsoleDevice
{
    private final Console delegate;

    public NativeConsole(final Console console)
    {
        this.delegate = Objects.requireNonNull(console);
    }

    @Override
    public ConsoleDevice printf(final String format, final Object... args)
    {
        delegate.printf(format, args);
        return this;
    }

    @Override
    public String readLine()
    {
        return delegate.readLine();
    }

    @Override
    public char[] readPassword()
    {
        return delegate.readPassword();
    }

    @Override
    public Reader reader()
    {
        return delegate.reader();
    }

    @Override
    public PrintWriter writer()
    {
        
        return delegate.writer();
    }

    @Override
    public String readLine(final String fmt, final Object... args)
    {
        return delegate.readLine(fmt, args);
    }

    @Override
    public void close() {
        // noop
    }

}
