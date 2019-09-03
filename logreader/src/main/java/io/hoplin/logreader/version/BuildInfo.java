package io.hoplin.logreader.version;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get build info provided in <code>build.properties</code> generated during compilation
 */
public class BuildInfo {

  private static final Logger LOGGER = LoggerFactory.getLogger(BuildInfo.class);

  public static Properties getManifest() {
    try (final InputStream is = BuildInfo.class.getClassLoader()
        .getResourceAsStream("META-INF/MANIFEST.MF")) {
      final Properties prop = new Properties();
      prop.load(is);

      return prop;
    } catch (final IOException ex) {
      LOGGER.error("Unable to read MANIFEST.MF", ex);
      throw new RuntimeException("Unable to read MANIFEST.MF", ex);
    }
  }

  public static String getBuildInfo() {
    try (final InputStream is = BuildInfo.class.getClassLoader()
        .getResourceAsStream("build.properties")) {
      final Scanner s = new Scanner(is, StandardCharsets.UTF_8.name()).useDelimiter("\\A");
      return s.hasNext() ? s.next() : "";
    } catch (final IOException ex) {
      LOGGER.error("Unable to read \"build.properties\"", ex);
      throw new RuntimeException("Unable to read build.properties\"", ex);
    }
  }

  public static Map<String, String> getBuildInfoAsMap() {
    final String buildInfo = getBuildInfo();

    return Arrays.stream(buildInfo.split(System.lineSeparator()))
        .map(s -> s.split("=")).collect(
            Collectors.toMap(k -> k[0], k -> k.length > 1 ? k[1] : ""));
  }
}
