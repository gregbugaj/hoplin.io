package hoplin.io;

import com.rabbitmq.client.ConnectionFactory;

/**
 * Client options
 * @see {@ConnectionFactory}
 */
public class RabbitMQOptions
{
    /**
     * The default port = {@code - 1} - {@code 5671} for SSL otherwise {@code 5672}
     */
    public static final int DEFAULT_PORT = -1;

    /**
     * The default host = {@code localhost}
     */
    public static final String DEFAULT_HOST = ConnectionFactory.DEFAULT_HOST;

    /**
     * The default user = {@code guest}
     */
    public static final String DEFAULT_USER = ConnectionFactory.DEFAULT_USER;

    /**
     * The default password = {@code guest}
     */
    public static final String DEFAULT_PASSWORD = ConnectionFactory.DEFAULT_PASS;

    /**
     * The default virtual host = {@code /}
     */
    public static final String DEFAULT_VIRTUAL_HOST = ConnectionFactory.DEFAULT_VHOST;

    /**
     * The default connection timeout = {@code 60000}
     */
    public static final int DEFAULT_CONNECTION_TIMEOUT = ConnectionFactory.DEFAULT_CONNECTION_TIMEOUT;

    /**
     * The default connection timeout = {@code 60}
     */
    public static final int DEFAULT_REQUESTED_HEARTBEAT = ConnectionFactory.DEFAULT_HEARTBEAT;

    /**
     * The default handshake timeout = {@code 10000}
     */
    public static final int DEFAULT_HANDSHAKE_TIMEOUT = ConnectionFactory.DEFAULT_HANDSHAKE_TIMEOUT;

    /**
     * The default requested channel max = {@code 0}
     */
    public static final int DEFAULT_REQUESTED_CHANNEL_MAX = ConnectionFactory.DEFAULT_CHANNEL_MAX;

    /**
     * The default network recovery internal = {@code 5000}
     */
    public static final long DEFAULT_NETWORK_RECOVERY_INTERNAL = 5000L;

    /**
     * The default automatic recovery enabled = {@code false}
     */
    public static final boolean DEFAULT_AUTOMATIC_RECOVERY_ENABLED = false;

    /**
     * The default connection retry delay = {@code 10000}
     */
    public static final long DEFAULT_CONNECTION_RETRY_DELAY = 10000L;

    /**
     * The default connection retries = {@code null} (no retry)
     */
    public static final Integer DEFAULT_CONNECTION_RETRIES = null;

    private Integer connectionRetries = DEFAULT_CONNECTION_RETRIES;
    private long connectionRetryDelay = DEFAULT_CONNECTION_RETRY_DELAY;
    private String uri = null;
    private String user = DEFAULT_USER;
    private String password = DEFAULT_PASSWORD;
    private String host = DEFAULT_HOST;
    private String virtualHost = DEFAULT_VIRTUAL_HOST;
    private int port = DEFAULT_PORT;
    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    private int requestedHeartbeat = DEFAULT_REQUESTED_HEARTBEAT;
    private int handshakeTimeout = DEFAULT_HANDSHAKE_TIMEOUT;
    private int requestedChannelMax = DEFAULT_REQUESTED_CHANNEL_MAX;
    private long networkRecoveryInterval = DEFAULT_NETWORK_RECOVERY_INTERNAL;
    private boolean automaticRecoveryEnabled = DEFAULT_AUTOMATIC_RECOVERY_ENABLED;
    private boolean includeProperties = false;

    public RabbitMQOptions()
    {

    }

    public RabbitMQOptions(final RabbitMQOptions that)
    {
        connectionRetries = that.connectionRetries;
        connectionRetryDelay = that.connectionRetryDelay;
        uri = that.uri;
        user = that.user;
        password = that.password;
        host = that.host;
        virtualHost = that.virtualHost;
        port = that.port;
        connectionTimeout = that.connectionTimeout;
        requestedHeartbeat = that.requestedHeartbeat;
        handshakeTimeout = that.handshakeTimeout;
        networkRecoveryInterval = that.networkRecoveryInterval;
        automaticRecoveryEnabled = that.automaticRecoveryEnabled;
        includeProperties = that.includeProperties;
        requestedChannelMax = that.requestedChannelMax;
    }

    /**
     * Create default option set
     * @return
     */
    public static RabbitMQOptions defaults()
    {
        return new RabbitMQOptions();
    }

    public Integer getConnectionRetries()
    {
        return connectionRetries;
    }

    public RabbitMQOptions setConnectionRetries(final Integer connectionRetries)
    {
        this.connectionRetries = connectionRetries;
        return this;
    }

    public long getConnectionRetryDelay()
    {
        return connectionRetryDelay;
    }

    public RabbitMQOptions setConnectionRetryDelay(final long connectionRetryDelay)
    {
        this.connectionRetryDelay = connectionRetryDelay;
        return this;
    }

    public String getUri()
    {
        return uri;
    }

    public RabbitMQOptions setUri(final String uri)
    {
        this.uri = uri;
        return this;
    }

    public String getUser()
    {
        return user;
    }

    public RabbitMQOptions setUser(final String user)
    {
        this.user = user;
        return this;
    }

    public String getPassword()
    {
        return password;
    }

    public RabbitMQOptions setPassword(final String password)
    {
        this.password = password;
        return this;
    }

    public String getHost()
    {
        return host;
    }

    public RabbitMQOptions setHost(final String host)
    {
        this.host = host;
        return this;
    }

    public String getVirtualHost()
    {
        return virtualHost;
    }

    public RabbitMQOptions setVirtualHost(final String virtualHost)
    {
        this.virtualHost = virtualHost;
        return this;
    }

    public int getPort()
    {
        return port;
    }

    public RabbitMQOptions setPort(final int port)
    {
        this.port = port;
        return this;
    }

    public int getConnectionTimeout()
    {
        return connectionTimeout;
    }

    public RabbitMQOptions setConnectionTimeout(final int connectionTimeout)
    {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public int getRequestedHeartbeat()
    {
        return requestedHeartbeat;
    }

    public RabbitMQOptions setRequestedHeartbeat(final int requestedHeartbeat)
    {
        this.requestedHeartbeat = requestedHeartbeat;
        return this;
    }

    public int getHandshakeTimeout()
    {
        return handshakeTimeout;
    }

    public RabbitMQOptions setHandshakeTimeout(final int handshakeTimeout)
    {
        this.handshakeTimeout = handshakeTimeout;
        return this;
    }

    public int getRequestedChannelMax()
    {
        return requestedChannelMax;
    }

    public RabbitMQOptions setRequestedChannelMax(final int requestedChannelMax)
    {
        this.requestedChannelMax = requestedChannelMax;
        return this;
    }

    public long getNetworkRecoveryInterval()
    {
        return networkRecoveryInterval;
    }

    public RabbitMQOptions setNetworkRecoveryInterval(final long networkRecoveryInterval)
    {
        this.networkRecoveryInterval = networkRecoveryInterval;
        return this;
    }

    public boolean isAutomaticRecoveryEnabled()
    {
        return automaticRecoveryEnabled;
    }

    public RabbitMQOptions setAutomaticRecoveryEnabled(final boolean automaticRecoveryEnabled)
    {
        this.automaticRecoveryEnabled = automaticRecoveryEnabled;
        return this;
    }

    public boolean isIncludeProperties()
    {
        return includeProperties;
    }

    public RabbitMQOptions setIncludeProperties(final boolean includeProperties)
    {
        this.includeProperties = includeProperties;
        return this;
    }

}
