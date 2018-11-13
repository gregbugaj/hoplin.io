package io.hoplin;

/**
 * Topic exchange
 *
 * Messages sent to a topic exchange can't have an arbitrary routing_key - it must be a list of words, delimited by dots.
 * The words can be anything, but usually they specify some features connected to the message.
 * A few valid routing key examples: "stock.usd.nyse", "nyse.vmw", "quick.orange.rabbit".
 * There can be as many words in the routing key as you like, up to the limit of 255 bytes.
 *
 * When a queue is bound with "#" (hash) binding key - it will receive all the messages, regardless of the routing key.
 *
 * When special characters "*" (star) and "#" (hash) aren't used in bindings, the topic exchange will behave just like a direct one.
 */
public class TopicExchange extends AbstractExchange
{
    public TopicExchange(final String name)
    {
        super(name);
    }

    @Override
    public ExchangeType getType()
    {
        return ExchangeType.TOPIC;
    }
}
