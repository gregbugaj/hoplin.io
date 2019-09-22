package io.hoplin;

public class Reply<T> {

  private static Reply EMPTY = new Reply();

  private T reply;

  public Reply() {
  }

  public Reply(T reply) {
    this.reply = reply;
  }

  /**
   * Check if the reply is empty
   *
   * @return
   */
  public boolean isEmpty() {
    return reply == null;
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
    return "isEmpty:" + isEmpty() + " val:[" + reply + "]";
  }
}
