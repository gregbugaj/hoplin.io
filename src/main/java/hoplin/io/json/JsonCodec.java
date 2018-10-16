package hoplin.io.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hoplin.io.rpc.MessagePayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.function.Consumer;

/**
 * A {@code Codec} that can encode and decode objects to and from JSON
 */
public class JsonCodec implements Codec
{
    private static final Logger log = LoggerFactory.getLogger(JsonCodec.class);

    private final Gson gson;

    public JsonCodec()
    {
        this((builder)->{});
    }

    public JsonCodec(final Consumer<GsonBuilder> consumer)
    {
        final GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();

        if (consumer != null)
            consumer.accept(builder);

        builder.registerTypeAdapter(byte[].class, new ByteArrayToBase64TypeAdapter());
        builder.registerTypeAdapter(Double.class, new DoubleJsonSerializer());
        builder.registerTypeAdapter(MessagePayload.class, new MessagePayloadSerializer());

        gson = builder.create();
    }

    @Override
    public byte[] serialize(final Object value)
    {
        final long s = System.currentTimeMillis();
        try
        {
            final String payload = gson.toJson(value);
            return payload.getBytes();
        }
        finally
        {
            if(log.isDebugEnabled())
                log.debug("serialize time (ms) {}", (System.currentTimeMillis() - s));
        }
    }

    @Override
    public byte[] serialize(Object value, Class<?> clazz)
    {
        final long s = System.currentTimeMillis();
        try
        {
            final String payload = gson.toJson(value, clazz);
            return payload.getBytes();
        }
        finally
        {
            if(log.isDebugEnabled())
                log.debug("serialize time (ms) {}",  (System.currentTimeMillis() - s));
        }
    }

    @Override
    public <E> E deserialize(final byte[] data, final Class<? extends E> clazz)
    {
        final long s = System.currentTimeMillis();
        try
        {
            final String input = new String(data, "UTF-8");
            return  gson.fromJson(input, clazz);
        }
        catch (final Exception t)
        {
            log.error("Unable to deserialize", t);
        }
        finally
        {
            if(log.isDebugEnabled())
                log.debug("de-serialize time (ms) {}", (System.currentTimeMillis() - s));
        }
        return null;
    }

    @Override
    public <E> E deserialize(final byte[] data, final Type type)
    {
        final long s = System.currentTimeMillis();

        try
        {
            final String input = new String(data, "UTF-8");
            return gson.fromJson(input, type);
        }
        catch (final Exception t)
        {
            log.error("Unable to deserialize", t);
        }
        finally
        {
            log.debug("de-serialize time (ms) {}", (System.currentTimeMillis() - s));
        }

        return null;
    }
}
