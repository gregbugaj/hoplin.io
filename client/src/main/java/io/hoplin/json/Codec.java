package io.hoplin.json;

import java.lang.reflect.Type;

/**
 * Codec interface used to encode/decode data
 */
public interface Codec {

  /**
   * Serialize object to <code>byte[]</code>
   *
   * @param value the value to serialize
   * @return serialized value as <code>byte[]</code>
   */
  byte[] serialize(final Object value);

  /**
   * Serialize object to <code>byte[]</code>
   *
   * @param value the value to serialize
   * @param clazz
   * @return serialized value as <code>byte[]</code>
   */
  byte[] serialize(final Object value, final Class<?> clazz);

  /**
   * Deserialize previously encoded data
   *
   * @param data the data to deserialize
   * @param clazz
   * @return
   */
  <E> E deserialize(final byte[] data, final Class<? extends E> clazz);

  /**
   * Deserialize data that might have generic type object Sample usage
   *
   * @param data the data to deserialize
   * @param type
   * @return
   */
  <E> E deserialize(final byte[] data, final Type type);
}
