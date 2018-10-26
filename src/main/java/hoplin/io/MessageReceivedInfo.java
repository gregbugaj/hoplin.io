package hoplin.io;

/**
 * Encapsulates all information regarding received message
 * @see com.rabbitmq.client.Envelope
 */
public class MessageReceivedInfo
{
    private final long deliveryTag;
    private final boolean redeliver;
    private final String exchange;
    private final String routingKey;
    private long ctime;
    private String queue;
    private String consumerTag;

    public MessageReceivedInfo (final String consumerTag,
                               long deliveryTag,
                               boolean redeliver,
                               String exchange,
                               String routingKey,
                               String queue,
                               final long ctime)
    {

        this.consumerTag = consumerTag;
        this.deliveryTag = deliveryTag;
        this.redeliver = redeliver;
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.queue = queue;
        this.ctime = ctime;
    }

    public long getDeliveryTag()
    {
        return deliveryTag;
    }

    public boolean isRedeliver()
    {
        return redeliver;
    }

    public String getExchange()
    {
        return exchange;
    }

    public String getRoutingKey()
    {
        return routingKey;
    }

    public long getCtime()
    {
        return ctime;
    }

    public String getQueue()
    {
        return queue;
    }

    public String getConsumerTag()
    {
        return consumerTag;
    }
}
