package io.hoplin;

/**
 * Acknowledgement strategy
 */
public interface AckStrategy extends ThrowingBiConsumer<com.rabbitmq.client.Channel, java.lang.Long, Exception>
{

}

