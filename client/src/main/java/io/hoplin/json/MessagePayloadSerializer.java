package io.hoplin.json;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.hoplin.HoplinRuntimeException;
import io.hoplin.MessagePayload;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Message payload serializer Messages exchanged between two clients are decorated with special JSON
 * tags to allow for generic type serialization.
 * <p>
 * If the '_payload_type_' is missing we will default to parsing the message based ont eh
 */
public class MessagePayloadSerializer implements JsonSerializer<MessagePayload>,
    JsonDeserializer<MessagePayload> {

  private static final String PROPERTY_NAME = "_payload_type_";

  private static final String STATUS_TAG = "status";

  private static final String PAYLOAD_TAG = "payload";

  private final Gson gson;

  public MessagePayloadSerializer() {
    this(new Gson());
  }

  public MessagePayloadSerializer(final Gson gson) {
    Objects.requireNonNull(gson, "Gson can't be null");
    this.gson = gson;
  }

  @SuppressWarnings("unchecked")
  @Override
  public MessagePayload deserialize(final JsonElement json, final Type typeOfT,
      final JsonDeserializationContext context) throws JsonParseException {
    final MessagePayload msg = new MessagePayload();
    // determine correct payload type
    if (json.isJsonObject()) {
      final JsonObject jsonObject = json.getAsJsonObject();
      final JsonElement propertyElement = jsonObject.get(PROPERTY_NAME);

      if (propertyElement == null) {
        final Map<String, JsonElement> raw = jsonObject
            .entrySet()
            .stream()
            .filter(e -> e.getValue().isJsonPrimitive())
            //.filter(e -> e.getValue().getAsJsonPrimitive().isString())
            .collect(Collectors.toMap(Entry::getKey, e -> e.getValue()));

        raw.forEach((key, val)->{
          System.out.println(key + " = " +val.isJsonPrimitive() + " = " + val);
        });

        throw new HoplinRuntimeException("Missing payloadtype tag : " + PROPERTY_NAME);
      }

      final JsonElement statusElement = jsonObject.get(STATUS_TAG);
      final String statusValue = statusElement.getAsString();
      final String payloadTypeName = propertyElement.getAsString();
      final JsonElement payload = jsonObject.get(PAYLOAD_TAG);

      try {
        final Class actualClass = Class.forName(payloadTypeName);
        final Object out = gson.fromJson(payload, actualClass);

        msg.setType(actualClass);
        msg.setPayload(out);
        msg.setStatus(Integer.parseInt(statusValue));

      } catch (final ClassNotFoundException e) {
        throw new JsonParseException(e.getMessage());
      }
    }

    return msg;
  }

  @Override
  public JsonElement serialize(final MessagePayload src, final Type typeOfSrc,
      final JsonSerializationContext context) {
    final JsonElement retValue = gson.toJsonTree(src);
    if (retValue.isJsonObject()) {
      final Object payload = src.getPayload();
      final Class<?> clazz = payload.getClass();

      retValue.getAsJsonObject().addProperty(PROPERTY_NAME, clazz.getName());
    }
    return retValue;
  }
}
