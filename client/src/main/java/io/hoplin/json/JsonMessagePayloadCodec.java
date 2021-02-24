package io.hoplin.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.hoplin.MessagePayload;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@code Codec} that can encode and decode objects
 *
 * @see MessagePayloadSerializer
 * @see MessagePayload
 */
public class JsonMessagePayloadCodec implements Codec {

  private static final JsonMessagePayloadCodec SERIALIZER = new JsonMessagePayloadCodec();

  private static final Logger log = LoggerFactory.getLogger(JsonMessagePayloadCodec.class);

  private final Gson gson;

  public JsonMessagePayloadCodec() {
    this(Collections.emptySet());
  }

  public JsonMessagePayloadCodec(Set<Class<?>> mappings) {
    this(mappings, builder -> {
    });
  }

  public JsonMessagePayloadCodec(final Set<Class<?>> handlerClasses,
      final Consumer<GsonBuilder> consumer) {
    Objects.requireNonNull(handlerClasses);
    final GsonBuilder builder = new GsonBuilder();
    builder.setPrettyPrinting();

    if (consumer != null) {
      consumer.accept(builder);
    }

    builder.registerTypeAdapter(byte[].class, new ByteArrayToBase64TypeAdapter());
    builder.registerTypeAdapter(Double.class, new DoubleJsonSerializer());
    builder.registerTypeAdapter(MessagePayload.class,
        new MessagePayloadSerializer(buildMappings(handlerClasses)));

    gson = builder.create();
  }

  /**
   * Serialize data using default serializer
   *
   * @param value
   * @return
   */
  public static byte[] serializeWithDefaults(final Object value) {
    Objects.requireNonNull(value);
    return SERIALIZER.serialize(value);
  }

  private Map<Class<?>, Set<String>> buildMappings(final Set<Class<?>> handlerClasses) {
    Objects.requireNonNull(handlerClasses);

    final Map<Class<?>, Set<String>> handlerClassFields = new HashMap<>();
    for (final Class<?> clz : handlerClasses) {
      final Set<String> names = new HashSet<>();
      final Field[] fields = clz.getDeclaredFields();
      for (final Field field : fields) {
        names.add(field.getName());
      }
      handlerClassFields.put(clz, names);
    }
    return handlerClassFields;
  }


  public byte[] serialize(final Object value) {
    Objects.requireNonNull(value);
    final long s = System.currentTimeMillis();
    try {
      return gson.toJson(value).getBytes(StandardCharsets.UTF_8);
    } finally {
      if (log.isTraceEnabled()) {
        log.trace("serialize time (ms) {}", (System.currentTimeMillis() - s));
      }
    }
  }

  @Override
  public byte[] serialize(Object value, Class<?> clazz) {
    Objects.requireNonNull(value);
    Objects.requireNonNull(clazz);

    final long s = System.currentTimeMillis();
    try {
      return gson.toJson(value, clazz).getBytes(StandardCharsets.UTF_8);
    } finally {
      if (log.isTraceEnabled()) {
        log.trace("serialize time (ms) {}", (System.currentTimeMillis() - s));
      }
    }
  }

  @Override
  public <E> E deserialize(final byte[] data, final Class<? extends E> clazz) {
    return deserialize(data, (Type) clazz);
  }

  @Override
  public <E> E deserialize(final byte[] data, final Type type) {
    final long s = System.currentTimeMillis();

    try {
      return gson.fromJson(new String(data, StandardCharsets.UTF_8), type);
    } catch (final Exception t) {
      log.error("Unable to deserialize", t);
    } finally {
      if (log.isTraceEnabled()) {
        log.trace("de-serialize time (ms) {}", (System.currentTimeMillis() - s));
      }
    }
    return null;
  }
}
