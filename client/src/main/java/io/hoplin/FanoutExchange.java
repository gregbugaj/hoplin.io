package io.hoplin;

/**
 * Fanout exchange
 *
 * A fanout exchange routes messages to all of the queues that are bound to it and the routing key is ignored.
 * If N queues are bound to a fanout exchange, when a new message is published to that exchange
 * a copy of the message is delivered to all N queues.
 *
 * Fanout exchanges are ideal for the broadcast routing of messages.
 *
 */
public class FanoutExchange extends AbstractExchange
{
    public FanoutExchange(String name)
    {
        super(name);
    }

    @Override
    public ExchangeType getType()
    {
        return ExchangeType.FANOUT;
    }
}
