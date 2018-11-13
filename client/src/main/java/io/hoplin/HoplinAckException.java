package io.hoplin;

/**
 * Exception thrown when consumer fails to acknowledge the message
 */
public class HoplinAckException extends Exception
{
    public HoplinAckException(final Throwable e)
    {
        super(e);
    }

    public HoplinAckException(final String msg, final Throwable e)
    {
        super(msg, e);
    }

    public HoplinAckException(final String msg)
    {
        super(msg);
    }

}
