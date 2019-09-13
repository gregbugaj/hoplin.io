package io.hoplin.json;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.hoplin.MessagePayload;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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

  private Map<Class<?>, Set<String>> handlerClassFields = new HashMap<>();

  public MessagePayloadSerializer() {
    this(new Gson());
  }

  public MessagePayloadSerializer(Map<Class<?>, Set<String>> handlerClassFields) {
    this(handlerClassFields, new Gson());
  }

  public MessagePayloadSerializer(final Gson gson) {
    Objects.requireNonNull(gson, "Gson can't be null");
    this.gson = gson;
  }

  private MessagePayloadSerializer(final Map<Class<?>, Set<String>> handlerClassFields,
      final Gson gson) {
    this.handlerClassFields = handlerClassFields;
    this.gson = gson;
  }

  @SuppressWarnings("unchecked")
  @Override
  public MessagePayload deserialize(final JsonElement json, final Type typeOfT,
      final JsonDeserializationContext context) throws JsonParseException {
    // determine correct payload type
    if (json.isJsonObject()) {
      final JsonObject jsonObject = json.getAsJsonObject();
      final JsonElement propertyElement = jsonObject.get(PROPERTY_NAME);

      if (propertyElement == null) {
        final Optional<Class<?>> located = locateBestMatchingType(jsonObject);
        final Class<?> type = located.orElseThrow(
            () -> new IllegalArgumentException("Unable to locate type for : " + jsonObject));
        final Object out = gson.fromJson(jsonObject, type);
        return MessagePayload.of(out, type, MessagePayload.SUCCESS);
      }

      final JsonElement statusElement = jsonObject.get(STATUS_TAG);
      final String statusValue = statusElement.getAsString();
      final String payloadTypeName = propertyElement.getAsString();
      final JsonElement payload = jsonObject.get(PAYLOAD_TAG);

      try {
        final Class actualClass = Class.forName(payloadTypeName);
        final Object out = gson.fromJson(payload, actualClass);
        return MessagePayload.of(out, actualClass, Integer.parseInt(statusValue));
      } catch (final ClassNotFoundException e) {
        throw new JsonParseException(e.getMessage());
      }
    } else if (json.isJsonPrimitive()) {
      return getMessagePayloadAsPrimitive(json);
    } else if (json.isJsonArray()) {
      return getMessagePayloadAsArrayPrimitive(json);
    }

    throw new IllegalStateException("Unsupported JSON message type : " + json);
  }

  private Optional<Class<?>> locateBestMatchingType(final JsonObject jsonObject) {
    final Map<String, JsonElement> raw = jsonObject
        .entrySet()
        .stream()
        .collect(Collectors.toMap(Entry::getKey, e -> e.getValue()));

    final Set<String> set1 = raw.keySet();
    int count = set1.size();
    Class<?> candidate = null;

    for (final Map.Entry<Class<?>, Set<String>> e : handlerClassFields.entrySet()) {
      final Class<?> key = e.getKey();
      final Set<String> set2 = e.getValue();
      final SetView<String> diffView = Sets.symmetricDifference(set1, set2);
      // sets are equal when both diff view is empty
      if (diffView.size() < count) {
        candidate = key;
      }
    }
    return Optional.ofNullable(candidate);
  }

  private MessagePayload getMessagePayloadAsArrayPrimitive(JsonElement json) {
    // we only allow arrays of primitives for Long[] and String[]
    final JsonArray array = json.getAsJsonArray();
    final int size = array.size();
    final List<String> stringList = new ArrayList<>();
    final List<Long> longList = new ArrayList<>();

    for (int i = 0; i < size; ++i) {
      final JsonElement elem = array.get(i);
      if (!elem.isJsonPrimitive()) {
        throw new IllegalStateException(
            "Only arrays of primitives are supported ex : [10, 20, 30, 40]");
      }
      final JsonPrimitive primitive = elem.getAsJsonPrimitive();
      if (primitive.isNumber()) {
        longList.add(elem.getAsLong());
      } else {
        stringList.add(elem.getAsString());
      }
    }

    if (longList.size() > 0) {
      return MessagePayload
          .of(longList.toArray(new Long[0]), Long[].class, MessagePayload.SUCCESS);
    } else {
      return MessagePayload
          .of(stringList.toArray(new String[0]), String[].class, MessagePayload.SUCCESS);
    }
  }

  private MessagePayload getMessagePayloadAsPrimitive(JsonElement json) {
    final JsonPrimitive primitive = json.getAsJsonPrimitive();
    if (primitive.isString()) {
      return MessagePayload.of(primitive.getAsString(), String.class, MessagePayload.SUCCESS);
    } else if (primitive.isBoolean()) {
      return MessagePayload.of(primitive.getAsBoolean(), Boolean.class, MessagePayload.SUCCESS);
    } else if (primitive.isNumber()) {
      final Number number = primitive.getAsNumber();
      if (("" + number).indexOf('.') > -1) {
        return MessagePayload.of(number.doubleValue(), Double.class, MessagePayload.SUCCESS);
      } else {
        return MessagePayload.of(number.longValue(), Long.class, MessagePayload.SUCCESS);
      }
    }
    // TODO : should throw??
    return null;
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
