package io.hoplin.logreader.cli;

import java.io.File;
import java.nio.file.Paths;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Environment that our application is being executed in
 */
public class Environment {

  public final static Logger LOGGER = LoggerFactory.getLogger(Environment.class);

  public final static File APPLICATION_DIR = new File(System.getProperty("user.dir"));
  private static Environment current;
  private final File configDir;
  private final String env;
  private ReplCliHandler replCliHandler;
  private boolean available;

  public Environment(final File configDir, final String env) {
    this.configDir = Objects.requireNonNull(configDir);
    this.env = Objects.requireNonNull(env);
  }

  public static synchronized Environment create(final String envdir, final String env) {
    if (current != null) {
      throw new IllegalStateException("Environment already created");
    }

    Objects.requireNonNull(env);
    LOGGER.info("Creating environment (dir, env) >  {}, {}", envdir, env);

    if (envdir != null) {
      final File envDir = new File(envdir);

      if (!envDir.exists() || !envDir.isDirectory()) {
        throw new RuntimeException("Invalid environment directory '" + envDir + "'");
      }

      current = new Environment(Paths.get(envDir.getAbsolutePath(), env).toFile(), env);
    }

    final File config = Paths.get(APPLICATION_DIR.getAbsolutePath(), "config", env).toFile();

    current = new Environment(config, env);
    current.configure();

    return current;
  }

  public static File getApplicationDir() {
    return APPLICATION_DIR;
  }

  public static Environment current() {
    if (current == null) {
      throw new IllegalStateException("Environment not initialized");
    }

    return current;
  }

  private void configure() {
    final String filename = "config.json";
    final File file = new File(configDir.getAbsolutePath(), filename);

    if (!file.exists()) {
      LOGGER.warn("config.json does not exits : {}", file);
      available = false;
      return;
    }

    available = true;
  }

  public File getConfigDir() {
    return configDir;
  }

  @Override
  public String toString() {
    return String.format("%s (env: %s)", configDir, env);
  }

  public ReplCliHandler getReplCliHandler() {
    return replCliHandler;
  }

  public void setReplCliHandler(final ReplCliHandler replCliHandler) {
    this.replCliHandler = replCliHandler;
  }


  /**
   * Check if the environment is available Some commands can be run without an environment required
   *
   * @return {@code true} if available {@code false} otherwise
   */
  public boolean isAvailable() {
    return available;
  }
}
