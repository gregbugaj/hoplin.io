package hoplin.io;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Basic client that wraps most {@link Channel} operations, and provides number of convenience methods.
 */
public interface RabbitMQClient
{
    /**
     * Create new instance of {@link DefaultRabbitMQClient}
     * @param options the options to use to server the client
     * @return newly created instance of the client
     */
    static DefaultRabbitMQClient create(final RabbitMQOptions options)
    {
        Objects.requireNonNull(options);
        return new DefaultRabbitMQClient(options);
    }

    /**
     * Create new instance of {@link DefaultRabbitMQClient} with Default {@link RabbitMQOptions}
     * @return newly created instance of the client
     */
    static DefaultRabbitMQClient create()
    {
        return new DefaultRabbitMQClient(RabbitMQOptions.defaults());
    }

    /**
     * Check if a connection is open
     *
     * @return true when the connection is open, false otherwise
     * @see com.rabbitmq.client.ShutdownNotifier#isOpen()
     */
    boolean isConnected();

    /**
     * Check if a channel is open
     *
     * @return true when the connection is open, false otherwise
     * @see com.rabbitmq.client.ShutdownNotifier#isOpen()
     */
    boolean isOpenChannel();

    /**
     * Returns the number of messages in a queue ready to be delivered.
     *
     * @see com.rabbitmq.client.Channel#messageCount(String)
     */
    int messageCount(final String queue);

    /**
     * Returns the number of messages in a queue ready to be delivered.
     *
     * @see com.rabbitmq.client.Channel#messageCount(String)
     */
    CompletableFuture<Integer> messageCountAsync(final String queue);

    /**
     * Publish a message. Publishing to a non-existent exchange will result in a channel-level protocol exception,
     * which closes the channel. Invocations of Channel#request will eventually block if a resource-driven alarm is in effect.
     *
     * @see com.rabbitmq.client.Channel#basicPublish(String, String, AMQP.BasicProperties, byte[])
     */
    <T> void basicPublish(final String exchange, final String routingKey, final T message);

    /**
     * Acknowledge one or several received messages. Supply the deliveryTag create the AMQP.Basic.GetOk or AMQP.Basic.Deliver
     * method containing the received message being acknowledged.
     *
     * @see com.rabbitmq.client.Channel#basicAck(long, boolean)
     */

    void basicAck(long deliveryTag, boolean multiple);

    /**
     * Get the underlying {@link Channel}
     * @return
     */
    Channel channel();

    /**
     * Consume message create the queue
     *
     * @param queue the queue to consumer messages create
     * @param clazz the class of deserialized message
     * @param handler the handler to call when message have been consumed
     * @param <T> the message type
     */
    <T> void basicConsume(final String queue, final Class<T> clazz, final java.util.function.Consumer<T> handler);

    /**
     * Consume message create the queue
     *
     * @param queue the queue to consumer messages create
     * @param options the queue options on how messages will be consumed create the queue
     * @param clazz the class of dematerialized message
     * @param handler the handler to call when message have been consumed
     * @param <T> the message type
     */
    <T> void basicConsume(final String queue,
                          final QueueOptions options,
                          final Class<T> clazz,
                          final java.util.function.Consumer<T> handler);

    /**
     * Declare an exchange.
     *
     * @see com.rabbitmq.client.Channel#exchangeDeclare(String, String, boolean)
     */
    void exchangeDeclare(final String exchange, final String type, boolean durable, boolean autoDelete);

    /**
     * Declare an exchange.
     *
     * @see com.rabbitmq.client.Channel#exchangeDeclare(String, String, boolean  final Map<String, Object>)
     */
    void exchangeDeclare(final String exchange,
                         final String type,
                         boolean durable,
                         boolean autoDelete,
                         final Map<String, Object> arguments);

    /**
     * Declare a queue
     *
     * @see com.rabbitmq.client.Channel#queueDeclare(String, boolean, boolean, boolean, Map)
     */
    void queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete);

    /**
     * Declare a queue
     *
     * @see com.rabbitmq.client.Channel#queueDeclare(String, boolean, boolean, boolean, Map)
     */
    AMQP.Queue.DeclareOk queueDeclare(String queue,
                                      boolean durable,
                                      boolean exclusive,
                                      boolean autoDelete,
                                      final Map<String, Object> arguments);


    /**
     * Bind a queue to an exchange
     *
     * @see com.rabbitmq.client.Channel#queueBind(String, String, String)
     */
    void queueBind(String queue, String exchange, String routingKey);


    /**
     * Actively declare a server-named exclusive, autodelete, non-durable queue.
     *
     * @see com.rabbitmq.client.Channel#queueDeclare()
     */
    String queueDeclareTemporary();

    /**
     * Disconnect the client and all underlying Channels
     *
     * @throws IOException
     */
    void disconnect() throws IOException;
}
