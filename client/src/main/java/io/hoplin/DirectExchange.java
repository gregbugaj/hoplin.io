package io.hoplin;

/**
 * Direct Exchange
 *
 * A direct exchange delivers messages to queues based on the message routing key.
 * A direct exchange is ideal for the unicast routing of messages (although they can be used for multicast routing as well)
 *
 * <pre>
 *                key=load.era     +----------+
 *              +------------------+ Service A|
 *              |                  +----------+
 *              |
 *              | key=load.era     +----------+
 *              +------------------+ Service A|
 * +------------+                  +----------+
 * |            |
 * |  Exchange  |
 * |            |
 * |            | key=load.eob     +----------+
 * +------------+------------------+ Service B|
 *             |                   +----------+
 *             |
 *             |  key=load.eobl    +----------+
 *             +-------------------+ Service C|
 *                                 +----------+
 * </pre>
 */
public class DirectExchange extends AbstractExchange
{

    public DirectExchange(final String name)
    {
        super(name);
    }

    @Override
    public ExchangeType getType()
    {
        return ExchangeType.DIRECT;
    }
}
