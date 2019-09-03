package io.hoplin.util;

/**
 * Response create executed process
 */
public class ProcessResponse {

  private final int exitCode;

  private final String output;

  public ProcessResponse(final String output, final int exitCode) {
    this.output = output;
    this.exitCode = exitCode;
  }

  public boolean success() {
    return exitCode == 0;
  }

  public int getExitCode() {
    return exitCode;
  }

  public String getOutput() {
    return output;
  }

  @Override
  public String toString() {
    return exitCode + " : " + output;
  }
}
