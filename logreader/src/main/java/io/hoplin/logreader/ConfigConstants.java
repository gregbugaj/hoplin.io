package io.hoplin.logreader;

public class ConfigConstants {

  public static final String SYSTEM_RUNTIME_ENVIRONMENT = "io.hoplin.environment";
  public static final String SYSTEM_RUNTIME_ENVIRONMENT_DIR = "io.hoplin.environment.dir";

  /**
   * All fields in this class should be accessed statically
   */
  private ConfigConstants() {
    throw new AssertionError();
  }
}
