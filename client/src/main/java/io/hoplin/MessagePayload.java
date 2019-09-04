package io.hoplin;

import java.util.Objects;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Message that will be sent over the wire with additional information;
 *
 * @param <T> The type of the payload
 */
public class MessagePayload<T> {

  public static int SUCCESS = 0;

  public static int FAILURE = 0;

  // 0 = Success
  // 1 = Failure
  private int status;

  // Message being sent
  private T payload;

  // Type of the message : Class
  private String type;

  // Creation time
  private long ctime;

  public MessagePayload() {
    // serialization
  }

  /**
   * Create new message payload
   *
   * @param msg the message to create
   */
  public MessagePayload(final T msg) {
    this(msg, SUCCESS);
  }

  /**
   * Create new message payload
   *
   * @param msg
   * @param status
   */
  public MessagePayload(final T msg, int status) {
    this.payload = Objects.requireNonNull(msg);
    this.type = msg.getClass().getName();
    this.ctime = System.currentTimeMillis();
    this.status = status;
  }

  public static MessagePayload error(final Throwable t) {
    return new MessagePayload<>(t, FAILURE);
  }

  public static <T> MessagePayload of(T out, Class<?> actualClass, int statusValue) {

    final MessagePayload msg = new MessagePayload();
    msg.setType(actualClass);
    msg.setPayload(out);
    msg.setStatus(statusValue);
    return msg;
  }

  public T getPayload() {
    return payload;
  }

  public MessagePayload<T> setPayload(final T payload) {
    this.payload = payload;
    return this;
  }

  public String getType() {
    return type;
  }

  public MessagePayload<T> setType(final Class type) {
    this.type = type.getName();
    return this;
  }

  public MessagePayload<T> setType(final String type) {
    this.type = type;
    return this;
  }

  /**
   * Get the type of message as class
   *
   * @return
   * @throws IllegalArgumentException if no class can be loaded
   */
  public Class<?> getTypeAsClass() {

    //boolean[]", "byte[]", "short[]
    String fqn = type;
    switch (type) {
      case "boolean":
        return Boolean.class;
      case "byte":
        return Byte.class;
      case "short":
        return Short.class;
      case "int":
        return Integer.class;
      case "long":
        return Long.class;
      case "float":
        return Float.class;
      case "double":
        return Double.class;
      case "char":
        return Character.class;
      default:
        try {
          return Class.forName(fqn);
        } catch (ClassNotFoundException ex) {
          throw new IllegalArgumentException("Class not found: " + fqn);
        }
    }
  }

  public long getCtime() {
    return ctime;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public boolean isFailure() {
    return status == 1;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.DEFAULT_STYLE);
  }
}
