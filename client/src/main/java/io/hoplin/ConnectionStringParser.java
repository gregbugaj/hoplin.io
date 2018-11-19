package io.hoplin;

public class ConnectionStringParser
{
    public static RabbitMQOptions parse(final String connectionString)
    {
        final String[] parts = connectionString.split(";");

        RabbitMQOptions options = new RabbitMQOptions();
        for(String part : parts)
        {
            final String[] kv = part.split("=");
            if(kv.length != 2)
                throw new RuntimeException("Invalid KeyValue pair");

            final String key = kv[0];
            final String value = kv[1];

            // host (e.g. host=localhost or host=192.168.2.56)
            // virtualHost (e.g. virtualHost=myVirtualHost) default is the default virtual host '/'

            if("host".equalsIgnoreCase(key))
                options.setHost(value);
            else if("virtualHost".equalsIgnoreCase(key))
                options.setVirtualHost(value);
            else if("username".equalsIgnoreCase(key))
                options.setUser(value);
            else if("password".equalsIgnoreCase(key))
                options.setPassword(value); 
            else if("requestedHeartbeat".equalsIgnoreCase(key))
                options.setRequestedHeartbeat(getIntVal(value));
            else if("prefetchcount".equalsIgnoreCase(key))
                options.setRequestedHeartbeat(getIntVal(value));
            else if("publisherConfirms".equalsIgnoreCase(key))
                options.setRequestedHeartbeat(getIntVal(value));
            else if("persistentMessages".equalsIgnoreCase(key))
                options.setRequestedHeartbeat(getIntVal(value));
            else if("timeout".equalsIgnoreCase(key))
                options.setRequestedHeartbeat(getIntVal(value));
            else if("product".equalsIgnoreCase(key))
                options.setRequestedHeartbeat(getIntVal(value));
            else if("platform".equalsIgnoreCase(key))
                options.setRequestedHeartbeat(getIntVal(value));
            else
                throw new IllegalArgumentException("Unknown option : " + key);
        }

        return options;
    }

    private static int getIntVal(final String value)
    {
        return Integer.parseInt(value);
    }

}
