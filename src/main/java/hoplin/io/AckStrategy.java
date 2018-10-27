package hoplin.io;

/**
 * Acknowledgement strategy
 */
public interface AckStrategy extends ThrowingBiConsumer<com.rabbitmq.client.Channel, java.lang.Long, Exception>
{

}

