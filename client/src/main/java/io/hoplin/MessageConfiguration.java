package io.hoplin;

import com.rabbitmq.client.BasicProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Message configuration that will be populated for each outgoing message
 */
public class MessageConfiguration {

  private BasicProperties properties;

  private Map<String, Object> headers = new HashMap<>();

  public Map<String, Object> getHeaders() {
    return headers;
  }

  /**
   * Add new header
   *
   * @param key
   * @param value
   * @return
   */
  public Object addHeader(final String key, final Object value) {
    headers.put(key, value);
    return this;
  }

}
