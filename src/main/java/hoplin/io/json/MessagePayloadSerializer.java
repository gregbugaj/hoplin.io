package hoplin.io.json;

import com.google.gson.*;
import hoplin.io.HoplinRuntimeException;
import hoplin.io.MessagePayload;

import java.lang.reflect.Type;

/**
 * Message payload serializer
 * Messages exchanged between two clients are decorated with special JSON tags
 * to allow for generic type serialization.
 */
public class MessagePayloadSerializer implements JsonSerializer<MessagePayload>, JsonDeserializer<MessagePayload>
{
    private static final String PROPERTY_NAME = "_payload_type_";

    private static final String PAYLOAD_TAG = "payload";

    private final Gson gson;

    public MessagePayloadSerializer()
    {
        gson = new Gson();
    }

    public MessagePayloadSerializer(final Gson gson)
    {
        this.gson = gson;
    }

    @Override
    public MessagePayload deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException
    {
        final MessagePayload msg = new MessagePayload();

        // determine correct payload type
        if (json.isJsonObject())
        {
            final JsonObject jsonObject = json.getAsJsonObject();
            final JsonElement propertyElement = jsonObject.get(PROPERTY_NAME);

            if(propertyElement == null)
                throw new HoplinRuntimeException("Missing payloadtype tag : " + PROPERTY_NAME);

            final String payloadTypeName = propertyElement.getAsString();
            final JsonElement payload = jsonObject.get(PAYLOAD_TAG);

            try
            {
                final Class actualClass = Class.forName(payloadTypeName);
                final Object out = gson.fromJson(payload, actualClass);

                msg.setType(actualClass);
                msg.setPayload(out);
            }
            catch (final ClassNotFoundException e)
            {
                throw new JsonParseException(e.getMessage());
            }
        }

        return msg;
    }

    @Override
    public JsonElement serialize(final MessagePayload src, final Type typeOfSrc,
                                 final JsonSerializationContext context)
    {
        final JsonElement retValue = gson.toJsonTree(src);
        if (retValue.isJsonObject())
        {
            final Object payload = src.getPayload();
            final Class<?> clazz = payload.getClass();

            retValue.getAsJsonObject().addProperty(PROPERTY_NAME, clazz.getName());
        }
        return retValue;
    }
}
