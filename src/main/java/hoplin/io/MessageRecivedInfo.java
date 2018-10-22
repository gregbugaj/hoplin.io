package hoplin.io;

/**
 * Encapsulates all information regarding received message
 * @see com.rabbitmq.client.Envelope
 */
public class MessageRecivedInfo
{
    private final long deliveryTag;
    private final boolean redeliver;
    private final String exchange;
    private final String routingKey;
    private long ctime ;
    private String queue;
    private String consumerTag;

    public MessageRecivedInfo(String consumerTag, long deliveryTag, boolean redeliver, String exchange, String routingKey, String queue)
    {

        this.consumerTag = consumerTag;
        this.deliveryTag = deliveryTag;
        this.redeliver = redeliver;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

}
