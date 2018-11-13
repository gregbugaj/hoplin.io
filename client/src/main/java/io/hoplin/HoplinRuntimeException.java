package io.hoplin;

public class HoplinRuntimeException extends RuntimeException
{

    public HoplinRuntimeException(final Throwable e)
    {
        super(e);
    }

    public HoplinRuntimeException(final String msg, final Throwable e)
    {
        super(msg, e);
    }

    public HoplinRuntimeException(final String msg)
    {
        super(msg);
    }

}
