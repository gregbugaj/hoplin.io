package io.hoplin.util;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(OsUtil.class);

  public static String getOsName() {
    return System.getProperty("os.name", "unknown");
  }

  public static String platform() {
    String osname = System.getProperty("os.name", "generic").toLowerCase();
    if (osname.startsWith("windows")) {
      return "win";
    } else if (osname.startsWith("linux")) {
      return "linux";
    } else if (osname.startsWith("sunos")) {
      return "solaris";
    } else if (osname.startsWith("mac") || osname.startsWith("darwin")) {
      return "mac";
    } else {
      return "generic";
    }
  }

  public static boolean isWindows() {
    return (getOsName().toLowerCase().contains("windows"));
  }

  public static boolean isLinux() {
    return getOsName().toLowerCase().contains("linux");
  }


  /**
   * Check if application is running under 'root account'
   */
  public static boolean checkRootAccess() {
    if (OsUtil.isLinux()) {
      int uid = 0;
      // if the user is 'root' like the result will be 0
      final ProcessResponse pr = ProcessHelper
          .execute(false, Arrays.asList("/usr/bin/id", "-u"), null);

      if (pr.success()) {
        String output = pr.getOutput();
        if (output != null) {
          try {
            output = output.replace(System.lineSeparator(), "");
            uid = Integer.parseInt(output);
          } catch (final NumberFormatException e) {
            throw new RuntimeException("Unable to get UID : " + output);
          }
        }
      }

      LOGGER.info("Access UID : {}", uid);
      return (uid == 0);
    }

    return false;
  }

}

