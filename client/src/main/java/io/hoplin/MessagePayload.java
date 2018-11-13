package io.hoplin;

import java.util.Objects;

/**
 * Message that will be sent over the wire with additional information;
 *
 * @param <T> The type of the payload
 */
public class MessagePayload<T>
{
    // Message being sent
    private T payload;

    // Type of the message : Class
    private String type;

    // Creation time
    private long ctime;

    public MessagePayload()
    {
        // serialization
    }

    public MessagePayload(final T msg)
    {
        this.payload = Objects.requireNonNull(msg);
        this.type = msg.getClass().getName();
        this.ctime = System.currentTimeMillis();
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

    /**
     * Get the type of message as class
     * @return
     */
    public Class<?> getTypeAsClass()
    {
        try
        {
            return Class.forName(type);
        }
        catch (final ClassNotFoundException e)
        {
            throw new HoplinRuntimeException("Can't create class for type :" + type, e);
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

    public long getCtime()
    {
        return ctime;
    }

}
