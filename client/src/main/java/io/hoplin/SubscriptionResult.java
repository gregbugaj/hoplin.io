package io.hoplin;

/**
 * Client subscription results
 * This is populated from supplied binding
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
