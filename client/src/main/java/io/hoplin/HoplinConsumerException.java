package io.hoplin;

/**
 * Exception thrown when consumer fails
 */
public class HoplinConsumerException extends RuntimeException
{
    public HoplinConsumerException(final Throwable e)
    {
        super(e);
    }

    public HoplinConsumerException(final String msg, final Throwable e)
    {
        super(msg, e);
    }

    public HoplinConsumerException(final String msg)
    {
        super(msg);
    }

}
