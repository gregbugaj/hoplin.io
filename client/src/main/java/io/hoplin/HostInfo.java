package io.hoplin;

import java.util.Objects;

/**
 * Information related to the host
 */
public class HostInfo {

  private final String hostname;

  private final String address;

  private HostInfo(final String hostname, final String address) {
    this.hostname = Objects.requireNonNull(hostname);
    this.address = Objects.requireNonNull(address);
  }

  public static HostInfo from(final String hostname, final String address) {
    return new HostInfo(hostname, address);
  }

  public String getHostname() {
    return hostname;
  }

  public String getAddress() {
    return address;
  }

  @Override
  public String toString() {
    return "HostInfo{" +
        "hostname='" + hostname + '\'' +
        ", address='" + address + '\'' +
        '}';
  }
}
