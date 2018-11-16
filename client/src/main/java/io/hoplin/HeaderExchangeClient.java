package io.hoplin;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Client is bound to individual {@link Binding}
 */
public class HeaderExchangeClient
{
    private static final Logger log = LoggerFactory.getLogger(HeaderExchangeClient.class);

    private final Binding binding;

    private RabbitMQClient client;

    public HeaderExchangeClient(final RabbitMQOptions options, final Binding binding, boolean publisher, boolean consumer)
    {
       Objects.requireNonNull(options);
       Objects.requireNonNull(binding);

       this.client = RabbitMQClient.create(options);
       this.binding = binding;

       bind(publisher, consumer);
    }

    /**
     * This will actively declare:
     *
     * a durable, non-autodelete exchange of "direct" type
     * a durable, non-exclusive, non-autodelete queue with a well-known name
     */
    private void bind(boolean publisher, boolean consumer)
    {
        final Channel channel = client.channel();
        final String exchangeName = binding.getExchange();
        final String queueName    = binding.getQueue();
        final String routingKey   = binding.getRoutingKey();
        final Map<String, Object> arguments = binding.getArguments();

        try
        {
            // Make sure that the Exchange and Queue are declared

            final AMQP.Exchange.DeclareOk exchangeDeclared = channel.exchangeDeclare(exchangeName, "header", true);
            final AMQP.Queue.DeclareOk queueDeclare = channel.queueDeclare(queueName, true, false, false, arguments);

            log.info("exchangeDeclared : {}", exchangeDeclared);
            log.info("queueDeclare : {}", queueDeclare);
            log.info("Binding client [exchangeName, queueName, routingKeys] : {}, {}, {}",exchangeName, queueName, routingKey);

            channel.queueBind(queueName, exchangeName, routingKey);

        }
        catch (final Exception e)
        {
            throw new HoplinRuntimeException("Unable to bind header exchange client", e);
        }

    }

    /**
     * Publish message to the queue with ALL defined routingKeys
     * @param message the message to request
     */
    public void basicPublish(final Object message)
    {
        final String routingKey  = binding.getRoutingKey();
        basicPublish(message, routingKey);
    }

    /**
     * Publish message to the queue with defined single routingKey
     *
     * @param message
     * @param routingKey
     */
    public void basicPublish(final Object message, final String routingKey)
    {
        final String exchangeName = binding.getExchange();
        client.basicPublish(exchangeName, routingKey, message);
    }

    public void queueBind(final Consumer<String> handler)
    {
        final String queue = binding.getQueue();
//        client.basicConsume(queue, handler);
    }

    /**
     * Create new {@link HeaderExchangeClient}
     *
     * @param options
     * @param binding
     * @param publisher
     * @param consumer
     * @return
     */
    public static HeaderExchangeClient create(final RabbitMQOptions options, final Binding binding, boolean publisher, boolean consumer)
    {
        return new HeaderExchangeClient(options, binding, publisher, consumer);
    }

}
