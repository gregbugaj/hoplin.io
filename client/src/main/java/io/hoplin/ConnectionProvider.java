package io.hoplin;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ShutdownListener;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

public interface ConnectionProvider extends ShutdownListener
{
    /**
     * Create default connection provider
     * @return
     */
    static ConnectionProvider create(final RabbitMQOptions config)
    {
        return new DefaultRabbitConnectionProvider(config);
    }

    /**
     * Acquire underlying {@link Channel}
     *
     * @return
     */
    Channel acquire();

    /**
     * Check if the have connection established
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
     * Check if the have connection established and channel is open
     *
     * @return true when the connection is open and channel is open, false otherwise
     */
    boolean isAvailable();

    /**
     * Disconnect client and the underlying channel
     *
     * @throws IOException
     */
    void disconnect() throws IOException;

    /**
     * Connect to our broker, this method is blocking
     *
     * @return {@link CompletableFuture<Boolean>}
     * @throws IOException
     * @throws TimeoutException
     */
    boolean connect() throws IOException, TimeoutException;
}

