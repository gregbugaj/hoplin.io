package io.hoplin;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ShutdownListener;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * Connection provider to RabbitMQ
 */
public interface ConnectionProvider extends ShutdownListener {

  /**
   * Create default connection provider
   *
   * @return
   */
  static ConnectionProvider create(final RabbitMQOptions config) {
    return new DefaultRabbitConnectionProvider(config);
  }

  /**
   * Create default connection provider and try to establish connection
   *
   * @param options
   * @return
   */
  static ConnectionProvider createAndConnect(RabbitMQOptions options) {
    try {
      final ConnectionProvider provider = create(options);
      if (!provider.connect()) {
        throw new IllegalStateException("Unable to connect to broker : " + options);
      }

      return provider;
    } catch (final IOException | TimeoutException e) {
      throw new HoplinRuntimeException("Unable to connect to broker", e);
    }
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

