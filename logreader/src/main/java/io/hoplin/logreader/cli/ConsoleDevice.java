package io.hoplin.logreader.cli;

import java.io.*;

public abstract class ConsoleDevice implements AutoCloseable
{
    private static ConsoleDevice DEFAULT = (System.console() == null) ? streamDevice(System.in, System.out)
        : new NativeConsole(System.console());

    private static ConsoleDevice console;

    public abstract ConsoleDevice printf(final String fmt,final Object... params);

    public abstract String readLine() throws ConsoleException;

    public abstract String readLine(final String fmt, final Object... args) throws ConsoleException;

    public abstract char[] readPassword();

    public abstract Reader reader();

    public abstract PrintWriter writer();

    public static ConsoleDevice streamDevice(final InputStream in, final OutputStream out)
    {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        final PrintWriter writer = new PrintWriter(out, true);
        return new EmulatedConsole(reader, writer);
    }

    public static ConsoleDevice console()
    {
        return console == null ? DEFAULT : console;
    }

    public static void console(final ConsoleDevice device)
    {
        console = device;
    }

    public static void reset()
    {
        console = null;
    }



}
