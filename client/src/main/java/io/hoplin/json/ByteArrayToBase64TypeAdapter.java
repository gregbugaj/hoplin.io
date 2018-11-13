package io.hoplin.json;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Base64;

public class ByteArrayToBase64TypeAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]>
{
    @Override
    public JsonElement serialize(final byte[] src, final Type typeOfSrc, final JsonSerializationContext context)
    {
        return new JsonPrimitive(Base64.getEncoder().encodeToString(src));
    }

    @Override
    public byte[] deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
            throws JsonParseException
    {
        return Base64.getDecoder().decode(json.getAsString());
    }
}
