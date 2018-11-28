package io.hoplin;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownSignalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Default implementation of {@ConnectionProvider}
 * This provider handles basic retry and disconnect policy
 */
public class DefaultRabbitConnectionProvider implements ConnectionProvider
{
    private static final Logger log = LoggerFactory.getLogger(DefaultRabbitConnectionProvider.class);

    private final ScheduledExecutorService executor;

    private final RabbitMQOptions config;

    private Connection connection;

    private Channel channel;

    public DefaultRabbitConnectionProvider(final RabbitMQOptions config)
    {
        this.config = Objects.requireNonNull(config);
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public Channel acquire()
    {
        if(!isConnected())
            throw new IllegalStateException("Client is not connected");

        if(!isOpenChannel())
        {
            try
            {
                log.debug("channel is closed");
                channel = connection.createChannel();
            }
            catch (final IOException e)
            {
                log.error("Channel is not available", e);
                throw new IllegalStateException("Channel is not available", e);
            }
        }

        return channel;
    }

    @Override
    public boolean isConnected()
    {
        return connection != null && connection.isOpen();
    }

    @Override
    public boolean isOpenChannel()
    {
        return channel != null && channel.isOpen();
    }

    @Override
    public boolean isAvailable()
    {
        return isConnected() && isOpenChannel();
    }

    @Override
    public boolean connect()
    {
        final Integer retries = config.getConnectionRetries();

        if(retries == null)
        {
            try
            {
                establishConnection(config);
            }
            catch (final IOException | TimeoutException e)
            {
                log.error("Unable to connect to rabbitmq", e);
                return false;
            }
        }

        else
        {
            long attempt = 0;
            long delay = config.getConnectionRetryDelay();

            while(attempt <= retries)
            {
                try
                {
                    establishConnection(config);
                    break;
                }
                catch (final IOException | TimeoutException e)
                {
                    log.error("Unable to connect to rabbitmq", e);

                    if(attempt >= retries)
                    {
                        log.info("Max number of connect attempts ({}) reached. Will not attempt to connect again", retries);
                        String msg = "Please check connection information and that the RabbitMQ Service is running at the specified endpoint.\n" +
                                     String.format("\tHostname: '%s'%n", config.getHost()) +
                                     String.format("\tVirtualHost: '%s'%n", config.getVirtualHost()) +
                                     String.format("\tUserName: '%s'%n",  config.getUser());

                        log.info(msg);
                        return false;
                    }

                    log.info("Attempting to reconnect to rabbitmq [{}]...", attempt);

                    try
                    {
                        TimeUnit.MILLISECONDS.sleep(delay);
                    }
                    catch (final InterruptedException ie)
                    {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                attempt++;
            }
        }

        return true;
    }

    private void establishConnection(final RabbitMQOptions config) throws IOException, TimeoutException
    {
        long s = System.currentTimeMillis();
        log.debug("Connecting to rabbitmq...");
        connection = newConnection(config);
        channel = connection.createChannel();
        long ms = System.currentTimeMillis() - s;
        log.debug("Connected to rabbitmq in {} ms", ms);
    }

    private static Connection newConnection(final RabbitMQOptions config) throws IOException, TimeoutException
    {
        final ConnectionFactory cf = new ConnectionFactory();
        final String uri = config.getUri();
        // Use uri if set, otherwise support individual connection parameters
        if (uri != null)
        {
            try
            {
                cf.setUri(uri);
            }
            catch (Exception e)
            {
                throw new IllegalArgumentException("Invalid rabbitmq connection uri " + uri);
            }
        }
        else
        {
            cf.setUsername(config.getUser());
            cf.setPassword(config.getPassword());
            cf.setHost(config.getHost());
            cf.setPort(config.getPort());
            cf.setVirtualHost(config.getVirtualHost());
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
    public void disconnect() throws IOException
    {
        this.executor.shutdownNow();

        try
        {
            log.debug("Disconnecting create rabbitmq...");

            if (connection != null)
            {
                try
                {
                    connection.close();
                    channel.close();
                }
                catch (final TimeoutException | IOException e)
                {
                    log.error("Unable to close connection or channel", e);
                    throw new IOException("Unable to close connection or channel", e);
                }
            }

            log.debug("Disconnected create rabbitmq !");
        }
        finally
        {
            connection = null;
            channel = null;
        }
    }

    @Override
    public void shutdownCompleted(final ShutdownSignalException cause)
    {
        if (cause.isInitiatedByApplication())
            return;

        log.info("RabbitMQ connection shutdown! The client will attempt to reconnect automatically", cause);
        asyncWaitAndReconnect();
    }

    private void asyncWaitAndReconnect()
    {
        executor.schedule(this::connect, 5, TimeUnit.SECONDS);
    }
}
