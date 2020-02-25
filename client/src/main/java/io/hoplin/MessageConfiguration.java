package io.hoplin;

import com.rabbitmq.client.BasicProperties;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Message configuration that will be populated for each outgoing message
 */
public class MessageConfiguration {

  private boolean nativeMessageFormat;

  private BasicProperties properties;

  private final Map<String, Object> headers = new HashMap<>();

  public Map<String, Object> getHeaders() {
    return headers;
  }

  public MessageConfiguration() {
    nativeMessageFormat = false;
  }

  /**
   * Add new header
   *
   * @param key   the key value to add
   * @param value the value to add
   * @return
   */
  public Object addHeader(final String key, final Object value) {
    Objects.requireNonNull(key, "key can't be null");
    headers.put(key, value);
    return this;
  }

  public MessageConfiguration setNativeMessageFormat(boolean nativeMessageFormat) {
    this.nativeMessageFormat = nativeMessageFormat;
    return this;
  }

  public BasicProperties getProperties() {
    return properties;
  }

  public boolean isNativeMessageFormat() {
    return nativeMessageFormat;
  }

  @Override
  public String toString() {
    return "MessageConfiguration{" +
        "nativeMessageFormat=" + nativeMessageFormat +
        ", properties=" + properties +
        ", headers=" + headers +
        '}';
  }
}
