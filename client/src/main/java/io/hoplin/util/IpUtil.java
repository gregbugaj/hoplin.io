package io.hoplin.util;

import io.hoplin.HostInfo;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpUtil {

  /**
   * Get information regarding the current host Information will be cached for later uses.
   *
   * @return
   */
  public static HostInfo getHostInfo() {
    try {
      final InetAddress inet = InetAddress.getLocalHost();
      return HostInfo.from(inet.getCanonicalHostName(), inet.getHostAddress());
    } catch (final UnknownHostException e) {
      // suppress
    }
    return HostInfo.from("localhost", "127.0.0.1");
  }
}
