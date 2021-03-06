package io.hoplin;

/**
 * Encapsulates all information regarding received message
 *
 * @see com.rabbitmq.client.Envelope
 */
public class MessageReceivedInfo {

    private final long deliveryTag;

    private final boolean redelivered;

    private final String exchange;

    private final String routingKey;

    private final String queue;

    private final long ctime;

    private final String consumerTag;

    public MessageReceivedInfo(final String consumerTag,
                               long deliveryTag,
                               boolean redelivered,
                               String exchange,
                               String routingKey,
                               String queue,
                               final long ctime) {

        this.consumerTag = consumerTag;
        this.deliveryTag = deliveryTag;
        this.redelivered = redelivered;
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.queue = queue;
        this.ctime = ctime;
    }

    public long getDeliveryTag() {
        return deliveryTag;
    }

    public boolean isRedelivered() {
        return redelivered;
    }

    public String getExchange() {
        return exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public long getCtime() {
        return ctime;
    }

    public String getConsumerTag() {
        return consumerTag;
    }

    public String getQueue() {
        return queue;
    }
}
