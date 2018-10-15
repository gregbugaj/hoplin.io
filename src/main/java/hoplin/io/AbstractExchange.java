package hoplin.io;

import java.util.Map;

public abstract class AbstractExchange implements Exchange
{
    private Map<String, Object> arguments;

    // exchange name
    private String name;

    // exchange is deleted when last queue is unbound from it
    private boolean autoDelete;

    // exchanges survive broker restart
    private boolean durable;

    private ExchangeType type;

    public AbstractExchange(final String name)
    {
        this.name = name;
    }

    @Override
    public Map<String, Object> getArguments()
    {
        return arguments;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public boolean isAutoDelete()
    {
        return autoDelete;
    }

    @Override
    public boolean isDurable()
    {
        return durable;
    }

    @Override
    public ExchangeType getType()
    {
        return type;
    }
}
