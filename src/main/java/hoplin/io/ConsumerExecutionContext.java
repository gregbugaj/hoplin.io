package hoplin.io;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

class ConsumerExecutionContext
{
    private MessageReceivedInfo receivedInfo;

    private AMQP.BasicProperties properties;

    public static ConsumerExecutionContext create(final String consumerTag, final Envelope envelope, AMQP.BasicProperties properties)
    {
        final MessageReceivedInfo receivedInfo = new MessageReceivedInfo(consumerTag,
                                                                         envelope.getDeliveryTag(),
                                                                         envelope.isRedeliver(),
                                                                         envelope.getExchange(),
                                                                         envelope.getRoutingKey(),
                                                                         System.currentTimeMillis()
        );


        final ConsumerExecutionContext context  = new ConsumerExecutionContext();
        context.setReceivedInfo(receivedInfo);
        context.setProperties(properties);

        return  context;
    }

    public MessageReceivedInfo getReceivedInfo()
    {
        return receivedInfo;
    }

    public ConsumerExecutionContext setReceivedInfo(final MessageReceivedInfo receivedInfo)
    {
        this.receivedInfo = receivedInfo;
        return this;
    }

    public AMQP.BasicProperties getProperties()
    {
        return properties;
    }

    public ConsumerExecutionContext setProperties(final AMQP.BasicProperties properties)
    {
        this.properties = properties;
        return this;
    }
}
