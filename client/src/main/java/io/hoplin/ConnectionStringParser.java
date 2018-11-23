package io.hoplin;

import java.util.Objects;

/**
 * Connection string parser
 * Parse the connection string in format key1=value;key2=value
 * <pre>host=localhost;virtualHost=vhost1;username=user;password=secret</pre>
 *
 * <ul>
 *     <li>host (e.g. host=localhost or host=192.168.2.56) </li?>
 *     <li>virtualHost (e.g. virtualHost=myVirtualHost) default is the default virtual host '/' </li?>
 *     <li>prefetchcount (e.g. prefetchcount=1) default is 10. This is the number of messages that will be delivered by RabbitMQ before an ack is sent </li?>
 *     <li>publisherConfirms (e.g. publisherConfirms=true) default is false.</li?>
 *     <li>persistentMessages (e.g. persistentMessages=false) default is true. This determines how the delivery_mode in basic.properties is set when a message is published. false=1, true=2. When set to true, messages will be persisted to disk by RabbitMQ and survive a server restart. Performance gains can be expected when set to false.</li?>
 * </ul>
 */
public class ConnectionStringParser
{
    /**
     * Parse the connection string in format key1=value;key2=value
     * <pre>host=localhost;virtualHost=vhost1;username=user;password=secret</pre>
     * @param connectionString the connection string to parse
     * @return the {@link RabbitMQOptions} created from the string
     */
    public static RabbitMQOptions parse(final String connectionString)
    {
        Objects.requireNonNull(connectionString);
        final String[] parts = connectionString.split(";");
        final RabbitMQOptions options = new RabbitMQOptions();

        for(final String part : parts)
        {
            final String[] kv = part.split("=");
            if(kv.length != 2)
                throw new HoplinRuntimeException("Invalid KeyValue pair, expected connection string is in format 'host=localhost;virtualHost=vhost1' but got : " + part);

            final String key = kv[0].trim();
            final String value = kv[1].trim();

            if("host".equalsIgnoreCase(key))
                options.setHost(value);
            else if("virtualHost".equalsIgnoreCase(key))
                options.setVirtualHost(value);
            else if("username".equalsIgnoreCase(key))
                options.setUser(value);
            else if("password".equalsIgnoreCase(key))
                options.setPassword(value); 
            else if("requestedHeartbeat".equalsIgnoreCase(key))
                options.setRequestedHeartbeat(Integer.parseInt(value));
            else if("timeout".equalsIgnoreCase(key))
                options.setConnectionTimeout(Integer.parseInt(value));
             else if("product".equalsIgnoreCase(key))
                options.setClientProperty("product", value, true);
            else if("platform".equalsIgnoreCase(key))
                options.setClientProperty("platform", value, true);
            else if("connectionRetries".equalsIgnoreCase(key))
                options.setConnectionRetries(Integer.parseInt(value));
            else if("connectionRetryDelay".equalsIgnoreCase(key))
                options.setConnectionRetryDelay(Long.parseLong(value));
            else if("publisherConfirms".equalsIgnoreCase(key))
                options.setPublisherConfirms(Boolean.parseBoolean(value));
            else if("prefetchCount".equalsIgnoreCase(key))
                options.setPrefetchCount(Integer.parseInt(value));
            else
                throw new IllegalArgumentException("Unknown option : " + key);
        }

        return options;
    }

}
