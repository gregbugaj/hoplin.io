package hoplin.io;

import java.util.Objects;

public class MessagePayload<T>
{
    private T payload;

    private String type;

    public MessagePayload()
    {
        // serialization
    }

    public MessagePayload(final T payload)
    {
        this.payload = Objects.requireNonNull(payload);
        this.type = payload.getClass().getName();
    }

    public T getPayload()
    {
        return payload;
    }

    public MessagePayload<T> setPayload(final T payload)
    {
        this.payload = payload;
        return this;
    }

    public String getType()
    {
        return type;
    }

    public Class<?> getTypeAsClass()
    {
        try
        {
            return Class.forName(type).getClass();
        }
        catch (ClassNotFoundException e)
        {
            throw new HoplinRuntimeException(e);
        }
    }


    public MessagePayload<T> setType(final Class type)
    {
        this.type = type.getName();
        return this;
    }

    public MessagePayload<T> setType(final String type)
    {
        this.type = type;
        return this;
    }

}
