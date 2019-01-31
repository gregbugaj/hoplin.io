package io.hoplin;

/**
 * Client subscription results, i
 * This is populated from supplied {@link Binding} and {@link SubscriptionConfig}
 */
public class SubscriptionResult
{
    private String exchange;

    private String queue;

    public SubscriptionResult(String exchangeName, String queueName)
    {
        this.exchange = exchangeName;
        this.queue = queueName;
    }

    public String getExchange()
    {
        return exchange;
    }

    public void setExchange(String exchange)
    {
        this.exchange = exchange;
    }

    public String getQueue()
    {
        return queue;
    }

    public void setQueue(String queue)
    {
        this.queue = queue;
    }
}
