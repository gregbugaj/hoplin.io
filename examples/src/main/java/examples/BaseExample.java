package examples;

import io.hoplin.RabbitMQOptions;

public abstract class BaseExample
{
    protected static RabbitMQOptions options()
    {
        final RabbitMQOptions options = new RabbitMQOptions();
        options.setConnectionRetries(3);
        options.setConnectionRetryDelay(250L);

        return options;
    }

    protected static RabbitMQOptions options(String connectionString)
    {
       return RabbitMQOptions.from(connectionString);
    }

}
