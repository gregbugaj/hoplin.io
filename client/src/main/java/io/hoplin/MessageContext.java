package io.hoplin;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

/**
 *  Context that message is associated with
 */
public class MessageContext
{
    private MessageReceivedInfo receivedInfo;

    private AMQP.BasicProperties properties;

    public static MessageContext create(final String consumerTag, final Envelope envelope, AMQP.BasicProperties properties)
    {
        final MessageReceivedInfo receivedInfo = new MessageReceivedInfo(consumerTag,
                                                                         envelope.getDeliveryTag(),
                                                                         envelope.isRedeliver(),
                                                                         envelope.getExchange(),
                                                                         envelope.getRoutingKey(),
                                                                         System.currentTimeMillis()
        );


        final MessageContext context  = new MessageContext();
        context.setReceivedInfo(receivedInfo);
        context.setProperties(properties);

        return  context;
    }

    public MessageReceivedInfo getReceivedInfo()
    {
        return receivedInfo;
    }

    public MessageContext setReceivedInfo(final MessageReceivedInfo receivedInfo)
    {
        this.receivedInfo = receivedInfo;
        return this;
    }

    public AMQP.BasicProperties getProperties()
    {
        return properties;
    }

    public MessageContext setProperties(final AMQP.BasicProperties properties)
    {
        this.properties = properties;
        return this;
    }
}
