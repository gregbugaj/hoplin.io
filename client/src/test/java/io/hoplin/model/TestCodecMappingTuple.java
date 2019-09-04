package io.hoplin.model;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class TestCodecMappingTuple {

  private long valA;

  private long valB;

  public long getValA() {
    return valA;
  }

  public TestCodecMappingTuple setValA(long valA) {
    this.valA = valA;
    return this;
  }

  public long getValB() {
    return valB;
  }

  public TestCodecMappingTuple setValB(long valB) {
    this.valB = valB;
    return this;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.DEFAULT_STYLE);
  }
}
