package io.hoplin;

import com.rabbitmq.client.ConnectionFactory;
import io.hoplin.util.OsUtil;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Client options
 *
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

    private Map<String, Object> clientProperties;

    // location where the unroutable messages will be stored
    private Path unroutableDirectory = new File(FileSystems.getDefault().getPath(".").toFile(), "unroutable-messages").toPath();

    private boolean keepUnroutableMessages = true;

    public RabbitMQOptions()
    {
        this.clientProperties = createDefaultClientProperties();
    }

    public RabbitMQOptions(final Map<String, Object> clientProperties, boolean overwrite)
    {
        Objects.requireNonNull(clientProperties);
        this.clientProperties = createDefaultClientProperties();
        clientProperties.forEach((key, value)->{
            if(overwrite)
                this.clientProperties.put(key, value);
            else
                addValueIfNotExists(this.clientProperties, key, value);
        });
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
        clientProperties = that.clientProperties;
        keepUnroutableMessages = that.keepUnroutableMessages;
        unroutableDirectory = that.unroutableDirectory;
    }

    private Map<String, Object> createDefaultClientProperties()
    {
        final Map<String, Object> props = new HashMap<>();

        props.put("client_api", "hoplin.io");
        props.put("connection_name", findApplicationName());
        props.put("platform", OsUtil.platform());
        props.put("version", findApplicationVersion());
        props.put("application", findApplicationName());
        props.put("application_location", findWorkingDirectory());
        props.put("machine_name", getHostName());
        props.put("user", System.getProperty("user.name"));
        props.put("connected", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()));
        props.put("requested_heartbeat", Integer.toString(requestedHeartbeat));

        return props;
    }

    /**
     * Add/Set a client property
     * @param key the key to set
     * @param value the value to set to
     * @param overwrite  {@code true} indicates that the value will be set otherwise it will be added
     */
    public void setClientProperty(final String key, final Object value, boolean overwrite)
    {
        Objects.requireNonNull(key);
        if(overwrite)
            this.clientProperties.put(key, value);
        else
            addValueIfNotExists(this.clientProperties, key, value);
    }


    public Object getClientProperty(final String key)
    {
        Objects.requireNonNull(key);
        return clientProperties.get(key);
    }

    private String findApplicationVersion()
    {
        return "unknown";
    }

    private String findApplicationName()
    {
        return "unknown";
    }

    private String getHostName()
    {
        try
        {
            return  InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e)
        {
            // suppressed
        }

        return "localhost";
    }

    private String findWorkingDirectory()
    {
        return Paths.get("").toAbsolutePath().toString();
    }

    private void addValueIfNotExists(final Map<String, Object> props,
                                     final String key,
                                     final Object value)
    {
        if(!props.containsKey(key))
            props.put(key, value);
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

    public Map<String, Object> getClientProperties()
    {
        return clientProperties;
    }

    public RabbitMQOptions setClientProperties(final Map<String, Object> clientProperties)
    {
        this.clientProperties = clientProperties;
        return this;
    }

    public Path getUnroutableDirectory()
    {
        return unroutableDirectory;
    }

    public RabbitMQOptions setUnroutableDirectory(final Path unroutableDirectory)
    {
        this.unroutableDirectory = unroutableDirectory;
        return this;
    }

    public boolean isKeepUnroutableMessages()
    {
        return keepUnroutableMessages;
    }

    public RabbitMQOptions setKeepUnroutableMessages(final boolean keepUnroutableMessages)
    {
        this.keepUnroutableMessages = keepUnroutableMessages;
        return this;
    }

    /**
     * Parse the connection string in format key1=value;key2=value
     * <pre>host=localhost;virtualHost=vhost1;username=user;password=secret</pre>
     * @param connectionString the connection string to parse
     * @return the {@link RabbitMQOptions} created from the string
     */
    public static RabbitMQOptions from(final String connectionString)
    {
       return ConnectionStringParser.parse(connectionString);
    }

    @Override public String toString()
    {
        return String.format(  "Host:    '%s'%n"
                             + "Hostname:    '%s'%n"
                             + "VirtualHost: '%s'%n"
                             + "Port: '%s'%n"
                , getHost(), getHostName(), getVirtualHost(), getPort());
    }
}
