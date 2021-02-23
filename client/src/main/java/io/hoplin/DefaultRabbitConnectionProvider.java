package io.hoplin;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownSignalException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@ConnectionProvider} This provider handles basic retry and disconnect
 * policy.
 */
public class DefaultRabbitConnectionProvider implements ConnectionProvider {

  private static final Logger log = LoggerFactory.getLogger(DefaultRabbitConnectionProvider.class);

  private final ScheduledExecutorService executor;

  private final RabbitMQOptions config;

  private Connection connection;

  private Channel channel;

  public DefaultRabbitConnectionProvider(final RabbitMQOptions config) {
    this.config = Objects.requireNonNull(config);
    this.executor = Executors.newSingleThreadScheduledExecutor();
  }

  private static Connection newConnection(final RabbitMQOptions config)
      throws IOException, TimeoutException {
    final ConnectionFactory cf = new ConnectionFactory();
    final String uri = config.getUri();
    // Use uri if set, otherwise support individual connection parameters
    if (uri != null) {
      try {
        cf.setUri(uri);
      } catch (Exception e) {
        throw new IllegalArgumentException("Invalid rabbitmq connection uri " + uri);
      }
    } else {

      cf.setUsername(config.getUser());
      cf.setPassword(config.getPassword());
      cf.setHost(config.getHost());
      cf.setPort(config.getPort());
      cf.setVirtualHost(config.getVirtualHost());

    }

    if (config.isTlsEnabled()) {
      try {
        log.info("Enabling TLS for AMQP input [{}/{}].", config.getHost(), config.getVirtualHost());
        cf.useSslProtocol();
      } catch (NoSuchAlgorithmException | KeyManagementException e) {
        throw new IOException("Couldn't enable TLS for AMQP input.", e);
      }
    }

    cf.setConnectionTimeout(config.getConnectionTimeout());
    cf.setRequestedHeartbeat(config.getRequestedHeartbeat());
    cf.setHandshakeTimeout(config.getHandshakeTimeout());
    cf.setRequestedChannelMax(config.getRequestedChannelMax());
    cf.setNetworkRecoveryInterval(config.getNetworkRecoveryInterval());
    cf.setAutomaticRecoveryEnabled(config.isAutomaticRecoveryEnabled());

    cf.setClientProperties(config.getClientProperties());

    return cf.newConnection();
  }

  @Override
  public Channel acquire() {
    validateConnectionReady();
    if (!isOpenChannel()) {
      try {
        log.debug("channel is closed");
        channel = createChannel();
      } catch (final IOException e) {
        log.error("Channel is not available", e);
        throw new IllegalStateException("Channel is not available", e);
      }
    }
    return channel;
  }

  @Override
  public Channel acquirePublishChannel() {
    validateConnectionReady();
    throw new RuntimeException("Not implemented");
  }

  private void validateConnectionReady() {
    if (!isConnected()) {
      log.info("Not connected to AMQP, attempting reconnect");
      final boolean connected = connect();
      if (!connected) {
        throw new IllegalStateException("Client is not connected after reconnect attempt");
      }
    }
  }

  @Override
  public boolean isConnected() {
    return connection != null && connection.isOpen();
  }

  @Override
  public boolean isOpenChannel() {
    return channel != null && channel.isOpen();
  }

  @Override
  public boolean isAvailable() {
    return isConnected() && isOpenChannel();
  }

  @Override
  public boolean connect() {
    final Integer retries = config.getConnectionRetries();

    if (retries == null) {
      try {
        establishConnection(config);
      } catch (final IOException | TimeoutException e) {
        log.error("Unable to connect to rabbitmq", e);
        return false;
      }
    } else {
      long attempt = 0;
      long delay = config.getConnectionRetryDelay();

      while (attempt <= retries) {
        try {
          establishConnection(config);
          break;
        } catch (final IOException | TimeoutException e) {
          log.error("Unable to connect to rabbitmq", e);

          if (attempt >= retries) {
            log.info(
                "Max number of connect attempts ({}) reached. Will not attempt to connect again",
                retries);
            String msg =
                "Please check connection information and that the RabbitMQ Service is running at the specified endpoint.\n"
                    +
                    String.format("\tHostname: '%s'%n", config.getHost()) +
                    String.format("\tVirtualHost: '%s'%n", config.getVirtualHost()) +
                    String.format("\tUserName: '%s'%n", config.getUser());

            log.info(msg);
            return false;
          }

          log.info("Attempting to reconnect to rabbitmq [{}]...", attempt);

          try {
            TimeUnit.MILLISECONDS.sleep(delay);
          } catch (final InterruptedException ie) {
            Thread.currentThread().interrupt();
            break;
          }
        }

        attempt++;
      }
    }

    return true;
  }

  private void establishConnection(final RabbitMQOptions config)
      throws IOException, TimeoutException {
    if (log.isDebugEnabled()) {
      log.debug("Connecting to rabbitmq...");
    }
    long s = System.currentTimeMillis();
    connection = newConnection(config);
    channel = createChannel();
    long ms = System.currentTimeMillis() - s;
    if (log.isDebugEnabled()) {
      log.debug("Connected to rabbitmq in {} ms", ms);
    }
  }

  /**
   * Create and decorate our channel for possible reconnect
   *
   * @return
   * @throws IOException
   */
  private Channel createChannel() throws IOException {
    final Channel channel = connection.createChannel();
    channel.addShutdownListener(sse -> {
      if (sse.isInitiatedByApplication()) {
        log.info("Channel #{} closed.", channel.getChannelNumber());
      } else {
        log.info("Channel #{} suddenly closed.", channel.getChannelNumber());
        log.error("Channel closed", sse);
      }
    });
    return channel;
  }

  @Override
  public void disconnect() {
    this.executor.shutdownNow();
    try {
      if (log.isDebugEnabled()) {
        log.debug("Disconnecting  rabbitmq...");
      }

      if (channel != null) {
        try {
          log.info("Close Channel #{}...", channel.getChannelNumber());
          channel.close();
        } catch (final TimeoutException | IOException e) {
          log.warn("Unable to close channel", e);
        }
      }

      if (connection != null) {
        try {
          log.info("Close Connection...");
          connection.close();
        } catch (final IOException e) {
          log.error("Unable to close connection", e);
        }
      }

      if (log.isDebugEnabled()) {
        log.debug("Disconnected rabbitmq !");
      }
    } finally {
      connection = null;
      channel = null;
    }
  }

  private synchronized void abortConnection() {
    if (connection == null) {
      return;
    }
    try {
      connection.abort();
    } catch (final Exception e) {
      log.warn("Failed to abort connect due to " + e.getMessage());
    }

    connection = null;
  }

  @Override
  public void shutdownCompleted(final ShutdownSignalException cause) {
    if (cause.isInitiatedByApplication()) {
      log.info("Shutting down AMPQ consumer.");
      return;
    }

    abortConnection();
    final long delay = config.getReconnectDelay();
    log.info(
        "RabbitMQ connection shutdown! The client will attempt to reconnect automatically in : {} sec, caused by : {}",
        delay, cause);
    asyncWaitAndReconnect(delay);
  }

  private void asyncWaitAndReconnect(final long delay) {
    executor.schedule(this::connect, delay, TimeUnit.MILLISECONDS);
  }
}
