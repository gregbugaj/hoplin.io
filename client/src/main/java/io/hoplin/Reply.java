package io.hoplin;

import java.util.Objects;

/**
 * Represents the result of a the request
 */
public class Reply<T> {

  private static final Reply<?> EMPTY = new Reply<>(null, null);

  // The reply value to the client
  private final T reply;

  // The exception hit during the execution of the request (or null if there was no exception).
  private Exception exception;

  public Reply(T reply, Exception exception) {
    this.reply = reply;
    this.exception = exception;
  }

  public Reply(T reply) {
    this.reply = reply;
  }

  /**
   * Reply payload that have completed exceptionally
   *
   * @param exception
   * @return
   */
  public static Reply<?> exceptionally(final Exception exception) {
    Objects.requireNonNull(exception);
    return new Reply<>(null, exception);
  }

  /**
   * Check if the reply is empty
   *
   * @return
   */
  public boolean isEmpty() {
    return reply == null;
  }

  /**
   * Check if the reply completed exceptionally
   *
   * @return
   */
  public boolean isExceptional() {
    return null != exception;
  }

  public Exception getException() {
    return exception;
  }

  public static <T> Reply<T> with(T reply) {
    return new Reply<T>(reply);
  }

  public static Reply<?> withEmpty() {
    return EMPTY;
  }

  public T getValue() {
    return reply;
  }

  @Override
  public String toString() {
    return "Reply{" +
        "isEmpty=" + isEmpty() +
        ", isExceptional=" + isExceptional() +
        '}';
  }
}
