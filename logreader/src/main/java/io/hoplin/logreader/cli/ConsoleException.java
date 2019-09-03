package io.hoplin.logreader.cli;

public class ConsoleException extends Exception {

  private static final long serialVersionUID = 1L;

  public ConsoleException(final String msg, final Throwable t) {
    super(msg, t);
  }

  public ConsoleException(final Throwable t) {
    super(t);
  }

}
